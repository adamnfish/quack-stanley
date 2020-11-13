package com.adamnfish.quackstanley.integration

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.models.{CreateGame, PlayerSummary}
import com.adamnfish.quackstanley.persistence.GameIO
import com.adamnfish.quackstanley.{AttemptValues, Config, HaveMatchers, TestPersistence}
import org.scalatest.{OneInstancePerTest, OptionValues}
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AnyFreeSpec

import scala.concurrent.ExecutionContext.Implicits.global


class CreateGameIntegrationTest extends AnyFreeSpec with Matchers
  with OneInstancePerTest with AttemptValues with OptionValues with HaveMatchers {

  val persistence = new TestPersistence
  val testConfig = Config("test", "test", persistence)

  "createGame" - {
    "uses provided names" in {
      val request = CreateGame("screen name", "game name")
      val newGame = createGame(request, testConfig).value()
      newGame.state should have(
        "gameName" as ("game name"),
        "screenName" as ("screen name")
      )
    }

    "does not deal any words or roles to the creator" in {
      val request = CreateGame("screen name", "game name")
      val newGame = createGame(request, testConfig).value()
      newGame.state should have(
        "role" as (None),
        "hand" as (Nil),
        "discardedWords" as (Nil),
        "points" as (Nil)
      )
    }

    "provides unique game IDs" in {
      val request1 = CreateGame("screen name", "game name")
      val request2 = CreateGame("screen name 2", "game name 2")
      val newGame1 = createGame(request1, testConfig).value()
      val newGame2 = createGame(request2, testConfig).value()
      newGame1.state.gameId should not equal newGame2.state.gameId
    }

    "provides unique player keys" in {
      val request1 = CreateGame("screen name", "game name")
      val request2 = CreateGame("screen name 2", "game name 2")
      val newGame1 = createGame(request1, testConfig).value()
      val newGame2 = createGame(request2, testConfig).value()
      newGame1.playerKey should not equal newGame2.playerKey
    }

    "sets this player as the creator on the saved game state" in {
      val request = CreateGame("screen name", "game name")
      val newGame = createGame(request, testConfig).value()
      val savedGameState = GameIO.getGameState(newGame.state.gameId, testConfig).value()
      savedGameState.creator shouldEqual newGame.playerKey
    }

    "puts creator into players with no points" in {
      val request = CreateGame("screen name", "game name")
      val newGame = createGame(request, testConfig).value()
      val savedGameState = GameIO.getGameState(newGame.state.gameId, testConfig).value()
      savedGameState.players shouldEqual Map(newGame.playerKey -> PlayerSummary("screen name", Nil))
    }

    "successfully generates a unique prefix code for the game" in {
      val request = CreateGame("screen name", "game name")
      val newGame = createGame(request, testConfig).value()
      newGame.state.gameId.value should startWith (newGame.gameCode)
    }

    "hard to test random UUIDs that clash for longer prefix codes" ignore {}

    "correctly persists game state" in {
      val request = CreateGame("screen name", "game name")
      val newGame = createGame(request, testConfig).value()
      val savedGameState = GameIO.getGameState(newGame.state.gameId, testConfig).value()
      savedGameState.creator shouldEqual newGame.playerKey
      savedGameState.gameId shouldEqual newGame.state.gameId
      savedGameState should have (
        "gameName" as ("game name"),
        "started" as (false)
      )
      savedGameState.players shouldEqual Map(newGame.playerKey -> PlayerSummary("screen name", Nil))
    }

    "correctly persists player state" in {
      val request = CreateGame("screen name", "game name")
      val newGame = createGame(request, testConfig).value()
      val savedPlayerState = GameIO.getPlayerState(newGame.playerKey, newGame.state.gameId, testConfig).value()
      savedPlayerState shouldEqual newGame.state
    }

    "validates user input," - {
      "flags empty screen name" in {
        val request = CreateGame("", "game name")
        val failure = createGame(request, testConfig).leftValue()
        failure.failures.head.context.value shouldEqual "screen name"
      }

      "flags empty game name" in {
        val request = CreateGame("screen name", "")
        val failure = createGame(request, testConfig).leftValue()
        failure.failures.head.context.value shouldEqual "game name"
      }

      "gives both errors if both are missing" in {
        val request = CreateGame("", "")
        val failure = createGame(request, testConfig).leftValue()
        failure.failures should have length 2
      }
    }
  }
}
