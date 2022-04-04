package com.adamnfish.quackstanley.persistence

import cats.data.EitherT
import com.adamnfish.quackstanley.Logic
import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.models.Serialization._
import com.adamnfish.quackstanley.models._
import io.circe.syntax._
import cats.implicits._


object GameIO {
  private val root = "data"

  def gameCodePath(gameCode: String): String =
    s"$root/$gameCode"

  def gameStatePath(gameId: GameId): String =
    s"$root/${gameId.value}/game.json"

  def playerStatePath(gameId: GameId, playerKey: PlayerKey): String =
    s"${playerStateDir(gameId)}/${playerKey.value}.json"

  def playerStateDir(gameId: GameId): String =
    s"$root/${gameId.value}/players"

  def writeGameState(gameState: GameState, persistence: Persistence): Attempt[Unit] = {
    persistence.writeJson(gameState.asJson, gameStatePath(gameState.gameId))
  }

  private val PlayerKeyFromPath = (root ++ "/[a-z0-9\\-]+/players/([a-z0-9\\-]+).json").r

  def playerKeyFromPath(path: String): Attempt[PlayerKey] = {
    path match {
      case PlayerKeyFromPath(key) =>
        EitherT.pure(PlayerKey(key))
      case _ =>
        EitherT.leftT(
          Failure(s"Could not extract player key from path $path", "Failed to lookup player data", 500).asFailedAttempt
        )
    }
  }

  def getGameState(gameId: GameId, persistence: Persistence): Attempt[GameState] = {
    for {
      json <- persistence.getJson(gameStatePath(gameId))
      gameState <- Serialization.extractJson[GameState](json)
    } yield gameState
  }

  def lookupGameIdFromCode(gameCode: String, persistence: Persistence): Attempt[GameId] = {
    val normalisedGameCode = gameCode.toLowerCase
    for {
      paths <- persistence.listFiles(gameCodePath(normalisedGameCode))
      gameId <- Logic.gameIdFromPrefixResults(normalisedGameCode, paths)
    } yield gameId
  }

  def writePlayerState(playerState: PlayerState, playerKey: PlayerKey, persistence: Persistence): Attempt[Unit] = {
    persistence.writeJson(playerState.asJson, playerStatePath(playerState.gameId, playerKey))
  }

  def writePlayerStates(playerState: Map[PlayerKey, PlayerState], persistence: Persistence): Attempt[Unit] = {
    playerState.toList.traverse { case (key, state) =>
      writePlayerState(state, key, persistence)
    }.as(())
  }

  def getPlayerState(playerKey: PlayerKey, gameId: GameId, persistence: Persistence): Attempt[PlayerState] = {
    for {
      json <- persistence.getJson(playerStatePath(gameId, playerKey))
      playerState <- Serialization.extractJson[PlayerState](json)
    } yield playerState
  }

  def getRegisteredPlayers(gameId: GameId, persistence: Persistence): Attempt[Map[PlayerKey, PlayerState]] = {
    for {
      paths <- persistence.listFiles(playerStateDir(gameId))
      playerKeys <- paths.traverse(playerKeyFromPath)
      playerStates <- playerKeys.traverse { playerKey =>
        getPlayerState(playerKey, gameId, persistence).map(playerKey -> _)
      }
    } yield playerStates.toMap
  }

  def checkPrefixUnique(gameId: GameId, prefixLength: Int, persistence: Persistence): Attempt[Boolean] = {
    val prefix = s"$root/${gameId.value.take(prefixLength)}"
    for {
      matches <- persistence.listFiles(prefix)
    } yield matches.isEmpty
  }
}
