package com.adamnfish.quackstanley.integration

import java.util.UUID

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.persistence.GameIO
import com.adamnfish.quackstanley.{AttemptValues, HaveMatchers, Logic, TestPersistence}
import org.joda.time.DateTime
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{OneInstancePerTest, OptionValues}

import scala.concurrent.ExecutionContext.Implicits.global


class RegisterHostIntegrationTest extends AnyFreeSpec with Matchers
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
      val hostCode = Logic.hostCodeFromKey(host)
      val gameState = GameState(gameId, "game-name", DateTime.now(), false, host, None, Map.empty)
      GameIO.writeGameState(gameState, persistence)

      "uses provided screen name" in {
        val request = RegisterHost(gameCode, hostCode, "host")
        val registered = registerHost(request, testConfig).value()
        registered.state.screenName shouldEqual "host"
      }

      "new host has no role, words or points" in {
        val request = RegisterHost(gameCode, hostCode, "host")
        val registered = registerHost(request, testConfig).value()
        registered.state should have(
          "role" as (None),
          "hand" as (Nil),
          "discardedWords" as (Nil),
          "points" as (Nil)
        )
      }

      "can register host from a longer game code" in {
        val request = RegisterHost(gameId.value.take(7), hostCode, "host")
        registerHost(request, testConfig).isSuccessfulAttempt() shouldBe true
      }

      "game code is case-insensitive" in {
        val request = RegisterHost(gameCode.toUpperCase(), hostCode, "host")
        registerHost(request, testConfig).isSuccessfulAttempt() shouldBe true
      }

      "host code is case-insensitive" in {
        val request = RegisterHost(gameCode, hostCode.toUpperCase(), "host")
        registerHost(request, testConfig).isSuccessfulAttempt() shouldBe true
      }

      "correctly persists host's state" in {
        val request = RegisterHost(gameCode, hostCode, "host")
        val registered = registerHost(request, testConfig).value()
        val savedState = GameIO.getPlayerState(registered.playerKey, gameId, persistence).value()
        registered.state shouldEqual savedState
      }

      "fails if the host code does not match" in {
        assert(hostCode != "aaaa", "test requires random hostCode != aaaa")
        val request = RegisterHost(gameCode, "aaaa", "host")
        registerHost(request, testConfig).isFailedAttempt() shouldBe true
      }

      "host cannot use same screen name as another player that has already joined" in {
        val firstRequest = RegisterPlayer(gameCode.toUpperCase(), "host")
        registerPlayer(firstRequest, testConfig).isSuccessfulAttempt() shouldBe true
        val hostRequest = RegisterHost(gameCode.toUpperCase(), hostCode, firstRequest.screenName)
        registerHost(hostRequest, testConfig).isFailedAttempt() shouldBe true
      }

      "fails if the game has already started" in {
        val startedState = gameState.copy(started = true)
        GameIO.writeGameState(startedState, persistence).value()
        val request = RegisterHost(gameCode, hostCode, "host")
        registerHost(request, testConfig).isFailedAttempt() shouldEqual true
      }

      "validates user input," - {
        "flags empty game code" in {
          val request = RegisterHost("", "abcd", "screen name")
          val failure = registerHost(request, testConfig).leftValue()
          failure.failures.head.context.value shouldEqual "game code"
        }

        "flags empty host code" in {
          val request = RegisterHost(gameCode, "", "screen name")
          val failure = registerHost(request, testConfig).leftValue()
          failure.failures.head.context.value shouldEqual "host code"
        }

        "checks game code is the correct format" in {
          val request = RegisterHost("not a UUID prefix", hostCode, "screen name")
          val failure = registerHost(request, testConfig).leftValue()
          failure.failures.head.context.value shouldEqual "game code"
        }

        "checks host code is the correct format" in {
          val request = RegisterHost(gameCode, "not a UUID prefix", "screen name")
          val failure = registerHost(request, testConfig).leftValue()
          failure.failures.head.context.value shouldEqual "host code"
        }

        "flags empty screen name" in {
          val request = RegisterHost(gameCode, hostCode, "")
          val failure = registerHost(request, testConfig).leftValue()
          failure.failures.head.context.value shouldEqual "screen name"
        }

        "fails if game code is too short" in {
          val request = RegisterHost("123", hostCode, "")
          val failure = registerHost(request, testConfig).leftValue()
          failure.failures should not be empty
        }

        "fails if host code is too short" in {
          val request = RegisterHost(gameCode, "abc", "")
          val failure = registerHost(request, testConfig).leftValue()
          failure.failures should not be empty
        }

        "gives all errors if everything fails" in {
          val request = RegisterHost("", "", "")
          val failure = registerHost(request, testConfig).leftValue()
          failure.failures should have length 3
        }
      }
    }

    "if the game does not exist," - {
      "fails" in {
        val request = RegisterHost(GameId(gameDoesNotExistIdUUID).value, "abcd", "host")
        registerHost(request, testConfig).isFailedAttempt() shouldEqual true
      }
    }
  }
}
