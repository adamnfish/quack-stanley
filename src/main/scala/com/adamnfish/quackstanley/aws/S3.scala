package com.adamnfish.quackstanley.aws

import com.adamnfish.quackstanley.Config
import com.adamnfish.quackstanley.attempt.{Attempt, Failure, LambdaIntegration}
import com.adamnfish.quackstanley.models.Serialization._
import com.adamnfish.quackstanley.models.{GameId, GameState, PlayerKey, PlayerState}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import io.circe.Json
import io.circe.parser.parse
import io.circe.syntax._

import scala.concurrent.ExecutionContext
import scala.io.Source
import scala.util.control.NonFatal
import scala.collection.JavaConverters._


object S3 {
  def client(): AmazonS3 = {
    AmazonS3ClientBuilder
      .standard()
      .withRegion("eu-west-1")
      .build()
  }

  private[aws] def gameStatePath(gameId: GameId): String =
    s"data/${gameId.value}/game.json"

  private[aws] def playerStatePath(gameId: GameId, playerKey: PlayerKey): String =
    s"${playerStateDir(gameId)}${playerKey.value}.json"

  private[aws] def playerStateDir(gameId: GameId): String =
    s"data/${gameId.value}/players/"

  def writeGameState(gameState: GameState, config: Config): Attempt[Unit] = {
    writeJson(gameState.asJson, gameStatePath(gameState.gameId), config)
  }

  def getGameState(gameId: GameId, config: Config)(implicit ec: ExecutionContext): Attempt[GameState] = {
    for {
      json <- getJson(gameStatePath(gameId), config)
      gameState <- LambdaIntegration.extractJson[GameState](json)
    } yield gameState
  }

  def writePlayerState(playerState: PlayerState, playerKey: PlayerKey, config: Config): Attempt[Unit] = {
    writeJson(playerState.asJson, playerStatePath(playerState.gameId, playerKey), config)
  }

  def getPlayerState(playerKey: PlayerKey, gameId: GameId, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerState] = {
    for {
      json <- getJson(gameStatePath(gameId), config)
      playerState <- LambdaIntegration.extractJson[PlayerState](json)
    } yield playerState
  }

  def getRegisteredPlayers(gameId: GameId, config: Config)(implicit ec: ExecutionContext): Attempt[Map[PlayerKey, String]] = {
    for {
      playerKeyIds <- listFiles(playerStateDir(gameId), config)
      playerKeys = playerKeyIds.map(PlayerKey)
      playerStates <- Attempt.traverse(playerKeys) { playerKey =>
        getPlayerState(playerKey, gameId, config).map(playerKey -> _)
      }
    } yield playerStates.map { case (playerKey, playerState) =>
      playerKey -> playerState.screenName
    }.toMap
  }

  // TODO maybe run in a Future?
  private def getJson(path: String, config: Config): Attempt[Json] = {
    try {
      val s3obj = config.s3Client.getObject(config.s3Bucket, path)
      val objStream = s3obj.getObjectContent
      Attempt.fromEither(parse(Source.fromInputStream(objStream, "UTF-8").mkString).left.map { parsingFailure =>
        Failure(s"Failed to parse JSON from S3 object $path", "Failed to parse persistent data", 500).asAttempt
      })
    } catch {
      case NonFatal(e) =>
        Attempt.Left {
          Failure(s"Error fetching file from S3 $path", "Error fetching persistent data", 500, Some(e.getMessage)).asAttempt
        }
    }
  }

  private def writeJson(json: Json, path: String, config: Config): Attempt[Unit] = {
    try {
      Attempt.Right {
        config.s3Client.putObject(config.s3Bucket, path, json.noSpaces)
      }
    } catch {
      case NonFatal(e) =>
        Attempt.Left {
          Failure(s"Error writing JSON to S3 $path", "Error writing persistent data", 500, Some(e.getMessage)).asAttempt
        }
    }
  }

  private def listFiles(path: String, config: Config): Attempt[List[String]] = {
    try {
      Attempt.Right {
        val objectListings = config.s3Client.listObjects(config.s3Bucket, path)
        objectListings.getObjectSummaries.asScala.toList.map(_.getKey)
      }
    } catch {
      case NonFatal(e) =>
        Attempt.Left {
          Failure(s"Error listing files in S3 $path", "Error reading from persistent data storage", 500, Some(e.getMessage))
        }
    }
  }
}
