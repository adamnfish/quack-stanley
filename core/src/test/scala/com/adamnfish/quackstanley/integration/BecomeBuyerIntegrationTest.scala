package com.adamnfish.quackstanley.integration

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.persistence.GameIO
import com.adamnfish.quackstanley.{AttemptValues, TestPersistence}
import org.joda.time.DateTime
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{OneInstancePerTest, OptionValues}

import java.util.UUID


class BecomeBuyerIntegrationTest extends AnyFreeSpec with Matchers
  with OneInstancePerTest with AttemptValues with OptionValues {

  val persistence = new TestPersistence
  val testConfig = Config("test", persistence)

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

  "becomeBuyer" - {
    "if the game exists" - {
      val creator = PlayerKey(creatorUUID)
      val gameId = GameId(gameIdUUID)
      val gameName = "game-name"

      "and this player is registered" - {
        val screenName = "player name"
        val playerKey = PlayerKey(playerKeyUUID)
        val playerState = PlayerState(gameId, gameName, screenName, List(Word("test")), Nil, None, Nil)
        val gameState = GameState(gameId, gameName, DateTime.now(), true, creator, None,
          Map(creator -> PlayerSummary("Creator", Nil), playerKey -> PlayerSummary(screenName, Nil))
        )
        GameIO.writeGameState(gameState, persistence)
        GameIO.writePlayerState(playerState, playerKey, persistence)

        "returns role in player info" in {
          val request = BecomeBuyer(gameId, playerKey)
          val playerInfo = becomeBuyer(request, testConfig).run()
          playerInfo.state.role.isDefined shouldEqual true
        }

        "persists role in player state" in {
          val request = BecomeBuyer(gameId, playerKey)
          val playerInfo = becomeBuyer(request, testConfig).run()
          val persistedPlayerState = GameIO.getPlayerState(playerKey, gameId, persistence).run()
          persistedPlayerState.role.isDefined shouldEqual true
        }

        "persists buyer in game state" in {
          val request = BecomeBuyer(gameId, playerKey)
          val playerInfo = becomeBuyer(request, testConfig).run()
          val persistedState = GameIO.getGameState(gameId, persistence).run()
          persistedState.round.isDefined shouldEqual true
        }

        "excludes current player from otherPlayers" in {
          val request = BecomeBuyer(gameId, playerKey)
          val playerInfo = becomeBuyer(request, testConfig).run()
          playerInfo.opponents should not contain screenName
        }

        "validates user input," - {
          "flags empty game id" in {
            val request = BecomeBuyer(GameId(""), playerKey)
            val failure = becomeBuyer(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "game ID"
          }

          "ensures GameID is the correct format" in {
            val request = BecomeBuyer(GameId("not uuid"), playerKey)
            val failure = becomeBuyer(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "game ID"
          }

          "flags empty player key" in {
            val request = BecomeBuyer(gameId, PlayerKey(""))
            val failure = becomeBuyer(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "player key"
          }

          "ensures player key is the correct format" in {
            val request = BecomeBuyer(gameId, PlayerKey("not uuid"))
            val failure = becomeBuyer(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "player key"
          }

          "gives all errors if multiple fields fail validation" in {
            val request = BecomeBuyer(GameId(""), PlayerKey(""))
            val failure = becomeBuyer(request, testConfig).leftValue()
            failure.failures should have length 2
          }
        }
      }

      "and this player is not registered, fails to auth player" in {
        val gameState = GameState(gameId, gameName, DateTime.now(), true, creator, None,
          Map(creator -> PlayerSummary("Creator", Nil))
        )
        GameIO.writeGameState(gameState, persistence)
        val request = BecomeBuyer(gameId, PlayerKey(playerDoesNotExistUUID))
        becomeBuyer(request, testConfig).isFailedAttempt()
      }

      "and this player is already the buyer, fails to become buyer" ignore {
        // TODO
      }
    }

    "if the game does not exist, fails to auth the player" in {
      val request = BecomeBuyer(GameId(gameDoesNotExistIdUUID), PlayerKey(playerDoesNotExistUUID))
      becomeBuyer(request, testConfig).isFailedAttempt()
    }
  }
}
