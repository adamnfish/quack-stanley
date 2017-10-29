package com.adamnfish.quackstanley

import java.util.UUID

import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.models.{GameId, GameState, PlayerKey, PlayerState}
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext


object Logic {
  def generateGameId(): GameId = {
    GameId(UUID.randomUUID.toString)
  }

  def generatePlayerKey(): PlayerKey = {
    PlayerKey(UUID.randomUUID().toString)
  }

  def newGame(gameName: String, playerName: String): GameState = {
    val gameId = generateGameId()
    val playerKey = generatePlayerKey()
    GameState(
      gameId, gameName, DateTime.now(), started = false, playerKey,
      Map(
        playerKey -> playerName
      )
    )
  }

  def newPlayer(gameId: GameId, gameName: String, screenName: String): PlayerState = {
    PlayerState(gameId, gameName, screenName, Nil, Nil, None, Nil)
  }

  def authenticate(playerKey: PlayerKey, gameState: GameState)(implicit ec: ExecutionContext): Attempt[String] = {
    Attempt.fromOption(gameState.players.get(playerKey),
      Failure("Player key not found in game state", "Invalid player", 404).asAttempt
    )
  }
}
