package com.adamnfish.quackstanley

import cats.data.EitherT
import cats.effect.IO
import com.adamnfish.quackstanley.attempt.{Attempt, FailedAttempt, Failure}
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.persistence.Persistence
import org.joda.time.DateTime

import java.util.UUID
import scala.util.Random.shuffle


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
      gameId, gameName, DateTime.now(), started = false, playerKey, None,
      Map(
        playerKey -> PlayerSummary(playerName, Nil)
      )
    )
  }

  def newPlayer(gameId: GameId, gameName: String, screenName: String): PlayerState = {
    PlayerState(gameId, gameName, screenName, Nil, Nil, None, Nil)
  }

  def playerInfo(playerKey: PlayerKey, playerState: PlayerState, gameState: GameState): PlayerInfo = {
    val otherPlayers = gameState.players.filterNot { case (key, _) =>
        playerKey == key
    }
    val roundInfo = gameState.round.map(roundToRoundInfo(_, gameState))
    PlayerInfo(playerState, gameState.started, otherPlayers.values.toList, roundInfo)
  }

  def lobbyPlayerInfo(playerKey: PlayerKey, playerState: PlayerState, playerStates: Map[PlayerKey, PlayerState]): PlayerInfo = {
    val otherPlayers = playerSummaries(playerStates)
      .filterNot { case (opponentKey, _) =>
        playerKey == opponentKey
      }
      .values
    PlayerInfo(playerState, started = false, otherPlayers.toList, round = None)
  }

  def roundToRoundInfo(round: Round, gameState: GameState): RoundInfo = {
    RoundInfo(
      // could make this function an attempt, but the game state would have
      // to be very broken for this to fail, shouldn't be possible?
      gameState.players(round.buyerKey).screenName,
      round.role,
      round.products.flatMap { case (playerKey, words) =>
        gameState.players.get(playerKey).map(_.screenName -> words)
      }
    )
  }

  def authenticate(playerKey: PlayerKey, gameState: GameState): Attempt[PlayerSummary] = {
    EitherT.fromOption[IO](gameState.players.get(playerKey),
      Failure("Player key not found in game state", "You are not a player in this game", 404).asFailedAttempt
    )
  }

  def authenticateCreator(playerKey: PlayerKey, gameState: GameState): Attempt[PlayerKey] = {
    if (gameState.creator == playerKey) {
      EitherT.pure(gameState.creator)
    } else {
      EitherT.leftT {
        Failure("Player key not found in game state", "You are not the game's creator", 404).asFailedAttempt
      }
    }
  }

  def authenticateBuyer(playerKey: PlayerKey, gameState: GameState): Attempt[PlayerKey] = {
    EitherT.fromOption[IO](
      gameState.round.find(_.buyerKey == playerKey).map(_.buyerKey),
      Failure("Player is not buyer", "Another player is already the buyer", 404).asFailedAttempt
    )
  }

  def playerHasRole(playerState: PlayerState, role: Role): Attempt[Role] = {
    EitherT.fromOption[IO](playerState.role.find(_ == role),
      Failure("Player does not have this role to give", "Cannot award point for a role that isn't yours", 400).asFailedAttempt
    )
  }

  def playerSummaries(states: Map[PlayerKey, PlayerState]): Map[PlayerKey, PlayerSummary] = {
    states.map { case (playerKey, playerState) =>
        playerKey -> PlayerSummary(playerState.screenName, Nil)
    }
  }

  def lookupPlayer(states: Map[PlayerKey, PlayerState], playerKey: PlayerKey): Attempt[PlayerState] = {
    EitherT.fromOption[IO](states.get(playerKey),
      Failure("Couldn't lookup creator's state", "Couldn't find the player", 500, None).asFailedAttempt
    )
  }

  def lookupPlayerByName(states: Map[PlayerKey, PlayerState], screenName: String): Attempt[(PlayerKey, PlayerState)] = {
    EitherT.fromOption[IO](states.find(_._2.screenName == screenName),
      Failure("Couldn't lookup player by name", s"Couldn't find a player called '$screenName'", 500, None).asFailedAttempt
    )
  }

  def startGameState(gameState: GameState, playerNames: Map[PlayerKey, PlayerSummary]): GameState = {
    gameState.copy(
      started = true,
      players = playerNames
    )
  }

  def verifyNoBuyer(gameState: GameState): Attempt[Unit] = {
    gameState.round match {
      case None =>
        EitherT.pure(())
      case Some(buyer) =>
        val playerName = gameState.players.view.mapValues(_.screenName).toMap.getOrElse(buyer.buyerKey, "another player")
        EitherT.leftT(Failure(s"Buyer already exists: $playerName", s"$playerName is already the buyer", 400).asFailedAttempt)
    }
  }

  def verifyNotStarted(gameState: GameState): Attempt[Unit] = {
    if (gameState.started) {
      EitherT.leftT(Failure("Game has already started", "The game has already started", 400).asFailedAttempt)
    } else {
      EitherT.pure(())
    }
  }

  def validatePlayerCount(playerCount: Int): Attempt[Unit] = {
    if (playerCount < 2) {
      EitherT.leftT(
        Failure(
          "Insufficient player count",
          "Quack Stanley requires at least 3 players to play properly. Make sure you wait for other players to join before starting the game.",
          400
        ).asFailedAttempt
      )
    } else {
      EitherT.pure(())
    }
  }

  def verifyUniqueScreenName(screenName: String, playerStates: Map[PlayerKey, PlayerState]): Attempt[Unit] = {
    playerStates.find { case (_, playerState) =>
      playerState.screenName == screenName
    }.fold(EitherT.pure[IO, FailedAttempt](())) { _ =>
      EitherT.leftT {
        Failure(
          "duplicate screen name",
          s"The name $screenName is already in use, please choose a different name.",
          409
        ).asFailedAttempt
      }
    }
  }

  def updateGameWithPitch(gameState: GameState, pitcher: PlayerKey, words: (Word, Word)): Attempt[GameState] = {
    EitherT.fromOption[IO](
      gameState.round.map { round =>
        val updatedProducts = round.products + (pitcher -> words)
        val roundWithPitch = round.copy(products = updatedProducts)
        gameState.copy(
          round = Some(roundWithPitch)
        )
      },
      FailedAttempt(Failure("Cannot add pitch to game, no round in progress", "There's no buyer to pitch to", 500))
    )
  }

  def updateGameWithAwardedPoint(gameState: GameState, winner: PlayerKey, role: Role): Attempt[GameState] = {
    for {
      winnerSummary <- EitherT.fromOption[IO](gameState.players.get(winner),
        FailedAttempt(Failure("Could not find winner in game state's players", "Could not find player", 404))
      )
      updatedWinnerSummary = winnerSummary.copy(points = winnerSummary.points :+ role)
      modifiedPlayers = gameState.players.updated(winner, updatedWinnerSummary)
    } yield gameState.copy(
      round = None,
      players = modifiedPlayers
    )
  }

  def usedWords(states: List[PlayerState]): Set[Word] = {
    states.flatMap(state => state.hand ++ state.discardedWords).toSet
  }

  def nextWords(n: Int, words: List[Word], used: Set[Word]): Attempt[List[Word]] = {
    val next = shuffle(words).filterNot(used.contains).take(n)
    if (next.size < n) {
      EitherT.leftT {
        Failure("Exhausted available words", "Ran out of words", 500).asFailedAttempt
      }
    } else {
      EitherT.pure(next)
    }
  }

  def usedRoles(states: List[PlayerState]): Set[Role] = {
    states.flatMap(state => state.points ++ state.role).toSet
  }

  def nextRole(roles: List[Role], used: Set[Role]): Attempt[Role] = {
    EitherT.fromOption[IO](shuffle(roles).filterNot(used.contains).headOption,
      Failure("Exhausted available roles", "Ran out of roles", 500).asFailedAttempt
    )
  }

  def dealWordsToAllPlayers(words: List[Word], players: Map[PlayerKey, PlayerState]): Attempt[Map[PlayerKey, PlayerState]] = {
    if (words.size < players.size * QuackStanley.handSize) {
      EitherT.leftT(
        Failure("dealWords wasn't given enough words for the players", "Failed to get words for all players", 500).asFailedAttempt
      )
    } else {
      EitherT.pure {
        val (_, dealtPlayers) = players.foldLeft[(List[Word], Map[PlayerKey, PlayerState])]((words, Map.empty)) {
          case ((remainingWords, acc), (playerKey, playerState)) =>
            (
              remainingWords.drop(QuackStanley.handSize),
              acc + (playerKey -> playerState.copy(hand = remainingWords.take(QuackStanley.handSize)))
            )
        }
        dealtPlayers
      }
    }
  }

  def fillHand(words: List[Word], playerState: PlayerState): Attempt[PlayerState] = {
    if (words.size < (QuackStanley.handSize - playerState.hand.size)) {
      EitherT.leftT(
        Failure("Not enough words provided to fill player hand", "Ran out of words", 500).asFailedAttempt
      )
    } else {
      EitherT.pure(
        playerState.copy(hand = playerState.hand ++ words)
      )
    }
  }

  def discardWords(words: (Word, Word), playerState: PlayerState): Attempt[PlayerState] = {
    val failures = List(words._1, words._2).flatMap { word =>
      if (playerState.hand.contains(word)) None
      else Some(Failure("Player cannot discard word not in their hand", s"Cannot discard words that aren't in your hand ($word)", 400, Some(word.value)))
    }

    if (failures.isEmpty) {
      EitherT.pure(
        playerState.copy(
          hand = playerState.hand.filterNot { word =>
            word == words._1 || word == words._2
          },
          discardedWords = playerState.discardedWords :+ words._1 :+ words._2
        )
      )
    } else {
      EitherT.leftT(FailedAttempt(failures))
    }
  }

  def dealRole(role: Role, player: PlayerState): PlayerState = {
    player.copy(role = Some(role))
  }

  def addRoleToPoints(playerState: PlayerState, point: Role): PlayerState = {
    playerState.copy(points = playerState.points :+ point)
  }

  def makeUniquePrefix(gameId: GameId, persistence: Persistence,
                       fn: (GameId, Int, Persistence) => Attempt[Boolean]
                      ): Attempt[String] = {
    val min = 4
    val max = 10
    def loop(prefixLength: Int): Attempt[String] = {
      fn(gameId, prefixLength, persistence).flatMap {
        case true =>
          EitherT.pure(gameId.value.take(prefixLength))
        case false if prefixLength < max =>
          loop(prefixLength + 1)
        case _ =>
          EitherT.leftT(
            Failure("Couldn't create unique prefix of GameID", "Couldn't set up game with a join code", 500).asFailedAttempt
          )
      }
    }
    loop(min)
  }

  def gameIdFromPrefixResults(gameCode: String, results: List[String]): Attempt[GameId] = {
    val PrefixedMatch = (".*/(" ++ gameCode ++ "[a-z0-9\\-]*)/game.json$").r
    val matches = results.flatMap {
      case PrefixedMatch(gameId) => Some(GameId(gameId))
      case _ => None
    }
    matches match {
      case Nil => EitherT.leftT(
        Failure("Couldn't find game from gameCode", "Couldn't find a game with that code", 404, Some(gameCode)).asFailedAttempt
      )
      case result :: Nil => EitherT.pure(result)
      case _ => EitherT.leftT(
        Failure("Multiple games matched gameCode", "Couldn't find a game to add you to, invalid code", 404, Some(gameCode)).asFailedAttempt
      )
    }
  }
}
