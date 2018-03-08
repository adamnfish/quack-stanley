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
      val gameCode = gameId.value
      val gameState = GameState(gameId, "game-name", DateTime.now(), false, creator, None, Map(creator -> "Creator"))
      GameIO.writeGameState(gameState, testConfig)

      "uses provided screen name" in {
        val request = RegisterPlayer(gameCode, "player one")
        val registered = registerPlayer(request, testConfig).value()
        registered.state.screenName shouldEqual "player one"
      }

      "new player has no role, words or points" in {
        val request = RegisterPlayer(gameCode, "player one")
        val registered = registerPlayer(request, testConfig).value()
        registered.state should have(
          'role (None),
          'hand (Nil),
          'discardedWords (Nil),
          'points (Nil)
        )
      }

      "can register player from prefix game code" in {
        val request = RegisterPlayer(gameCode.take(4), "player one")
        registerPlayer(request, testConfig).isSuccessfulAttempt() shouldBe true
      }

      "prefix game code is case-insensitive" in {
        val request = RegisterPlayer(gameCode.take(4).toUpperCase(), "player one")
        registerPlayer(request, testConfig).isSuccessfulAttempt() shouldBe true
      }

      "correctly persists player state" in {
        val request = RegisterPlayer(gameCode, "player one")
        val registered = registerPlayer(request, testConfig).value()
        val savedState = GameIO.getPlayerState(registered.playerKey, gameId, testConfig).value()
        registered.state shouldEqual savedState
      }

      "does not allow duplicate screen name" ignore {}

      "validates user input," - {
        "flags empty game code" in {
          val request = RegisterPlayer("", "screen name")
          val failure = registerPlayer(request, testConfig).leftValue()
          failure.failures.head.context.value shouldEqual "game code"
        }

        "checks GameID is the correct format" in {
          val request = RegisterPlayer("not a UUID prefix", "screen name")
          val failure = registerPlayer(request, testConfig).leftValue()
          failure.failures.head.context.value shouldEqual "game code"
        }

        "flags empty screen name" in {
          val request = RegisterPlayer(gameCode, "")
          val failure = registerPlayer(request, testConfig).leftValue()
          failure.failures.head.context.value shouldEqual "screen name"
        }

        "fails if game code is too short" in {
          val request = RegisterPlayer("123", "")
          val failure = registerPlayer(request, testConfig).leftValue()
          failure.failures should not be empty
        }

        "gives all errors if everything fails" in {
          val request = RegisterPlayer("", "")
          val failure = registerPlayer(request, testConfig).leftValue()
          failure.failures should have length 3
        }
      }

      "fails if the game has already started" in {
        val startedState = gameState.copy(started = true)
        GameIO.writeGameState(startedState, testConfig).value()
        val request = RegisterPlayer(gameCode, "player two")
        registerPlayer(request, testConfig).isFailedAttempt() shouldEqual true
      }
    }

    "if the game doe not exist," - {
      "fails" in {
        val request = RegisterPlayer(GameId(gameDoesNotExistIdUUID).value, "player name")
        registerPlayer(request, testConfig).isFailedAttempt() shouldEqual true
      }
    }
  }
}
