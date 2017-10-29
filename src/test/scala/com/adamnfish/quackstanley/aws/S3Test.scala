package com.adamnfish.quackstanley.aws

import com.adamnfish.quackstanley.models.{GameId, PlayerKey}
import org.scalatest.{FreeSpec, Matchers}


class S3Test extends FreeSpec with Matchers {
  "path function" - {
    val playerKey = PlayerKey("player-key")
    val gameId = GameId("test")

    "for game state" - {
      "does not start with slash" in {
        S3.gameStatePath(gameId).head should not equal "/"
      }

      "includes game id" in {
        S3.gameStatePath(gameId) should include ("/")
      }
    }

    "for player state" - {
      "does not start with slash" in {
        S3.playerStatePath(gameId, playerKey).head should not equal "/"
      }

      "includes game id" in {
        S3.playerStatePath(gameId, playerKey) should include ("/")
      }

      "player key is the filename" in {
        S3.playerStatePath(gameId, playerKey) should endWith (s"/${playerKey.value}.json")
      }
    }
  }
}
