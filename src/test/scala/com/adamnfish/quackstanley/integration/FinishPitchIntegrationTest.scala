package com.adamnfish.quackstanley.integration

import java.util.UUID

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.persistence.GameIO
import com.adamnfish.quackstanley.{AttemptValues, Config, QuackStanley, TestPersistence}
import org.joda.time.DateTime
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest, OptionValues}

import scala.concurrent.ExecutionContext.Implicits.global


class FinishPitchIntegrationTest extends FreeSpec with Matchers
  with OneInstancePerTest with AttemptValues with OptionValues {

  val persistence = new TestPersistence
  val testConfig = Config("test", "test", persistence)

  val creatorUUID = UUID.randomUUID().toString
  val gameIdUUID = UUID.randomUUID().toString
  val gameDoesNotExistIdUUID = UUID.randomUUID().toString
  val playerKeyUUID = UUID.randomUUID().toString
  val playerDoesNotExistUUID = UUID.randomUUID().toString
  assert(
    Set(
      creatorUUID, gameIdUUID, gameDoesNotExistIdUUID, playerKeyUUID, playerDoesNotExistUUID
    ).size == 5,
    "Ensuring random UUID test data is distinct"
  )

  "finishPitch" - {
    "if the game exists" - {
      val creator = PlayerKey(creatorUUID)
      val gameId = GameId(gameIdUUID)
      val gameName = "game-name"

      "and this player is registered" - {
        val screenName = "player name"
        val playerKey = PlayerKey(playerKeyUUID)
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

        "persists discarded words to player's state" in {
          val playerInfo = finishPitch(request, testConfig).value()
          val persistedPlayerState = GameIO.getPlayerState(playerKey, gameId, testConfig).value()
          persistedPlayerState.discardedWords shouldEqual List(Word("one"), Word("two"))
        }

        "validates user input," - {
          "flags empty game id" in {
            val request = FinishPitch(GameId(""), playerKey, (Word(""), Word("")))
            val failure = finishPitch(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "game ID"
          }

          "ensures GameID is the correct format" in {
            val request = FinishPitch(GameId("not uuid"), playerKey, (Word(""), Word("")))
            val failure = finishPitch(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "game ID"
          }

          "flags empty player key" in {
            val request = FinishPitch(gameId, PlayerKey(""), (Word(""), Word("")))
            val failure = finishPitch(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "player key"
          }

          "ensures player key is the correct format" in {
            val request = FinishPitch(gameId, PlayerKey("not uuid"), (Word(""), Word("")))
            val failure = finishPitch(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "player key"
          }

          "flags empty first word" in {
            val request = FinishPitch(gameId, playerKey, (Word(""), Word("not-empty")))
            val failure = finishPitch(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "first word"
          }

          "flags empty second word" in {
            val request = FinishPitch(gameId, playerKey, (Word("not-empty"), Word("")))
            val failure = finishPitch(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "second word"
          }

          "gives all errors if multiple fields fail validation" in {
            val request = FinishPitch(GameId(""), PlayerKey(""), (Word(""), Word("")))
            val failure = finishPitch(request, testConfig).leftValue()
            failure.failures should have length 4
          }
        }
      }

      "and this player is not registered, fails to auth player" in {
        val gameState = GameState(gameId, gameName, DateTime.now(), true, creator, None,
          Map(creator -> "Creator")
        )
        val playerKey = PlayerKey(playerKeyUUID)
        val request = FinishPitch(gameId, playerKey, (Word("one"), Word("two")))

        GameIO.writeGameState(gameState, testConfig)
        finishPitch(request, testConfig).isFailedAttempt() shouldEqual true
      }
    }

    "if the game does not exist, fails to auth the player" in {
      val request = FinishPitch(GameId(gameDoesNotExistIdUUID), PlayerKey(playerDoesNotExistUUID), (Word("one"), Word("two")))
      finishPitch(request, testConfig).isFailedAttempt() shouldEqual true
    }
  }
}
