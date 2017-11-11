package com.adamnfish.quackstanley.integration

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.{AttemptValues, Config, TestPersistence}
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.persistence.GameIO
import org.joda.time.DateTime
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}

import scala.concurrent.ExecutionContext.Implicits.global


class BecomeBuyerIntegrationTest extends FreeSpec with Matchers with OneInstancePerTest with AttemptValues {
  val persistence = new TestPersistence
  val testConfig = Config("test", "test", persistence)

  "becomeBuyer" - {
    "if the game exists" - {
      val creator = PlayerKey("creator")
      val gameId = GameId("test-game")
      val gameName = "game-name"

      "and this player is registered" - {
        val screenName = "player name"
        val playerKey = PlayerKey("player-key")
        val playerState = PlayerState(gameId, gameName, screenName, List(Word("test")), Nil, None, Nil)
        val gameState = GameState(gameId, gameName, DateTime.now(), true, creator, None,
          Map(creator -> "Creator", playerKey -> screenName)
        )
        GameIO.writeGameState(gameState, testConfig)
        GameIO.writePlayerState(playerState, playerKey, testConfig)

        "returns role in player info" in {
          val request = BecomeBuyer(gameId, playerKey)
          val playerInfo = becomeBuyer(request, testConfig).value()
          playerInfo.state.role.isDefined shouldEqual true
        }

        "persists role in player state" in {
          val request = BecomeBuyer(gameId, playerKey)
          val playerInfo = becomeBuyer(request, testConfig).value()
          val persistedPlayerState = GameIO.getPlayerState(playerKey, gameId, testConfig).value()
          persistedPlayerState.role.isDefined shouldEqual true
        }

        "persists buyer in game state" in {
          val request = BecomeBuyer(gameId, playerKey)
          val playerInfo = becomeBuyer(request, testConfig).value()
          val persistedState = GameIO.getGameState(gameId, testConfig).value()
          persistedState.buyer.isDefined shouldEqual true
        }
      }

      "and this player is not registered, fails to auth player" in {
        val gameState = GameState(gameId, gameName, DateTime.now(), true, creator, None,
          Map(creator -> "Creator")
        )
        GameIO.writeGameState(gameState, testConfig)
        val request = BecomeBuyer(gameId, PlayerKey("does not exist"))
        becomeBuyer(request, testConfig).isFailedAttempt() shouldEqual true
      }
    }

    "if the game does not exist, fails to auth the player" in {
      val request = BecomeBuyer(GameId("does-not-exist"), PlayerKey("no-player"))
      becomeBuyer(request, testConfig).isFailedAttempt() shouldEqual true
    }
  }
}
