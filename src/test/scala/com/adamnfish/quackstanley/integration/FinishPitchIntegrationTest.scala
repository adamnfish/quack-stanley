package com.adamnfish.quackstanley.integration

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.persistence.GameIO
import com.adamnfish.quackstanley.{AttemptValues, Config, QuackStanley, TestPersistence}
import org.joda.time.DateTime
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}

import scala.concurrent.ExecutionContext.Implicits.global


class FinishPitchIntegrationTest extends FreeSpec with Matchers with OneInstancePerTest with AttemptValues {
  val persistence = new TestPersistence
  val testConfig = Config("test", "test", persistence)

  "finishPitch" - {
    "if the game exists" - {
      val creator = PlayerKey("creator")
      val gameId = GameId("test-game")
      val gameName = "game-name"

      "and this player is registered" - {
        val screenName = "player name"
        val playerKey = PlayerKey("player-key")
        val hand = Word("one") :: Word("two") :: List.fill(QuackStanley.handSize - 2)(Word("padding"))
        val playerState = PlayerState(gameId, gameName, screenName, hand, Nil, None, Nil)
        val gameState = GameState(gameId, gameName, DateTime.now(), true, creator, None,
          Map(creator -> "Creator", playerKey -> screenName)
        )
        val request = FinishPitch(gameId, playerKey, (Word("one"), Word("two")))
        GameIO.writeGameState(gameState, testConfig)
        GameIO.writePlayerState(playerState, playerKey, testConfig)

        "fills hand" in {
          val playerInfo = finishPitch(request, testConfig).value()
          playerInfo.state.hand.size shouldEqual QuackStanley.handSize
        }

        "new hand contains the non-discarded words" in {
          val playerInfo = finishPitch(request, testConfig).value()
          playerInfo.state.hand.count(_ == Word("padding")) shouldEqual (QuackStanley.handSize - 2)
        }

        "new hand contains replacement words" in {
          val playerInfo = finishPitch(request, testConfig).value()
          playerInfo.state.hand.count(_ != Word("padding")) shouldEqual 2
        }

        "persists new hand in player state" in {
          val playerInfo = finishPitch(request, testConfig).value()
          val persistedPlayerState = GameIO.getPlayerState(playerKey, gameId, testConfig).value()
          persistedPlayerState.hand shouldEqual playerInfo.state.hand
        }
      }

      "and this player is not registered, fails to auth player" in {
        val gameState = GameState(gameId, gameName, DateTime.now(), true, creator, None,
          Map(creator -> "Creator")
        )
        val playerKey = PlayerKey("player-key")
        val request = FinishPitch(gameId, playerKey, (Word("one"), Word("two")))

        GameIO.writeGameState(gameState, testConfig)
        finishPitch(request, testConfig).isFailedAttempt() shouldEqual true
      }
    }

    "if the game does not exist, fails to auth the player" in {
      val request = FinishPitch(GameId("game-id"), PlayerKey("player-key"), (Word("one"), Word("two")))
      finishPitch(request, testConfig).isFailedAttempt() shouldEqual true
    }
  }
}
