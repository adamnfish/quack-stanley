package com.adamnfish.quackstanley

import com.adamnfish.quackstanley.Logic._
import com.adamnfish.quackstanley.models.{GameId, GameState, PlayerKey}
import org.joda.time.DateTime
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global


class LogicTest extends FreeSpec with Matchers with AttemptValues {
  "newGame" - {
    "populates the player states map with just the creator" in {
      newGame("test-game", "test-player").players.size shouldEqual 1
    }

    "puts creator into the player states map" in {
      val (_, screenName) = newGame("test-game", "test-player").players.head
      screenName shouldEqual "test-player"
    }

    "sets initial state with started = false" in {
      newGame("test-game", "test-player").started shouldEqual false
    }

    "submitting player is in players " in {
      val initialGameState = newGame("test-game", "test-player")
      initialGameState.creator shouldEqual initialGameState.players.keys.head
    }
  }

  "newPlayer" - {
    "correctly sets up initial player state" in {
      newPlayer(GameId("game-id"), "game", "player") should have (
        'screenName ("player"),
        'gameName ("game")
      )
    }
  }

  "authenticate" - {
    val playerKey = PlayerKey("player")

    "when user exists in player map" - {
      val gameState = GameState(GameId("game-id"), "game-name", DateTime.now(), started = true, creator = PlayerKey("foo"),
        Map(
          PlayerKey("player") -> "player-name",
          PlayerKey("foo") -> "another-player"
        )
      )

      "returns the user's screenName" in {
        authenticate(playerKey, gameState).value() shouldEqual "player-name"
      }
    }

    "when user does not exist in player map" - {
      val gameState = GameState(GameId("game-id"), "game-name", DateTime.now(), started = true, creator = PlayerKey("foo"),
        Map(
          PlayerKey("foo") -> "another-player"
        )
      )

      "returns a not found failure" in {
        authenticate(playerKey, gameState).leftValue().statusCode shouldEqual 404
      }
    }
  }
}
