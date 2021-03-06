package com.adamnfish.quackstanley

import com.adamnfish.quackstanley.Logic._
import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.models.Validation._
import com.adamnfish.quackstanley.persistence.GameIO._

import scala.concurrent.ExecutionContext


object QuackStanley {
  val handSize = 6

  def dispatch(apiOperation: ApiOperation, config: Config)(implicit ec: ExecutionContext): Attempt[ApiResponse] = {
    apiOperation match {
      case data: CreateGame => createGame(data, config)
      case data: RegisterPlayer => registerPlayer(data, config)
      case data: StartGame => startGame(data, config)
      case data: BecomeBuyer => becomeBuyer(data, config)
      case data: RelinquishBuyer => relinquishBuyer(data, config)
      case data: StartPitch => startPitch(data, config)
      case data: FinishPitch => finishPitch(data, config)
      case data: AwardPoint => awardPoint(data, config)
      case data: Mulligan => mulligan(data, config)
      case data: Ping => ping(data, config)
      case data: LobbyPing => lobbyPing(data, config)
      case data: Wake => wake(data, config)
    }
  }

  /**
    * Creates a new game and automatically registers this player as the creator.
    */
  def createGame(data: CreateGame, config: Config)(implicit ec: ExecutionContext): Attempt[NewGame] = {
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
  def registerPlayer(data: RegisterPlayer, config: Config)(implicit ec: ExecutionContext): Attempt[Registered] = {
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
  def startGame(data: StartGame, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
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
  def becomeBuyer(data: BecomeBuyer, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
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
  def relinquishBuyer(data: RelinquishBuyer, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
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
  def awardPoint(data: AwardPoint, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
    for {
      _ <- validate(data)
      gameState <- getGameState(data.gameId, config.persistence)
      _ <- authenticateBuyer(data.playerKey, gameState)
      players <- getRegisteredPlayers(data.gameId, config.persistence)
      playerState <- lookupPlayer(players, data.playerKey)
      _ <- playerHasRole(playerState, data.role)
      updatedPlayerState = playerState.copy(role = None)
      winningPlayerDetails <- lookupPlayerByName(players, data.awardToPlayerWithName)
      (winningPlayerKey, winningPlayer) = winningPlayerDetails
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
    *
    * Ping is called very regularly so optimisations are in place to reduce costs.
    */
  def ping(data: Ping, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
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
  def lobbyPing(data: LobbyPing, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
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
  def wake(data: Wake, config: Config)(implicit ec: ExecutionContext): Attempt[Ok] = {
    Attempt.Right(Ok("ok"))
  }
}
