package com.adamnfish.quackstanley.persistence

import com.adamnfish.quackstanley.AttemptValues
import com.adamnfish.quackstanley.models.{GameId, PlayerKey}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers


class GameIOTest extends AnyFreeSpec with Matchers with AttemptValues {
  "path function" - {
    val playerKey = PlayerKey("player-key")
    val gameId = GameId("test")

    "for game state" - {
      "does not start with slash" in {
        GameIO.gameStatePath(gameId).head should not equal "/"
      }

      "includes game id" in {
        GameIO.gameStatePath(gameId) should include (s"/${gameId.value}")
      }
    }

    "for player state" - {
      "does not start with slash" in {
        GameIO.playerStatePath(gameId, playerKey).head should not equal "/"
      }

      "includes game id" in {
        GameIO.playerStatePath(gameId, playerKey) should include (s"/${gameId.value}/")
      }

      "player key is the filename" in {
        GameIO.playerStatePath(gameId, playerKey) should endWith (s"/${playerKey.value}.json")
      }
    }
  }

  "playerKeyFromPath" - {
    "extracts player key from valid path" in {
      val playerKey = GameIO.playerKeyFromPath(s"data/game-id/players/player-key.json").run()
      playerKey shouldEqual PlayerKey("player-key")
    }

    "fails to extract player key from invalid path" in {
      GameIO.playerKeyFromPath(s"data/game-id/players/player-key.json.json").isFailedAttempt()

      GameIO.playerKeyFromPath("").isFailedAttempt()
    }
  }
}
