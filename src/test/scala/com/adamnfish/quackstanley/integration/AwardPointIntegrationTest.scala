package com.adamnfish.quackstanley.integration

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.persistence.GameIO
import com.adamnfish.quackstanley.{AttemptValues, Config, TestPersistence}
import org.joda.time.DateTime
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


class AwardPointIntegrationTest extends FreeSpec with Matchers with OneInstancePerTest with AttemptValues {
  val persistence = new TestPersistence
  val testConfig = Config("test", "test", persistence)

  "awardPoint" - {
    "if the game exists" - {
      val creator = PlayerKey("creator")
      val gameId = GameId("test-game")
      val gameName = "game-name"

      "and this player is registered" - {
        val screenName = "player name"
        val playerKey = PlayerKey("player-key")
        val playerState = PlayerState(gameId, gameName, screenName, List(Word("test")), Nil, Some(Role("role")), Nil)
        val winningPlayerKey = PlayerKey("winning-player-key")
        val winnerScreenName = "winner"
        val winningPlayerState = PlayerState(gameId, gameName, winnerScreenName, Nil, Nil, None, Nil)
        val gameState = GameState(gameId, gameName, DateTime.now(), true, creator, Some(playerKey),
          Map(
            creator -> "Creator",
            playerKey -> screenName,
            winningPlayerKey -> "winner"
          )
        )
        GameIO.writeGameState(gameState, testConfig)
        GameIO.writePlayerState(playerState, playerKey, testConfig)
        GameIO.writePlayerState(winningPlayerState, winningPlayerKey, testConfig)

        "returns player info without role" in {
          val request = AwardPoint(gameId, playerKey, Role("role"), winnerScreenName)
          val playerInfo = awardPoint(request, testConfig).value()
          playerInfo.state.role.isEmpty shouldEqual true
        }

        "persists point in winner's state" in {
          val request = AwardPoint(gameId, playerKey, Role("role"), winnerScreenName)
          val playerInfo = awardPoint(request, testConfig).value()
          val persistedWinnerState = GameIO.getPlayerState(winningPlayerKey, gameId, testConfig).value()
          persistedWinnerState.points should contain(Role("role"))
        }

        "persists removal of role from player's state" in {
          val request = AwardPoint(gameId, playerKey, Role("role"), winnerScreenName)
          val playerInfo = awardPoint(request, testConfig).value()
          val persistedPlayerState = GameIO.getPlayerState(playerKey, gameId, testConfig).value()
          persistedPlayerState.role.isEmpty shouldEqual true
        }

        "persists removal of buyer from game state" in {
          val request = AwardPoint(gameId, playerKey, Role("role"), winnerScreenName)
          val playerInfo = awardPoint(request, testConfig).value()
          val persistedState = GameIO.getGameState(gameId, testConfig).value()
          persistedState.buyer.isDefined shouldEqual true
        }

        "fails if the player does not have the role to award" in {
          val request = AwardPoint(gameId, playerKey, Role("different-role"), winnerScreenName)
          awardPoint(request, testConfig).isFailedAttempt() shouldEqual true
        }

        "fails if the player is not the buyer" in {
          val request = AwardPoint(gameId, creator, Role("role"), winnerScreenName)
          awardPoint(request, testConfig).isFailedAttempt() shouldEqual true
        }
      }

      "and this player is not registered, fails to auth player" in {
        val gameState = GameState(gameId, gameName, DateTime.now(), true, creator, None,
          Map(creator -> "Creator")
        )
        GameIO.writeGameState(gameState, testConfig)
        val request = AwardPoint(gameId, PlayerKey("does not exist"), Role("role"), "winner")
        awardPoint(request, testConfig).isFailedAttempt() shouldEqual true
      }
    }

    "if the game does not exist, fails to auth the player" in {
      val request = AwardPoint(GameId("does-not-exist"), PlayerKey("no-player"), Role("role"), "winner")
      awardPoint(request, testConfig).isFailedAttempt() shouldEqual true
    }
  }
}
