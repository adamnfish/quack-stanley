package com.adamnfish.quackstanley.integration

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

  "registerPlayer" - {
    "if the game exists" - {
      val creator = PlayerKey("creator")
      val gameId = GameId("test-game")
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

      "handles failures nicely," - {
        "flags empty game id" in {
          val request = RegisterPlayer(GameId(""), "screen name")
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
    }

    "if the game doe not exist," - {
      "fails" in {
        val request = RegisterPlayer(GameId("does not exist"), "player name")
        registerPlayer(request, testConfig).isFailedAttempt() shouldEqual true
      }
    }
  }
}
