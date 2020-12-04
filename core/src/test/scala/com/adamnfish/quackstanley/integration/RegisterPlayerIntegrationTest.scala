package com.adamnfish.quackstanley.integration

import java.util.UUID

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.persistence.GameIO
import com.adamnfish.quackstanley.{AttemptValues, HaveMatchers, TestPersistence}
import org.joda.time.DateTime
import org.scalatest.{OneInstancePerTest, OptionValues}
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AnyFreeSpec

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


class RegisterPlayerIntegrationTest extends AnyFreeSpec with Matchers
  with OneInstancePerTest with AttemptValues with OptionValues with HaveMatchers {

  val persistence = new TestPersistence
  val testConfig = Config("test", persistence)

  val hostUUID = UUID.randomUUID().toString
  val gameIdUUID = UUID.randomUUID().toString
  val gameDoesNotExistIdUUID = UUID.randomUUID().toString
  assert(
    Set(
      hostUUID, gameIdUUID, gameDoesNotExistIdUUID
    ).size == 3,
    "Ensuring random UUID test data is distinct"
  )

  "registerPlayer" - {
    "if the game exists" - {
      val host = PlayerKey(hostUUID)
      val gameId = GameId(gameIdUUID)
      val gameCode = gameId.value.take(4)
      val gameState = GameState(gameId, "game-name", DateTime.now(), false, host, None,
        Map(host -> PlayerSummary("Host", Nil))
      )
      val hostState = PlayerState(gameState.gameId, gameState.gameName, "Host", Nil, Nil, None, Nil)
      GameIO.writePlayerState(hostState, host, persistence)
      GameIO.writeGameState(gameState, persistence)

      "uses provided screen name" in {
        val request = RegisterPlayer(gameCode, "player one")
        val registered = registerPlayer(request, testConfig).value()
        registered.state.screenName shouldEqual "player one"
      }

      "new player has no role, words or points" in {
        val request = RegisterPlayer(gameCode, "player one")
        val registered = registerPlayer(request, testConfig).value()
        registered.state should have(
          "role" as (None),
          "hand" as (Nil),
          "discardedWords" as (Nil),
          "points" as (Nil)
        )
      }

      "can register player from a longer game code prefix" in {
        val request = RegisterPlayer(gameId.value.take(6), "player one")
        registerPlayer(request, testConfig).isSuccessfulAttempt() shouldBe true
      }

      "prefix game code is case-insensitive" in {
        val request = RegisterPlayer(gameCode.toUpperCase(), "player one")
        registerPlayer(request, testConfig).isSuccessfulAttempt() shouldBe true
      }

      "correctly persists player state" in {
        val request = RegisterPlayer(gameCode, "player one")
        val registered = registerPlayer(request, testConfig).value()
        val savedState = GameIO.getPlayerState(registered.playerKey, gameId, persistence).value()
        registered.state shouldEqual savedState
      }

      "does not allow duplicate screen names" - {
        "player cannot use the host's screen name" in {
          val request = RegisterPlayer(gameCode, "Host")
          registerPlayer(request, testConfig).isFailedAttempt() shouldBe true
        }

        "player cannot use another player's screen name" in {
          val firstRequest = RegisterPlayer(gameCode.toUpperCase(), "player one")
          registerPlayer(firstRequest, testConfig).isSuccessfulAttempt() shouldBe true
          val duplicateRequest = firstRequest
          registerPlayer(duplicateRequest, testConfig).isFailedAttempt() shouldBe true
        }
      }

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
          failure.failures should have length 2
        }
      }

      "fails if the game has already started" in {
        val startedState = gameState.copy(started = true)
        GameIO.writeGameState(startedState, persistence).value()
        val request = RegisterPlayer(gameCode, "player two")
        registerPlayer(request, testConfig).isFailedAttempt() shouldEqual true
      }
    }

    "if the game does not exist," - {
      "fails" in {
        val request = RegisterPlayer(GameId(gameDoesNotExistIdUUID).value, "player name")
        registerPlayer(request, testConfig).isFailedAttempt() shouldEqual true
      }
    }
  }
}
