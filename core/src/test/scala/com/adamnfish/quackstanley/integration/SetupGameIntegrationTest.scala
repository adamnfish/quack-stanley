package com.adamnfish.quackstanley.integration

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.models.{Config, SetupGame}
import com.adamnfish.quackstanley.persistence.GameIO
import com.adamnfish.quackstanley.{AttemptValues, HaveMatchers, QuackStanley, TestPersistence}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{OneInstancePerTest, OptionValues}

import scala.concurrent.ExecutionContext.Implicits.global


class SetupGameIntegrationTest extends AnyFreeSpec with Matchers
  with OneInstancePerTest with AttemptValues with OptionValues with HaveMatchers {

  val persistence = new TestPersistence
  val testConfig = Config("test", persistence)

  "setupGame" - {
    "provides unique game codes" in {
      val request1 = SetupGame("game name")
      val request2 = SetupGame("game name 2")
      val newEmptyGame1 = setupGame(request1, testConfig).value()
      val newEmptyGame2 = setupGame(request2, testConfig).value()
      newEmptyGame1.gameCode should not equal newEmptyGame2.gameCode
    }

    "provides unique host codes" in {
      val request1 = SetupGame("game name")
      val request2 = SetupGame("game name 2")
      val newEmptyGame1 = setupGame(request1, testConfig).value()
      val newEmptyGame2 = setupGame(request2, testConfig).value()
      newEmptyGame1.hostCode should not equal newEmptyGame2.hostCode
    }

    "game code is a prefix of the game ID (and can be used to look up the game)" in {
      val request = SetupGame("Test game")
      val newEmptyGame = QuackStanley.setupGame(request, testConfig).value()
      val gameId = GameIO.lookupGameIdFromCode(newEmptyGame.gameCode, persistence).value()
      val savedGameState = GameIO.getGameState(gameId, persistence).value()
      savedGameState.gameId.value should startWith(newEmptyGame.gameCode)
    }

    "host code is a prefix of the game's host key" in {
      val request = SetupGame("Test game")
      val newEmptyGame = QuackStanley.setupGame(request, testConfig).value()
      val gameId = GameIO.lookupGameIdFromCode(newEmptyGame.gameCode, persistence).value()
      val savedGameState = GameIO.getGameState(gameId, persistence).value()
      savedGameState.host.value should startWith(newEmptyGame.hostCode)
    }

    "Game should have no players after setup" in {
      val request = SetupGame("Test game")
      val newEmptyGame = QuackStanley.setupGame(request, testConfig).value()
      val gameId = GameIO.lookupGameIdFromCode(newEmptyGame.gameCode, persistence).value()
      val savedGameState = GameIO.getGameState(gameId, persistence).value()
      savedGameState.players shouldBe empty
    }

    "hard to test random UUIDs that clash for longer prefix codes" ignore {}

    "correctly persists game state" in {
      val request = SetupGame("Test game")
      val newEmptyGame = QuackStanley.setupGame(request, testConfig).value()
      val gameId = GameIO.lookupGameIdFromCode(newEmptyGame.gameCode, persistence).value()
      val savedGameState = GameIO.getGameState(gameId, persistence).value()
      savedGameState should have(
        "gameName" as ("Test game"),
        "started" as (false),
        "round" as None,
      )
    }

    "validates user input," - {
      "flags empty game name" in {
        val request = SetupGame("")
        val failure = setupGame(request, testConfig).leftValue()
        failure.failures.head.context.value shouldEqual "game name"
      }
    }
  }
}
