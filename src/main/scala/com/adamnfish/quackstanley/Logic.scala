package com.adamnfish.quackstanley

import java.util.UUID

import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
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

  def nextRoles(n: Int, roles: List[Role], used: Set[Role]): Attempt[List[Role]] = {
    val next = shuffle(roles).filterNot(used.contains).take(n)
    if (next.size < n) {
      Attempt.Left {
        Failure("Exhausted available roles", "Ran out of roles", 500)
      }
    } else {
      Attempt.Right(next)
    }
  }

  def dealWords(words: List[Word], players: Map[PlayerKey, PlayerState]): Attempt[Map[PlayerKey, PlayerState]] = {
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
}
