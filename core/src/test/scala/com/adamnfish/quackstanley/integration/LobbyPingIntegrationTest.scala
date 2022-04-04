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


class LobbyPingIntegrationTest extends AnyFreeSpec with Matchers
  with OneInstancePerTest with AttemptValues with OptionValues {

  val persistence = new TestPersistence
  val testConfig = Config("test", persistence)

  val creatorUUID = UUID.randomUUID().toString
  val gameIdUUID = UUID.randomUUID().toString
  val gameDoesNotExistIdUUID = UUID.randomUUID().toString
  val playerKeyUUID = UUID.randomUUID().toString
  val player2KeyUUID = UUID.randomUUID().toString
  val playerDoesNotExistUUID = UUID.randomUUID().toString
  assert(
    Set(
      creatorUUID, gameIdUUID, gameDoesNotExistIdUUID, playerKeyUUID, player2KeyUUID, playerDoesNotExistUUID
    ).size == 6,
    "Ensuring random UUID test data is distinct"
  )

  "lobbyPing" - {
    "if the game exists" - {
      val gameId = GameId(gameIdUUID)
      val gameName = "game-name"
      val creatorScreenName = "creator"
      val creatorKey = PlayerKey(creatorUUID)
      val creatorState = PlayerState(gameId, gameName, creatorScreenName, Nil, Nil, None, Nil)

      val playerScreenName = "player"
      val playerKey = PlayerKey(playerKeyUUID)
      val playerState = PlayerState(gameId, gameName, playerScreenName, Nil, Nil, None, Nil)
      val player2ScreenName = "player2"
      val player2Key = PlayerKey(player2KeyUUID)
      val player2State = PlayerState(gameId, gameName, player2ScreenName, Nil, Nil, None, Nil)

      val gameState = GameState(gameId, gameName, DateTime.now(), started = false, creatorKey, None,
        Map(creatorKey -> PlayerSummary(creatorScreenName, Nil))
      )
      GameIO.writeGameState(gameState, persistence).run()
      GameIO.writePlayerState(creatorState, creatorKey, persistence).run()
      GameIO.writePlayerState(playerState, playerKey, persistence).run()
      GameIO.writePlayerState(player2State, player2Key, persistence).run()

      // LOOK AT START GAME FOR CREATOR VALIDATION ETC

      "and this player is the creator" - {
        "succeeds" in {
          val request = LobbyPing(gameId, creatorKey)
          lobbyPing(request, testConfig).isSuccessfulAttempt()
        }

        "returns correct player info for creator" in {
          val request = LobbyPing(gameId, creatorKey)
          val playerInfo = lobbyPing(request, testConfig).run()
          playerInfo.state.screenName shouldEqual creatorScreenName
        }

        "excludes correct player from 'otherPlayers'" in {
          val request = LobbyPing(gameId, creatorKey)
          val playerInfo = lobbyPing(request, testConfig).run()
          playerInfo.opponents should not contain creatorScreenName
        }

        "includes other players in 'otherPlayers'" in {
          val request = LobbyPing(gameId, creatorKey)
          val playerInfo = lobbyPing(request, testConfig).run()
          playerInfo.opponents.map(_.screenName) should contain.allOf(playerScreenName, player2ScreenName)
        }

        "validates user input," - {
          "flags empty game id" in {
            val request = LobbyPing(GameId(""), creatorKey)
            val failure = lobbyPing(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "game ID"
          }

          "ensures GameID is the correct format" in {
            val request = LobbyPing(GameId("not uuid"), creatorKey)
            val failure = lobbyPing(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "game ID"
          }

          "flags empty player key" in {
            val request = LobbyPing(gameId, PlayerKey(""))
            val failure = lobbyPing(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "player key"
          }

          "ensures player key is the correct format" in {
            val request = LobbyPing(gameId, PlayerKey("not uuid"))
            val failure = lobbyPing(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "player key"
          }

          "gives all errors if multiple fields fail validation" in {
            val request = LobbyPing(GameId(""), PlayerKey(""))
            val failure = lobbyPing(request, testConfig).leftValue()
            failure.failures should have length 2
          }
        }
      }

      "and this player is not registered, fails to auth player" in {
        val request = LobbyPing(gameId, PlayerKey(playerDoesNotExistUUID))
        lobbyPing(request, testConfig).isFailedAttempt()
      }

      "if the player is not the creator, fails to perform the action" in {
        val request = LobbyPing(gameId, playerKey)
        lobbyPing(request, testConfig).isFailedAttempt()
      }

      "fails if the game has already started" in {
        val startedState = gameState.copy(started = true)
        GameIO.writeGameState(startedState, persistence).run()
        val request = LobbyPing(gameId, creatorKey)
        lobbyPing(request, testConfig).isFailedAttempt()
      }
    }

    "if the game does not exist, fails to auth the player" in {
      val request = LobbyPing(GameId(gameDoesNotExistIdUUID), PlayerKey(playerDoesNotExistUUID))
      lobbyPing(request, testConfig).isFailedAttempt()
    }
  }
}
