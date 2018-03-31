package com.adamnfish.quackstanley

import com.adamnfish.quackstanley.Logic._
import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.models.Validation._
import com.adamnfish.quackstanley.persistence.GameIO._

import scala.concurrent.ExecutionContext


object QuackStanley {
  val handSize = 5

  /**
    * Creates a new game and automatically registers this player as the creator.
    */
  def createGame(data: CreateGame, config: Config)(implicit ec: ExecutionContext): Attempt[NewGame] = {
    val gameState = newGame(data.gameName, data.screenName)
    val playerKey = gameState.creator
    val playerState = newPlayer(gameState.gameId, gameState.gameName, data.screenName)
    for {
      _ <- validate(data)
      code <- makeUniquePrefix(gameState.gameId, config, checkPrefixUnique)
      _ <- writeGameState(gameState, config)
      _ <- writePlayerState(playerState, playerKey, config)
    } yield NewGame(playerState, gameState.creator, code)
  }

  /**
    * Adds a player with the provided screen name to the specified game.
    *
    * Note that this function does not update the game state to prevent race conditions, this will be
    * done once by the creator when the game is started.
    *
    * TODO: allow unique prefix of game ID to specify game (min 4 chars).
    * TODO: Optionally find a way to map dictionary(/game) words to key prefixes for easy sharing.
    */
  def registerPlayer(data: RegisterPlayer, config: Config)(implicit ec: ExecutionContext): Attempt[Registered] = {
    for {
      _ <- validate(data)
      gameId <- lookupGameIdFromCode(data.gameCode, config)
      gameState <- getGameState(gameId, config)
      _ <- verifyNotStarted(gameState)
      newPlayerKey = generatePlayerKey()
      playerState = newPlayer(gameState.gameId, gameState.gameName, data.screenName)
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
      _ <- validate(data)
      gameState <- getGameState(data.gameId, config)
      _ <- authenticateCreator(data.playerKey, gameState)
      _ <- verifyNotStarted(gameState)
      playerStates <- getRegisteredPlayers(data.gameId, config)
      allWords <- Resources.words()
      words <- nextWords(handSize * playerStates.size, allWords, Set.empty)
      players = playerSummaries(playerStates)
      dealtPlayers <- dealWordsToAllPlayers(words, playerStates)
      creatorState <- lookupPlayer(dealtPlayers, data.playerKey)
      updatedGameState = startGameState(gameState, players)
      _ <- writeGameState(updatedGameState, config)
      _ <- writePlayerStates(dealtPlayers, config)
    } yield playerInfo(data.playerKey, creatorState, updatedGameState)
  }

  /**
    * Signals that the player would like to be the next "buyer".
    * We can deal them a role and wait for people to pitch to that player.
    */
  def becomeBuyer(data: BecomeBuyer, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
    for {
      _ <- validate(data)
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
    } yield playerInfo(data.playerKey, playerWithRole, gameWithBuyer)
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
    Attempt.Left(
      Failure("start pitch is not implemented", "'start pitch' functionality has not yet been created", 404)
    )
  }

  /**
    * Discards player's words, replaces them with new ones.
    */
  def finishPitch(data: FinishPitch, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
    for {
      _ <- validate(data)
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
    } yield playerInfo(data.playerKey, refilledPlayerState, gameState)
  }

  /**
    * Ends the round and gives the point (word) to another player.
    */
  def awardPoint(data: AwardPoint, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
    for {
      _ <- validate(data)
      gameState <- getGameState(data.gameId, config)
      _ <- authenticateBuyer(data.playerKey, gameState)
      players <- getRegisteredPlayers(data.gameId, config)
      playerState <- lookupPlayer(players, data.playerKey)
      _ <- playerHasRole(playerState, data.role)
      updatedPlayerState = playerState.copy(role = None)
      updatedGameState = gameState.copy(buyer = None)
      winningPlayerDetails <- lookupPlayerByName(players, data.awardToPlayerWithName)
      (winningPlayerKey, winningPlayer) = winningPlayerDetails
      updatedWinningPlayer = addRoleToPoints(winningPlayer, data.role)
      _ <- writePlayerState(updatedPlayerState, data.playerKey, config)
      _ <- writePlayerState(updatedWinningPlayer, winningPlayerKey, config)
      _ <- writeGameState(updatedGameState, config)
    } yield playerInfo(data.playerKey, updatedPlayerState, gameState)
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
    Attempt.Left(
      Failure("mulligan is not implemented", "'mulligan' functionality has not yet been created", 404)
    )
  }

  /**
    * return current state for the player.
    */
  def ping(data: Ping, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
    for {
      _ <- validate(data)
      gameState <- getGameState(data.gameId, config)
      _ <- authenticate(data.playerKey, gameState)
      playerState <- getPlayerState(data.playerKey, gameState.gameId, config)
    } yield playerInfo(data.playerKey, playerState, gameState)
  }

  /**
    * No-op to wake the Lambda.
    */
  def wake(data: Wake, config: Config)(implicit ec: ExecutionContext): Attempt[Ok] = {
    Attempt.Right(Ok("ok"))
  }
}
