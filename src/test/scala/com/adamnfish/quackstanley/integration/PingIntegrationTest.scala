package com.adamnfish.quackstanley.integration

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.{AttemptValues, Config, TestPersistence}
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.persistence.GameIO
import org.joda.time.DateTime
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}

import scala.concurrent.ExecutionContext.Implicits.global


class PingIntegrationTest extends FreeSpec with Matchers with OneInstancePerTest with AttemptValues {
  val persistence = new TestPersistence
  val testConfig = Config("test", "test", persistence)

  "ping" - {
    "if the game exists" - {
      val creator = PlayerKey("creator")
      val gameId = GameId("test-game")
      val gameName = "game-name"

      "and this player is registered" - {
        val screenName = "player name"
        val playerKey = PlayerKey("player-key")
        val playerState = PlayerState(gameId, gameName, screenName, List(Word("test")), Nil, None, Nil)
        val gameState = GameState(gameId, gameName, DateTime.now(), true, creator,
          Map(creator -> "Creator", playerKey -> screenName)
        )
        GameIO.writeGameState(gameState, testConfig)
        GameIO.writePlayerState(playerState, playerKey, testConfig)

        "returns correct player info for player" in {
          val request = Ping(gameId, playerKey)
          val playerInfo = ping(request, testConfig).value()
          playerInfo.state.screenName shouldEqual screenName
        }
      }

      "and this player is not registered, fails to auth player" in {
        val gameState = GameState(gameId, gameName, DateTime.now(), true, creator,
          Map(creator -> "Creator")
        )
        GameIO.writeGameState(gameState, testConfig)
        val request = Ping(gameId, PlayerKey("does not exist"))
        ping(request, testConfig).isFailedAttempt() shouldEqual true
      }
    }

    "if the game does not exist, fails to auth the player" in {
      val request = Ping(GameId("does-not-exist"), PlayerKey("no-player"))
      ping(request, testConfig).isFailedAttempt() shouldEqual true
    }
  }
}
