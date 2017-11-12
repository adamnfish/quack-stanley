package com.adamnfish.quackstanley.persistence

import com.adamnfish.quackstanley.Config
import com.adamnfish.quackstanley.attempt.{Attempt, Failure, LambdaIntegration}
import com.adamnfish.quackstanley.models._
import io.circe.syntax._
import com.adamnfish.quackstanley.models.Serialization._

import scala.concurrent.ExecutionContext


object GameIO {
  def gameStatePath(gameId: GameId): String =
    s"data/${gameId.value}/game.json"

  def playerStatePath(gameId: GameId, playerKey: PlayerKey): String =
    s"${playerStateDir(gameId)}${playerKey.value}.json"

  def playerStateDir(gameId: GameId): String =
    s"data/${gameId.value}/players/"

  def writeGameState(gameState: GameState, config: Config): Attempt[Unit] = {
    config.ioClient.writeJson(gameState.asJson, gameStatePath(gameState.gameId), config)
  }

  private val PlayerKeyFromPath = "data/[a-z0-9\\-]+/players/([a-z0-9\\-]+).json".r

  def playerKeyFromPath(path: String): Attempt[PlayerKey] = {
    path match {
      case PlayerKeyFromPath(key) =>
        Attempt.Right(PlayerKey(key))
      case _ =>
        Attempt.Left(
          Failure(s"Could not extract player key from path $path", "Faild to lookup player data", 500)
        )
    }
  }

  def getGameState(gameId: GameId, config: Config)(implicit ec: ExecutionContext): Attempt[GameState] = {
    for {
      json <- config.ioClient.getJson(gameStatePath(gameId), config)
      gameState <- Serialization.extractJson[GameState](json)
    } yield gameState
  }

  def writePlayerState(playerState: PlayerState, playerKey: PlayerKey, config: Config): Attempt[Unit] = {
    config.ioClient.writeJson(playerState.asJson, playerStatePath(playerState.gameId, playerKey), config)
  }

  def getPlayerState(playerKey: PlayerKey, gameId: GameId, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerState] = {
    for {
      json <- config.ioClient.getJson(playerStatePath(gameId, playerKey), config)
      playerState <- Serialization.extractJson[PlayerState](json)
    } yield playerState
  }

  def getRegisteredPlayers(gameId: GameId, config: Config)(implicit ec: ExecutionContext): Attempt[Map[PlayerKey, PlayerState]] = {
    for {
      paths <- config.ioClient.listFiles(playerStateDir(gameId), config)
      playerKeys <- Attempt.traverse(paths)(playerKeyFromPath)
      playerStates <- Attempt.traverse(playerKeys) { playerKey =>
        getPlayerState(playerKey, gameId, config).map(playerKey -> _)
      }
    } yield playerStates.toMap
  }
}