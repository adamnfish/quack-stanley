package com.adamnfish.quackstanley.integration

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.persistence.GameIO
import com.adamnfish.quackstanley.{AttemptValues, Config, QuackStanley, TestPersistence}
import org.joda.time.DateTime
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}

import scala.concurrent.ExecutionContext.Implicits.global


class StartGameIntegrationTest extends FreeSpec with Matchers with OneInstancePerTest with AttemptValues {
  val persistence = new TestPersistence
  val testConfig = Config("test", "test", persistence)

  "startGame" - {
    "if the game exists" - {
      val gameId = GameId("test-game")
      val gameName = "game-name"
      val creatorScreenName = "creator"
      val creatorKey = PlayerKey("creator")
      val creatorState = PlayerState(gameId, gameName, creatorScreenName, Nil, Nil, None, Nil)
      val playerScreenName = "player"
      val playerKey = PlayerKey("player")
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
      }

      "and this player is not registered, fails to auth player" in {
        val request = StartGame(gameId, PlayerKey("does not exist"))
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
      val request = StartGame(GameId("does-not-exist"), PlayerKey("no-player"))
      startGame(request, testConfig).isFailedAttempt() shouldEqual true
    }

  }
}
