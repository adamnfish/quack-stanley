package com.adamnfish.quackstanley.integration

import java.util.UUID

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.persistence.GameIO
import com.adamnfish.quackstanley.{AttemptValues, Config, TestPersistence}
import org.joda.time.DateTime
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest, OptionValues}

import scala.concurrent.ExecutionContext.Implicits.global


class RelinquishBuyerIntegrationTest extends FreeSpec with Matchers
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

  "relinquishBuyer" - {
    "if the game exists" - {
      val creator = PlayerKey(creatorUUID)
      val gameId = GameId(gameIdUUID)
      val gameName = "game-name"

      "and this player is registered" - {
        val screenName = "player name"
        val playerKey = PlayerKey(playerKeyUUID)
        val role = Role("role")
        val playerState = PlayerState(gameId, gameName, screenName, List(Word("test")), Nil, Some(role), Nil)
        val gameState = GameState(gameId, gameName, DateTime.now(), true, creator, Some(Round(playerKey, role, Map.empty)),
          Map(creator -> PlayerSummary("Creator", Nil), playerKey -> PlayerSummary(screenName, Nil))
        )
        GameIO.writeGameState(gameState, testConfig)
        GameIO.writePlayerState(playerState, playerKey, testConfig)

        "returns no role in player info" in {
          val request = RelinquishBuyer(gameId, playerKey)
          val playerInfo = relinquishBuyer(request, testConfig).value()
          playerInfo.state.role.isEmpty shouldEqual true
        }

        "persists no role in player state" in {
          val request = RelinquishBuyer(gameId, playerKey)
          val playerInfo = relinquishBuyer(request, testConfig).value()
          val persistedPlayerState = GameIO.getPlayerState(playerKey, gameId, testConfig).value()
          persistedPlayerState.role.isEmpty shouldEqual true
        }

        "persists no buyer in game state" in {
          val request = RelinquishBuyer(gameId, playerKey)
          val playerInfo = relinquishBuyer(request, testConfig).value()
          val persistedState = GameIO.getGameState(gameId, testConfig).value()
          persistedState.round.isEmpty shouldEqual true
        }

        "excludes current player from otherPlayers" in {
          val request = RelinquishBuyer(gameId, playerKey)
          val playerInfo = relinquishBuyer(request, testConfig).value()
          playerInfo.opponents should not contain screenName
        }

        "validates user input," - {
          "flags empty game id" in {
            val request = RelinquishBuyer(GameId(""), playerKey)
            val failure = relinquishBuyer(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "game ID"
          }

          "ensures GameID is the correct format" in {
            val request = RelinquishBuyer(GameId("not uuid"), playerKey)
            val failure = relinquishBuyer(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "game ID"
          }

          "flags empty player key" in {
            val request = RelinquishBuyer(gameId, PlayerKey(""))
            val failure = relinquishBuyer(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "player key"
          }

          "ensures player key is the correct format" in {
            val request = RelinquishBuyer(gameId, PlayerKey("not uuid"))
            val failure = relinquishBuyer(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "player key"
          }

          "gives all errors if multiple fields fail validation" in {
            val request = RelinquishBuyer(GameId(""), PlayerKey(""))
            val failure = relinquishBuyer(request, testConfig).leftValue()
            failure.failures should have length 2
          }
        }
      }

      "and this player is not registered, fails to auth player" in {
        val gameState = GameState(gameId, gameName, DateTime.now(), true, creator, None,
          Map(creator -> PlayerSummary("Creator", Nil))
        )
        GameIO.writeGameState(gameState, testConfig)
        val request = RelinquishBuyer(gameId, PlayerKey(playerDoesNotExistUUID))
        relinquishBuyer(request, testConfig).isFailedAttempt() shouldEqual true
      }

      "and this player is not the buyer, fails to relinquish buyer" ignore {
        // TODO
      }
    }

    "if the game does not exist, fails to auth the player" in {
      val request = RelinquishBuyer(GameId(gameDoesNotExistIdUUID), PlayerKey(playerDoesNotExistUUID))
      relinquishBuyer(request, testConfig).isFailedAttempt() shouldEqual true
    }
  }
}
