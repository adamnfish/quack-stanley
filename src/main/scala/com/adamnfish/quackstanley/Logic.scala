package com.adamnfish.quackstanley

import java.util.UUID

import com.adamnfish.quackstanley.models.{GameId, GameState, PlayerKey, PlayerState}
import org.joda.time.DateTime


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
        playerKey -> newPlayer(gameId, gameName, playerName)
      )
    )
  }

  def newPlayer(gameId: GameId, gameName: String, screenName: String): PlayerState = {
    PlayerState(gameId, gameName, screenName, Nil, Nil, None, Nil)
  }
}
