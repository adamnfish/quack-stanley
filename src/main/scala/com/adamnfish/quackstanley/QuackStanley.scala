package com.adamnfish.quackstanley

import com.adamnfish.quackstanley.Logic._
import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.persistence.GameIO._

import scala.concurrent.ExecutionContext


object QuackStanley {
  val handSize = 5

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
    * Sets this game's state to "started" and sets up the initial player states (e.g. dealing them words).
    *
    * This is also the chance to write the player names/keys into the game state.
    * Doing it from here prevents race hazards since reading and writing S3 files is not atomic.
    */
  def startGame(data: StartGame, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
    for {
      gameState <- getGameState(data.gameId, config)
      _ <- authenticateCreator(data.playerKey, gameState)
      players <- getRegisteredPlayers(data.gameId, config)
      allRoles <- Resources.roles()
      allWords <- Resources.words()
      words <- nextWords(handSize * players.size, allWords, Set.empty)
      role <- nextRole(allRoles, Set.empty)
      names = makePlayerNames(players)
      dealtPlayers <- dealWordsToAllPlayers(words, players)
      creatorState <- lookupPlayer(dealtPlayers, data.playerKey)
      updatedGameState = startGameState(gameState, names)
      _ <- writeGameState(updatedGameState, config)
    } yield PlayerInfo(creatorState, updatedGameState)
  }

  /**
    * Signals that the player would like to be the next "buyer".
    * We can deal them a role and wait for people to pitch to that player.
    */
  def becomeBuyer(data: BecomeBuyer, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
    for {
      gameState <- getGameState(data.gameId, config)
      _ <- authenticate(data.playerKey, gameState)
      _ <- verifyNoBuyer(gameState)
      players <- getRegisteredPlayers(data.gameId, config)
      player <- lookupPlayer(players, data.playerKey)
      allRoles <- Resources.roles()
      role <- nextRole(allRoles, usedRoles(players.values.toList))
      playerWithRole = player.copy(role = Some(role))
      gameWithBuyer = gameState.copy(buyer = Some(data.playerKey))
      _ <- writeGameState(gameWithBuyer, config)
      _ <- writePlayerState(playerWithRole, data.playerKey, config)
    } yield PlayerInfo(playerWithRole, gameWithBuyer)
  }

  /**
   * Marks this player as "pitching" so that other clients can see that.
    *
    * Is this necessary?
   */
  def startPitch(data: StartPitch, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
    // auth
    // validate no one else is pitching
    // write player state
    // update game state
    ???
  }

  /**
    * Discards player's words, replaces them with new ones.
    */
  def finishPitch(data: FinishPitch, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
    for {
      gameState <- getGameState(data.gameId, config)
      _ <- authenticate(data.playerKey, gameState)
      players <- getRegisteredPlayers(data.gameId, config)
      playerState <- lookupPlayer(players, data.playerKey)
      allWords <- Resources.words()
      used = usedWords(players.values.toList)
      refillWords <- nextWords(2, allWords, used)
      discardedPlayerState <- discardWords(data.words, playerState)
      refilledPlayerState <- fillHand(refillWords, discardedPlayerState)
      _ <- writePlayerState(refilledPlayerState, data.playerKey, config)
    } yield PlayerInfo(refilledPlayerState, gameState)
  }

  /**
    * Ends the round and gives the point (word) to another player.
    */
  def awardPoint(data: AwardPoint, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
    // auth as buyer
    // lookup winning player
    // add word to winning player's points
    // set game state to "no buyer"
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
    */
  def ping(data: Ping, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
    for {
      gameState <- getGameState(data.gameId, config)
      _ <- authenticate(data.playerKey, gameState)
      playerState <- getPlayerState(data.playerKey, gameState.gameId, config)
    } yield PlayerInfo(playerState, gameState)
  }
}
