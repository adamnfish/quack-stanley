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


class AwardPointIntegrationTest extends AnyFreeSpec with Matchers
  with OneInstancePerTest with AttemptValues with OptionValues {

  val persistence = new TestPersistence
  val testConfig = Config("test", persistence)
  val creatorUUID = UUID.randomUUID().toString
  val gameIdUUID = UUID.randomUUID().toString
  val gameDoesNotExistIdUUID = UUID.randomUUID().toString
  val playerKeyUUID = UUID.randomUUID().toString
  val playerDoesNotExistUUID = UUID.randomUUID().toString
  val winningPlayerKeyUUID = UUID.randomUUID().toString
  assert(
    Set(
      creatorUUID, gameIdUUID, gameDoesNotExistIdUUID, playerKeyUUID, playerDoesNotExistUUID, winningPlayerKeyUUID
    ).size == 6,
    "Ensuring random UUID test data is distinct"
  )

  "awardPoint" - {
    "if the game exists" - {
      val creator = PlayerKey(creatorUUID)
      val gameId = GameId(gameIdUUID)
      val gameName = "game-name"

      "and this player and winner are both registered" - {
        val screenName = "player name"
        val playerKey = PlayerKey(playerKeyUUID)
        val role = Role("role")
        val playerState = PlayerState(gameId, gameName, screenName, List(Word("test")), Nil, Some(role), Nil)
        val winningPlayerKey = PlayerKey(winningPlayerKeyUUID)
        val winnerScreenName = "winner"
        val winningPlayerState = PlayerState(gameId, gameName, winnerScreenName, Nil, Nil, None, Nil)
        val gameState = GameState(gameId, gameName, DateTime.now(), true, creator, Some(Round(playerKey, role, Map.empty)),
          Map(
            creator -> PlayerSummary("Creator", Nil),
            playerKey -> PlayerSummary(screenName, Nil),
            winningPlayerKey -> PlayerSummary(winnerScreenName, Nil)
          )
        )
        GameIO.writeGameState(gameState, persistence)
        GameIO.writePlayerState(playerState, playerKey, persistence)
        GameIO.writePlayerState(winningPlayerState, winningPlayerKey, persistence)

        "returns player info without role" in {
          val request = AwardPoint(gameId, playerKey, Role("role"), winnerScreenName)
          val playerInfo = awardPoint(request, testConfig).run()
          playerInfo.state.role.isEmpty shouldEqual true
        }

        "returns state includes new point in winners summary" in {
          val request = AwardPoint(gameId, playerKey, Role("role"), winnerScreenName)
          val playerInfo = awardPoint(request, testConfig).run()
          val winnerSummary = playerInfo.opponents.find(_.screenName == winnerScreenName).value
          winnerSummary.points should contain(Role("role"))
        }

        "persists point in winner's state" in {
          val request = AwardPoint(gameId, playerKey, Role("role"), winnerScreenName)
          val playerInfo = awardPoint(request, testConfig).run()
          val persistedWinnerState = GameIO.getPlayerState(winningPlayerKey, gameId, persistence).run()
          persistedWinnerState.points should contain(Role("role"))
        }

        "persists point in winner's player summary in game's state" in {
          val request = AwardPoint(gameId, playerKey, Role("role"), winnerScreenName)
          val playerInfo = awardPoint(request, testConfig).run()
          val persistedGameState = GameIO.getGameState(gameId, persistence).run()
          persistedGameState.players.get(winningPlayerKey).value.points should contain(Role("role"))
        }

        "persists removal of role from player's state" in {
          val request = AwardPoint(gameId, playerKey, Role("role"), winnerScreenName)
          val playerInfo = awardPoint(request, testConfig).run()
          val persistedPlayerState = GameIO.getPlayerState(playerKey, gameId, persistence).run()
          persistedPlayerState.role.isEmpty shouldEqual true
        }

        "persists removal of buyer from game state" in {
          val request = AwardPoint(gameId, playerKey, Role("role"), winnerScreenName)
          val playerInfo = awardPoint(request, testConfig).run()
          val persistedState = GameIO.getGameState(gameId, persistence).run()
          persistedState.round.isEmpty shouldEqual true
        }

        "excludes current player from otherPlayers" in {
          val request = AwardPoint(gameId, playerKey, Role("role"), winnerScreenName)
          val playerInfo = awardPoint(request, testConfig).run()
          playerInfo.opponents should not contain screenName
        }

        "fails if the player does not have the role to award" in {
          val request = AwardPoint(gameId, playerKey, Role("different-role"), winnerScreenName)
          awardPoint(request, testConfig).isFailedAttempt()
        }

        "fails if the player is not the buyer" in {
          val request = AwardPoint(gameId, creator, Role("role"), winnerScreenName)
          awardPoint(request, testConfig).isFailedAttempt()
        }

        "validates user input," - {
          "flags empty game id" in {
            val request = AwardPoint(GameId(""), playerKey, Role("role"), winnerScreenName)
            val failure = awardPoint(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "game ID"
          }

          "ensures GameID is the correct format" in {
            val request = AwardPoint(GameId("not uuid"), playerKey, Role("role"), winnerScreenName)
            val failure = awardPoint(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "game ID"
          }

          "flags empty player key" in {
            val request = AwardPoint(gameId, PlayerKey(""), Role("role"), winnerScreenName)
            val failure = awardPoint(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "player key"
          }

          "ensures player key is the correct format" in {
            val request = AwardPoint(gameId, PlayerKey("not uuid"), Role("role"), winnerScreenName)
            val failure = awardPoint(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "player key"
          }

          "flags empty role" in {
            val request = AwardPoint(gameId, playerKey, Role(""), winnerScreenName)
            val failure = awardPoint(request, testConfig).leftValue()
            failure.failures.head.context.value shouldEqual "role"
          }

          "gives all errors if multiple fields fail validation" in {
            val request = AwardPoint(GameId(""), PlayerKey(""), Role(""), winnerScreenName)
            val failure = awardPoint(request, testConfig).leftValue()
            failure.failures should have length 3
          }
        }
      }

      "and this player is not registered, fails to auth player" in {
        val gameState = GameState(gameId, gameName, DateTime.now(), true, creator, None,
          Map(creator -> PlayerSummary("Creator", Nil))
        )
        GameIO.writeGameState(gameState, persistence)
        val request = AwardPoint(gameId, PlayerKey(playerDoesNotExistUUID), Role("role"), "winner")
        awardPoint(request, testConfig).isFailedAttempt()
      }
    }

    "if the game does not exist, fails to auth the player" in {
      val request = AwardPoint(GameId(gameDoesNotExistIdUUID), PlayerKey(playerDoesNotExistUUID), Role("role"), "winner")
      awardPoint(request, testConfig).isFailedAttempt()
    }
  }
}
