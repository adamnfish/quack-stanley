package com.adamnfish.quackstanley.persistence

import com.adamnfish.quackstanley.models.{GameId, PlayerKey}
import org.scalatest.{FreeSpec, Matchers}


class GameIOTest extends FreeSpec with Matchers {
  "path function" - {
    val playerKey = PlayerKey("player-key")
    val gameId = GameId("test")

    "for game state" - {
      "does not start with slash" in {
        GameIO.gameStatePath(gameId).head should not equal "/"
      }

      "includes game id" in {
        GameIO.gameStatePath(gameId) should include ("/")
      }
    }

    "for player state" - {
      "does not start with slash" in {
        GameIO.playerStatePath(gameId, playerKey).head should not equal "/"
      }

      "includes game id" in {
        GameIO.playerStatePath(gameId, playerKey) should include ("/")
      }

      "player key is the filename" in {
        GameIO.playerStatePath(gameId, playerKey) should endWith (s"/${playerKey.value}.json")
      }
    }
  }
}
