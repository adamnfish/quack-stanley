package com.adamnfish.quackstanley

import java.util.UUID

import com.adamnfish.quackstanley.attempt.{Attempt, FailedAttempt, Failure}
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.persistence.GameIO
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext
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
    PlayerInfo(playerState, gameState.started, otherPlayers.values.toList)
  }

  def authenticate(playerKey: PlayerKey, gameState: GameState)(implicit ec: ExecutionContext): Attempt[PlayerSummary] = {
    Attempt.fromOption(gameState.players.get(playerKey),
      Failure("Player key not found in game state", "Invalid player", 404).asAttempt
    )
  }

  def authenticateCreator(playerKey: PlayerKey, gameState: GameState)(implicit ec: ExecutionContext): Attempt[PlayerKey] = {
    if (gameState.creator == playerKey) {
      Attempt.Right(gameState.creator)
    } else {
      Attempt.Left {
        Failure("Player key not found in game state", "Invalid player", 404).asAttempt
      }
    }
  }

  def authenticateBuyer(playerKey: PlayerKey, gameState: GameState)(implicit ec: ExecutionContext): Attempt[PlayerKey] = {
    Attempt.fromOption(
      gameState.buyer.flatMap { buyerKey =>
        gameState.buyer.find(_ == playerKey)
      },
      Failure("Player is not buyer", "Another player is already buyer", 404).asAttempt
    )
  }

  def playerHasRole(playerState: PlayerState, role: Role): Attempt[Role] = {
    Attempt.fromOption(playerState.role.find(_ == role),
      Failure("Player does not have this role to give", "Attempting to award a point for incorrect role", 400).asAttempt
    )
  }

  def playerSummaries(states: Map[PlayerKey, PlayerState]): Map[PlayerKey, PlayerSummary] = {
    states.map { case (playerKey, playerState) =>
        playerKey -> PlayerSummary(playerState.screenName, Nil)
    }
  }

  def lookupPlayer(states: Map[PlayerKey, PlayerState], playerKey: PlayerKey): Attempt[PlayerState] = {
    Attempt.fromOption(states.get(playerKey),
      Failure("Couldn't lookup creator's state", "Failed to lookup player", 500, None).asAttempt
    )
  }

  def lookupPlayerByName(states: Map[PlayerKey, PlayerState], screenName: String): Attempt[(PlayerKey, PlayerState)] = {
    Attempt.fromOption(states.find(_._2.screenName == screenName),
      Failure("Couldn't lookup player by name", s"Failed to lookup player with name '$screenName'", 500, None).asAttempt
    )
  }

  def startGameState(gameState: GameState, playerNames: Map[PlayerKey, PlayerSummary]): GameState = {
    gameState.copy(
      started = true,
      players = playerNames
    )
  }

  def verifyNoBuyer(gameState: GameState): Attempt[Unit] = {
    gameState.buyer match {
      case None =>
        Attempt.Right(())
      case Some(buyerKey) =>
        val playerName = gameState.players.getOrElse(buyerKey, "another player")
        Attempt.Left(Failure(s"Buyer already exists: $playerName", s"$playerName is already the buyer", 400))
    }
  }

  def verifyNotStarted(gameState: GameState): Attempt[Unit] = {
    if (gameState.started) {
      Attempt.Left(Failure("Game has already started", "The game has already started", 400))
    } else {
      Attempt.Right(())
    }
  }

  def usedWords(states: List[PlayerState]): Set[Word] = {
    states.flatMap(state => state.hand ++ state.discardedWords).toSet
  }

  def nextWords(n: Int, words: List[Word], used: Set[Word]): Attempt[List[Word]] = {
    val next = shuffle(words).filterNot(used.contains).take(n)
    if (next.size < n) {
      Attempt.Left {
        Failure("Exhausted available words", "Ran out of words", 500).asAttempt
      }
    } else {
      Attempt.Right(next)
    }
  }

  def usedRoles(states: List[PlayerState]): Set[Role] = {
    states.flatMap(state => state.points ++ state.role).toSet
  }

  def nextRole(roles: List[Role], used: Set[Role]): Attempt[Role] = {
    Attempt.fromOption(shuffle(roles).filterNot(used.contains).headOption,
      Failure("Exhausted available roles", "Ran out of roles", 500).asAttempt
    )
  }

  def dealWordsToAllPlayers(words: List[Word], players: Map[PlayerKey, PlayerState]): Attempt[Map[PlayerKey, PlayerState]] = {
    if (words.size < players.size * QuackStanley.handSize) {
      Attempt.Left(
        Failure("dealWords wasn't given enough words for the players", "Failed to get words for all players", 500)
      )
    } else {
      Attempt.Right {
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
      Attempt.Left(
        Failure("Not enough words provided to fill player hand", "Ran out of words", 500)
      )
    } else {
      Attempt.Right(
        playerState.copy(hand = playerState.hand ++ words)
      )
    }
  }

  def discardWords(words: (Word, Word), playerState: PlayerState): Attempt[PlayerState] = {
    val failures = List(words._1, words._2).flatMap { word =>
      if (playerState.hand.contains(word)) None
      else Some(Failure("Player cannot discard word not in their hand", s"Cannot discard words that aren;t in your hand ($word)", 400, Some(word.value)))
    }

    if (failures.isEmpty) {
      Attempt.Right(
        playerState.copy(
          hand = playerState.hand.filterNot { word =>
            word == words._1 || word == words._2
          },
          discardedWords = playerState.discardedWords :+ words._1 :+ words._2
        )
      )
    } else {
      Attempt.Left(FailedAttempt(failures))
    }
  }

  def dealRole(role: Role, player: PlayerState): PlayerState = {
    player.copy(role = Some(role))
  }

  def addRoleToPoints(playerState: PlayerState, point: Role): PlayerState = {
    playerState.copy(points = playerState.points :+ point)
  }

  def makeUniquePrefix(gameId: GameId, config: Config,
                       fn: (GameId, Int, Config) => Attempt[Boolean]
                      )
                      (implicit ec: ExecutionContext): Attempt[String] = {
    val min = 4
    val max = 10
    def loop(prefixLength: Int): Attempt[String] = {
      fn(gameId, prefixLength, config).flatMap {
        case true =>
          Attempt.Right(gameId.value.take(prefixLength))
        case false if prefixLength < max =>
          loop(prefixLength + 1)
        case _ =>
          Attempt.Left(
            Failure("Couldn't create unique prefix of GameID", "Couldn't create game password", 500)
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
      case Nil => Attempt.Left(
        Failure("Couldn't find game from gameCode", "No matching games found", 404, Some(gameCode))
      )
      case result :: Nil => Attempt.Right(result)
      case _ => Attempt.Left(
        Failure("Multiple games matched gameCode", "Couldn't add you to a game, invalid code", 404, Some(gameCode))
      )
    }
  }
}
