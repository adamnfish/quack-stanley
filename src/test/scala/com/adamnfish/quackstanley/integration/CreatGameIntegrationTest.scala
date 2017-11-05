package com.adamnfish.quackstanley.integration

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.models.CreateGame
import com.adamnfish.quackstanley.persistence.GameIO
import com.adamnfish.quackstanley.{AttemptValues, Config, TestPersistence}
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}

import scala.concurrent.ExecutionContext.Implicits.global


class CreatGameIntegrationTest extends FreeSpec with Matchers with OneInstancePerTest with AttemptValues {
  val persistence = new TestPersistence
  val testConfig = Config("test", "test", persistence)

  "createGame" - {
    "uses provided names" in {
      val request = CreateGame("screen name", "game name")
      val registered = createGame(request, testConfig).value()
      registered.state should have(
        'gameName ("game name"),
        'screenName ("screen name"),
      )
    }

    "does not deal any words or roles to the creator" in {
      val request = CreateGame("screen name", "game name")
      val registered = createGame(request, testConfig).value()
      registered.state should have(
        'role (None),
        'hand (Nil),
        'discardedWords (Nil),
        'points (Nil)
      )
    }

    "provides unique game IDs" in {
      val request1 = CreateGame("screen name", "game name")
      val request2 = CreateGame("screen name 2", "game name 2")
      val registered1 = createGame(request1, testConfig).value()
      val registered2 = createGame(request2, testConfig).value()
      registered1.state.gameId should not equal registered2.state.gameId
    }

    "provides unique player keys" in {
      val request1 = CreateGame("screen name", "game name")
      val request2 = CreateGame("screen name 2", "game name 2")
      val registered1 = createGame(request1, testConfig).value()
      val registered2 = createGame(request2, testConfig).value()
      registered1.playerKey should not equal registered2.playerKey
    }

    "sets this player as the creator on the saved game state" in {
      val request = CreateGame("screen name", "game name")
      val registered = createGame(request, testConfig).value()
      val savedGameState = GameIO.getGameState(registered.state.gameId, testConfig).value()
      savedGameState.creator shouldEqual registered.playerKey
    }

    "correctly persists game state" in {
      val request = CreateGame("screen name", "game name")
      val registered = createGame(request, testConfig).value()
      val savedGameState = GameIO.getGameState(registered.state.gameId, testConfig).value()
      savedGameState.creator shouldEqual registered.playerKey
      savedGameState.gameId shouldEqual registered.state.gameId
      savedGameState should have (
        'gameName ("game name"),
        'started (false)
      )
      savedGameState.players should contain(registered.playerKey -> "screen name")
    }

    "correctly persists player state" in {
      val request = CreateGame("screen name", "game name")
      val registered = createGame(request, testConfig).value()
      val savedPlayerState = GameIO.getPlayerState(registered.playerKey, registered.state.gameId, testConfig).value()
      savedPlayerState shouldEqual registered.state
    }
  }
}
