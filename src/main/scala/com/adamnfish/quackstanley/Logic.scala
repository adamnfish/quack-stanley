package com.adamnfish.quackstanley

import java.util.UUID

import com.adamnfish.quackstanley.attempt.{Attempt, FailedAttempt, Failure}
import com.adamnfish.quackstanley.models._
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

  def authenticateCreator(playerKey: PlayerKey, gameState: GameState)(implicit ec: ExecutionContext): Attempt[PlayerKey] = {
    if (gameState.creator == playerKey) {
      Attempt.Right(gameState.creator)
    } else {
      Attempt.Left {
        Failure("Player key not found in game state", "Invalid player", 404).asAttempt
      }
    }
  }

  def makePlayerNames(states: Map[PlayerKey, PlayerState]): Map[PlayerKey, String] = {
    states.map { case (playerKey, playerState) =>
        playerKey -> playerState.screenName
    }
  }

  def lookupPlayer(states: Map[PlayerKey, PlayerState], playerKey: PlayerKey): Attempt[PlayerState] = {
    Attempt.fromOption(states.get(playerKey),
      Failure("Couldn't lookup creator's state", "Failed to lookup player", 500, None).asAttempt
    )
  }

  def startGameState(gameState: GameState, playerNames: Map[PlayerKey, String]): GameState = {
    gameState.copy(
      started = true,
      players = playerNames
    )
  }

  def verifyNoBuyer(gameState: GameState): Attempt[Unit] = {
    if (gameState.buyer.isDefined) {
      Attempt.Left(Failure("Buyer already exists", "This game already has a buyer", 400))
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
        words.grouped(QuackStanley.handSize)
        val (_, dealtPlayers) = players.foldLeft[(List[Word], Map[PlayerKey, PlayerState])]((Nil, Map.empty)) {
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
        playerState.copy(hand = playerState.hand.filterNot { word =>
          word == words._1 || word == words._2
        })
      )
    } else {
      Attempt.Left(FailedAttempt(failures))
    }
  }

  def dealRole(role: Role, player: PlayerState): PlayerState = {
    player.copy(role = Some(role))
  }
}
