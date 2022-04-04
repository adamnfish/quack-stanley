package com.adamnfish.quackstanley

import cats.data.EitherT
import cats.implicits._
import com.adamnfish.quackstanley.Logic._
import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.models.Validation._
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.persistence.GameIO._


object QuackStanley {
  val handSize = 6

  def dispatch(apiOperation: ApiOperation, config: Config): Attempt[ApiResponse] = {
    apiOperation match {
      case data: CreateGame =>
        createGame(data, config).widen[ApiResponse]
      case data: RegisterPlayer =>
        registerPlayer(data, config).widen[ApiResponse]
      case data: StartGame =>
        startGame(data, config).widen[ApiResponse]
      case data: BecomeBuyer =>
        becomeBuyer(data, config).widen[ApiResponse]
      case data: RelinquishBuyer =>
        relinquishBuyer(data, config).widen[ApiResponse]
      case data: StartPitch =>
        startPitch(data, config).widen[ApiResponse]
      case data: FinishPitch =>
        finishPitch(data, config).widen[ApiResponse]
      case data: AwardPoint =>
        awardPoint(data, config).widen[ApiResponse]
      case data: Mulligan =>
        mulligan(data, config).widen[ApiResponse]
      case data: Ping =>
        ping(data, config).widen[ApiResponse]
      case data: LobbyPing =>
        lobbyPing(data, config).widen[ApiResponse]
      case data: Wake =>
        wake(data, config).widen[ApiResponse]
    }
  }

  /**
    * Creates a new game and automatically registers this player as the creator.
    */
  def createGame(data: CreateGame, config: Config): Attempt[NewGame] = {
    val gameState = newGame(data.gameName, data.screenName)
    val playerKey = gameState.creator
    val playerState = newPlayer(gameState.gameId, gameState.gameName, data.screenName)
    for {
      _ <- validate(data)
      code <- makeUniquePrefix(gameState.gameId, config.persistence, checkPrefixUnique)
      _ <- writeGameState(gameState, config.persistence)
      _ <- writePlayerState(playerState, playerKey, config.persistence)
    } yield NewGame(playerState, gameState.creator, code)
  }

  /**
    * Adds a player with the provided screen name to the specified game.
    *
    * Note that this function does not update the game state to prevent race conditions, this will be
    * done once by the creator when the game is started.
    *
    * Because player info is only added to the game state when the game starts, we need to lookup all
    * the players' info separately to check if the screen name is unique.
    */
  def registerPlayer(data: RegisterPlayer, config: Config): Attempt[Registered] = {
    for {
      _ <- validate(data)
      gameId <- lookupGameIdFromCode(data.gameCode, config.persistence)
      gameState <- getGameState(gameId, config.persistence)
      _ <- verifyNotStarted(gameState)
      playerStates <- getRegisteredPlayers(gameId, config.persistence)
      _ <- verifyUniqueScreenName(data.screenName, playerStates)
      newPlayerKey = generatePlayerKey()
      playerState = newPlayer(gameState.gameId, gameState.gameName, data.screenName)
      _ <- writePlayerState(playerState, newPlayerKey, config.persistence)
    } yield Registered(playerState, newPlayerKey)
  }

  /**
    * Sets this game's state to "started" and sets up the initial player states (e.g. dealing them words).
    *
    * This is also the chance to write the player names/keys into the game state.
    * Doing it from here prevents race hazards since reading and writing S3 files is not atomic.
    */
  def startGame(data: StartGame, config: Config): Attempt[PlayerInfo] = {
    for {
      _ <- validate(data)
      gameState <- getGameState(data.gameId, config.persistence)
      _ <- authenticateCreator(data.playerKey, gameState)
      _ <- verifyNotStarted(gameState)
      playerStates <- getRegisteredPlayers(data.gameId, config.persistence)
      _ <- validatePlayerCount(playerStates.size)
      allWords <- Resources.words
      words <- nextWords(handSize * playerStates.size, allWords, Set.empty)
      players = playerSummaries(playerStates)
      dealtPlayers <- dealWordsToAllPlayers(words, playerStates)
      creatorState <- lookupPlayer(dealtPlayers, data.playerKey)
      updatedGameState = startGameState(gameState, players)
      _ <- writeGameState(updatedGameState, config.persistence)
      _ <- writePlayerStates(dealtPlayers, config.persistence)
    } yield playerInfo(data.playerKey, creatorState, updatedGameState)
  }

  /**
    * Signals that the player would like to be the next "buyer".
    * We can deal them a role and wait for people to pitch to that player.
    */
  def becomeBuyer(data: BecomeBuyer, config: Config): Attempt[PlayerInfo] = {
    for {
      _ <- validate(data)
      gameState <- getGameState(data.gameId, config.persistence)
      _ <- authenticate(data.playerKey, gameState)
      _ <- verifyNoBuyer(gameState)
      players <- getRegisteredPlayers(data.gameId, config.persistence)
      player <- lookupPlayer(players, data.playerKey)
      allRoles <- Resources.roles
      role <- nextRole(allRoles, usedRoles(players.values.toList))
      playerWithRole = player.copy(role = Some(role))
      gameWithBuyer = gameState.copy(round = Some(Round(data.playerKey, role, Map.empty)))
      _ <- writeGameState(gameWithBuyer, config.persistence)
      _ <- writePlayerState(playerWithRole, data.playerKey, config.persistence)
    } yield playerInfo(data.playerKey, playerWithRole, gameWithBuyer)
  }

  /**
    * Allows player to stop being the game's "buyer" without granting a point to another player.
    */
  def relinquishBuyer(data: RelinquishBuyer, config: Config): Attempt[PlayerInfo] = {
    for {
      _ <- validate(data)
      gameState <- getGameState(data.gameId, config.persistence)
      _ <- authenticate(data.playerKey, gameState)
      _ <- authenticateBuyer(data.playerKey, gameState)
      player <- getPlayerState(data.playerKey, data.gameId, config.persistence)
      gameWithoutBuyer = gameState.copy(round = None)
      playerWithoutBuyer = player.copy(role = None)
      _ <- writeGameState(gameWithoutBuyer, config.persistence)
      _ <- writePlayerState(playerWithoutBuyer, data.playerKey, config.persistence)
    } yield playerInfo(data.playerKey, playerWithoutBuyer, gameWithoutBuyer)
  }

  /**
   * Marks this player as "pitching" so that other clients can see that.
    *
    * Is this necessary?
   */
  def startPitch(data: StartPitch, config: Config): Attempt[PlayerInfo] = {
    // auth
    // validate no one else is pitching
    // write player state
    // update game state
    EitherT.leftT(
      Failure("start pitch is not implemented", "'start pitch' functionality has not yet been created", 404).asFailedAttempt
    )
  }

  /**
    * Discards player's words, replaces them with new ones.
    */
  def finishPitch(data: FinishPitch, config: Config): Attempt[PlayerInfo] = {
    for {
      _ <- validate(data)
      gameState <- getGameState(data.gameId, config.persistence)
      _ <- authenticate(data.playerKey, gameState)
      players <- getRegisteredPlayers(data.gameId, config.persistence)
      playerState <- lookupPlayer(players, data.playerKey)
      allWords <- Resources.words
      used = usedWords(players.values.toList)
      refillWords <- nextWords(2, allWords, used)
      discardedPlayerState <- discardWords(data.words, playerState)
      refilledPlayerState <- fillHand(refillWords, discardedPlayerState)
      gameStateWithPitch <- updateGameWithPitch(gameState, data.playerKey, data.words)
      _ <- writePlayerState(refilledPlayerState, data.playerKey, config.persistence)
      _ <- writeGameState(gameStateWithPitch, config.persistence)
    } yield playerInfo(data.playerKey, refilledPlayerState, gameStateWithPitch)
  }

  /**
    * Ends the round and gives the point (word) to another player.
    */
  def awardPoint(data: AwardPoint, config: Config): Attempt[PlayerInfo] = {
    for {
      _ <- validate(data)
      gameState <- getGameState(data.gameId, config.persistence)
      _ <- authenticateBuyer(data.playerKey, gameState)
      players <- getRegisteredPlayers(data.gameId, config.persistence)
      playerState <- lookupPlayer(players, data.playerKey)
      _ <- playerHasRole(playerState, data.role)
      updatedPlayerState = playerState.copy(role = None)
      (winningPlayerKey, winningPlayer) <- lookupPlayerByName(players, data.awardToPlayerWithName)
      updatedGameState <- updateGameWithAwardedPoint(gameState, winningPlayerKey, data.role)
      updatedWinningPlayer = addRoleToPoints(winningPlayer, data.role)
      _ <- writePlayerState(updatedPlayerState, data.playerKey, config.persistence)
      _ <- writePlayerState(updatedWinningPlayer, winningPlayerKey, config.persistence)
      _ <- writeGameState(updatedGameState, config.persistence)
    } yield playerInfo(data.playerKey, updatedPlayerState, updatedGameState)
  }

  /**
    * Player discards a point (if they have one) and their current hand.
    * Player is given a new hand of words.
    */
  def mulligan(data: Mulligan, config: Config): Attempt[PlayerInfo] = {
    // auth
    // verify role is provided if they have any points
    // discard words
    // get new words for the player
    EitherT.leftT(
      Failure("mulligan is not implemented", "'mulligan' functionality has not yet been created", 404).asFailedAttempt
    )
  }

  /**
    * return current state for the player.
    *
    * Ping is called very regularly so optimisations are in place to reduce costs.
    */
  def ping(data: Ping, config: Config): Attempt[PlayerInfo] = {
    for {
      _ <- validate(data)
      // kick off requests in parallel to speed up response
      fGameState = getGameState(data.gameId, config.persistence)
      fPlayerState = getPlayerState(data.playerKey, data.gameId, config.persistence)
      gameState <- fGameState
      _ <- authenticate(data.playerKey, gameState)
      playerState <- fPlayerState
    } yield playerInfo(data.playerKey, playerState, gameState)
  }

  /**
    * return current state for the player with a description of who has joined the game.
    *
    * LobbyPing is called regularly so optimisations are in place to reduce costs.
    */
  def lobbyPing(data: LobbyPing, config: Config): Attempt[PlayerInfo] = {
    for {
      _ <- validate(data)
      // kick off requests in parallel to speed up response
      fGameState = getGameState(data.gameId, config.persistence)
      fPlayerState = getPlayerState(data.playerKey, data.gameId, config.persistence)
      gameState <- fGameState
      _ <- authenticateCreator(data.playerKey, gameState)
      _ <- verifyNotStarted(gameState)
      playerStates <- getRegisteredPlayers(data.gameId, config.persistence)
      playerState <- fPlayerState
    } yield lobbyPlayerInfo(data.playerKey, playerState, playerStates)
  }

  /**
    * No-op to wake the Lambda.
    */
  def wake(data: Wake, config: Config): Attempt[Ok] = {
    EitherT.pure(Ok("ok"))
  }
}
