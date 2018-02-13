package com.adamnfish.quackstanley.integration

import java.util.UUID

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.persistence.GameIO
import com.adamnfish.quackstanley.{AttemptValues, Config, TestPersistence}
import org.joda.time.DateTime
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest, OptionValues}

import scala.concurrent.ExecutionContext.Implicits.global


class RegisterPlayerIntegrationTest extends FreeSpec with Matchers
  with OneInstancePerTest with AttemptValues with OptionValues {

  val persistence = new TestPersistence
  val testConfig = Config("test", "test", persistence)

  val creatorUUID = UUID.randomUUID().toString
  val gameIdUUID = UUID.randomUUID().toString
  val gameDoesNotExistIdUUID = UUID.randomUUID().toString
  assert(
    Set(
      creatorUUID, gameIdUUID, gameDoesNotExistIdUUID
    ).size == 3,
    "Ensuring random UUID test data is distinct"
  )

  "registerPlayer" - {
    "if the game exists" - {
      val creator = PlayerKey(creatorUUID)
      val gameId = GameId(gameIdUUID)
      val gameState = GameState(gameId, "game-name", DateTime.now(), false, creator, None, Map(creator -> "Creator"))
      GameIO.writeGameState(gameState, testConfig)

      "uses provided screen name" in {
        val request = RegisterPlayer(gameId, "player one")
        val registered = registerPlayer(request, testConfig).value()
        registered.state.screenName shouldEqual "player one"
      }

      "new player has no role, words or points" in {
        val request = RegisterPlayer(gameId, "player one")
        val registered = registerPlayer(request, testConfig).value()
        registered.state should have(
          'role (None),
          'hand (Nil),
          'discardedWords (Nil),
          'points (Nil)
        )
      }

      "correctly persists player state" in {
        val request = RegisterPlayer(gameId, "player one")
        val registered = registerPlayer(request, testConfig).value()
        val savedState = GameIO.getPlayerState(registered.playerKey, gameId, testConfig).value()
        registered.state shouldEqual savedState
      }

      "does not allow duplicate screen name" ignore {}

      "validates user input," - {
        "flags empty game id" in {
          val request = RegisterPlayer(GameId(""), "screen name")
          val failure = registerPlayer(request, testConfig).leftValue()
          failure.failures.head.context.value shouldEqual "game ID"
        }

        "checks GameID is the correct format" in {
          val request = RegisterPlayer(GameId("not a UUID"), "screen name")
          val failure = registerPlayer(request, testConfig).leftValue()
          failure.failures.head.context.value shouldEqual "game ID"
        }

        "flags empty screen name" in {
          val request = RegisterPlayer(gameId, "")
          val failure = registerPlayer(request, testConfig).leftValue()
          failure.failures.head.context.value shouldEqual "screen name"
        }

        "gives both errors if both are missing" in {
          val request = RegisterPlayer(GameId(""), "")
          val failure = registerPlayer(request, testConfig).leftValue()
          failure.failures should have length 2
        }
      }

      "fails if the game has already started" in {
        val startedState = gameState.copy(started = true)
        GameIO.writeGameState(startedState, testConfig).value()
        val request = RegisterPlayer(gameId, "player two")
        registerPlayer(request, testConfig).isFailedAttempt() shouldEqual true
      }
    }

    "if the game doe not exist," - {
      "fails" in {
        val request = RegisterPlayer(GameId(gameDoesNotExistIdUUID), "player name")
        registerPlayer(request, testConfig).isFailedAttempt() shouldEqual true
      }
    }
  }
}
