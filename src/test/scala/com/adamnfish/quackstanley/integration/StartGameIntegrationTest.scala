package com.adamnfish.quackstanley.integration

import java.util.UUID

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.persistence.GameIO
import com.adamnfish.quackstanley.{AttemptValues, Config, QuackStanley, TestPersistence}
import org.joda.time.DateTime
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest, OptionValues}

import scala.concurrent.ExecutionContext.Implicits.global


class StartGameIntegrationTest extends FreeSpec with Matchers
  with OneInstancePerTest with AttemptValues with OptionValues {

  val persistence = new TestPersistence
  val testConfig = Config("test", "test", persistence)

  val creatorUUID = UUID.randomUUID().toString
  val gameIdUUID = UUID.randomUUID().toString
  val gameDoesNotExistIdUUID = UUID.randomUUID().toString
  val playerKeyUUID = UUID.randomUUID().toString
  val playerDoesNotExistUUID = UUID.randomUUID().toString
  assert(
    Set(
      creatorUUID, gameIdUUID, gameDoesNotExistIdUUID, playerKeyUUID, playerDoesNotExistUUID
    ).size == 5,
    "Ensuring random UUID test data is distinct"
  )

  "startGame" - {
    "if the game exists" - {
      val gameId = GameId(gameIdUUID)
      val gameName = "game-name"
      val creatorScreenName = "creator"
      val creatorKey = PlayerKey(creatorUUID)
      val creatorState = PlayerState(gameId, gameName, creatorScreenName, Nil, Nil, None, Nil)
      val playerScreenName = "player"
      val playerKey = PlayerKey(playerKeyUUID)
      val playerState = PlayerState(gameId, gameName, playerScreenName, Nil, Nil, None, Nil)
      val gameState = GameState(gameId, gameName, DateTime.now(), started = false, creatorKey, None,
        Map(creatorKey -> creatorScreenName, playerKey -> playerScreenName)
      )
      GameIO.writeGameState(gameState, testConfig).value()
      GameIO.writePlayerState(creatorState, creatorKey, testConfig).value()
      GameIO.writePlayerState(playerState, playerKey, testConfig).value()

      "and this player is the creator" - {
        "succeeds" in {
          val request = StartGame(gameId, creatorKey)
          startGame(request, testConfig).isSuccessfulAttempt() shouldEqual true
        }

        "writes all players into players game state" in {
          val request = StartGame(gameId, creatorKey)
          startGame(request, testConfig)
          val savedState = GameIO.getGameState(gameId, testConfig).value()
          savedState.players shouldEqual Map(
            creatorKey -> creatorScreenName,
            playerKey -> playerScreenName
          )
        }

        "returns other players in playerInfo" in {
          val request = StartGame(gameId, creatorKey)
          val playerInfo = startGame(request, testConfig).value()
          playerInfo.otherPlayers.toSet shouldEqual Set(creatorScreenName, playerScreenName)
        }

        "sets started to true" in {
          val request = StartGame(gameId, creatorKey)
          val playerInfo = startGame(request, testConfig).value()
          playerInfo.started shouldEqual true
        }

        "persists started setting" in {
          val request = StartGame(gameId, creatorKey)
          startGame(request, testConfig).value()
          val savedState = GameIO.getGameState(gameId, testConfig).value()
          savedState.started shouldEqual true
        }

        "populates (and persists) each player's hand" in {
          val request = StartGame(gameId, creatorKey)
          startGame(request, testConfig).value()
          val savedCreatorState = GameIO.getPlayerState(creatorKey, gameId, testConfig).value()
          val savedPlayerState = GameIO.getPlayerState(playerKey, gameId, testConfig).value()
          savedCreatorState.hand.size shouldEqual QuackStanley.handSize
          savedPlayerState.hand.size shouldEqual QuackStanley.handSize
        }

        "validates user input," - {
          "flags empty game id" in {
            val request = StartGame(GameId(""), creatorKey)
            val failure = startGame(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "game ID"
          }

          "ensures GameID is the correct format" in {
            val request = StartGame(GameId("not uuid"), creatorKey)
            val failure = startGame(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "game ID"
          }

          "flags empty player key" in {
            val request = StartGame(gameId, PlayerKey(""))
            val failure = startGame(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "player key"
          }

          "ensures player key is the correct format" in {
            val request = StartGame(gameId, PlayerKey("not uuid"))
            val failure = startGame(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "player key"
          }

          "gives all errors if multiple fields fail validation" in {
            val request = StartGame(GameId(""), PlayerKey(""))
            val failure = startGame(request, testConfig).leftValue()
            failure.failures should have length 2
          }
        }
      }

      "and this player is not registered, fails to auth player" in {
        val request = StartGame(gameId, PlayerKey(playerDoesNotExistUUID))
        startGame(request, testConfig).isFailedAttempt() shouldEqual true
      }

      "if the player is not the creator, fails to perform the action" in {
        val request = StartGame(gameId, playerKey)
        startGame(request, testConfig).isFailedAttempt() shouldEqual true
      }

      "fails if the game has already started" in {
        val startedState = gameState.copy(started = true)
        GameIO.writeGameState(startedState, testConfig).value()
        val request = StartGame(gameId, creatorKey)
        startGame(request, testConfig).isFailedAttempt() shouldEqual true
      }
    }

    "if the game does not exist, fails to auth the player" in {
      val request = StartGame(GameId(gameDoesNotExistIdUUID), PlayerKey(playerDoesNotExistUUID))
      startGame(request, testConfig).isFailedAttempt() shouldEqual true
    }
  }
}
