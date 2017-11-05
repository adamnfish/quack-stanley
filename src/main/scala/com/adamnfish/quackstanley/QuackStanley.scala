package com.adamnfish.quackstanley

import com.adamnfish.quackstanley.Logic._
import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.persistence.GameIO._

import scala.concurrent.ExecutionContext


object QuackStanley {
  /**
    * Creates a new game and automatically registers this player as the creator.
    */
  def createGame(data: CreateGame, config: Config)(implicit ec: ExecutionContext): Attempt[Registered] = {
    val gameState = newGame(data.gameName, data.screenName)
    val playerKey = gameState.creator
    val playerState = newPlayer(gameState.gameId, gameState.gameName, data.screenName)
    for {
      _ <- writeGameState(gameState, config)
      _ <- writePlayerState(playerState, playerKey, config)
    } yield Registered(playerState, gameState.creator)
  }

  /**
    * Adds a player with the provided screen name to the specified game.
    *
    * Note that this function does not update the game state to prevent race conditions, this will be
    * done once by the creator when the game is started.
    */
  def registerPlayer(data: RegisterPlayer, config: Config)(implicit ec: ExecutionContext): Attempt[Registered] = {
    for {
      gameState <- getGameState(data.gameId, config)
      newPlayerKey = generatePlayerKey()
      playerState = newPlayer(gameState.gameId, gameState.gameName,data.playerName)
      _ <- writePlayerState(playerState, newPlayerKey, config)
    } yield Registered(playerState, newPlayerKey)
  }

  /**
    * Sets this game's state to "started".
    *
    * This is also the chance to write the player names/keys into the game state.
    * Doing it from here prevents race hazards since reading and writing S3 files is not atomic.
    *
    * TODO: deal words and role
    */
  def startGame(data: StartGame, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
    for {
      gameState <- getGameState(data.gameId, config)
      _ <- authenticateCreator(data.playerKey, gameState)
      players <- getRegisteredPlayers(data.gameId, config)
      playerNames = players.values.toList
      updatedGameState = gameState.copy(
        players = players,
        started = true
      )
      _ <- writeGameState(updatedGameState, config)
      playerState <- getPlayerState(data.playerKey, data.gameId, config)
    } yield PlayerInfo(playerState, started = true, playerNames)
  }

  /**
   * Is this required?
   */
//  def startPitch

  /**
    * Discards player's words, replaces them with new ones.
    */
  def finishPitch(data: FinishPitch, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
    // auth
    // check words belong to player
    // discard cards
    // refill player's hand
    ???
  }

  /**
    * Ends the round and gives the point (word) to another player.
    * Starts the next round by assigning a role to the next player.
    */
  def awardPoint(data: AwardPoint, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
    // auth as buyer
    // lookup winning player
    // add word to winning player's points
    // find next player
    // assign next player a word
    ???
  }

  /**
    * Player discards a point (if they have one) and their current hand.
    * Player is given a new hand of words.
    */
  def mulligan(data: Mulligan, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
    // auth
    // verify role is provided if they have any points
    // discard words
    // get new words for the player
    ???
  }

  /**
    * return current state for the player.
    * This is particularly to check for
    */
  def ping(data: Ping, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
    // auth
    // return player state
    ???
  }
}
