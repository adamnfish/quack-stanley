package com.adamnfish.quackstanley

import com.adamnfish.quackstanley.Logic._
import com.adamnfish.quackstanley.models._
import org.joda.time.DateTime
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global


class LogicTest extends FreeSpec with Matchers with AttemptValues {
  "newGame" - {
    "populates the player states map with just the creator" in {
      newGame("test-game", "test-player").players.size shouldEqual 1
    }

    "puts creator into the player states map" in {
      val (_, screenName) = newGame("test-game", "test-player").players.head
      screenName shouldEqual "test-player"
    }

    "sets initial state with started = false" in {
      newGame("test-game", "test-player").started shouldEqual false
    }

    "submitting player is in players " in {
      val initialGameState = newGame("test-game", "test-player")
      initialGameState.creator shouldEqual initialGameState.players.keys.head
    }
  }

  "newPlayer" - {
    "correctly sets up initial player state" in {
      newPlayer(GameId("game-id"), "game", "player") should have (
        'screenName ("player"),
        'gameName ("game")
      )
    }
  }

  "authenticate" - {
    val playerKey = PlayerKey("player")

    "when user exists in player map" - {
      val gameState = GameState(GameId("game-id"), "game-name", DateTime.now(), started = true, creator = PlayerKey("foo"),
        Map(
          PlayerKey("player") -> "player-name",
          PlayerKey("foo") -> "another-player"
        )
      )

      "returns the user's screenName" in {
        authenticate(playerKey, gameState).value() shouldEqual "player-name"
      }
    }

    "when user does not exist in player map" - {
      val gameState = GameState(GameId("game-id"), "game-name", DateTime.now(), started = true, creator = PlayerKey("foo"),
        Map(
          PlayerKey("foo") -> "another-player"
        )
      )

      "returns a not found failure" in {
        authenticate(playerKey, gameState).leftValue().statusCode shouldEqual 404
      }
    }
  }

  "authenticateCreator" - {
    val gameState = GameState(GameId("game-id"), "game-name", DateTime.now(), started = true, creator = PlayerKey("foo"),
      Map(
        PlayerKey("player") -> "player-name",
        PlayerKey("foo") -> "another-player"
      )
    )

    "fails if the key is not in the group" in {
      authenticateCreator(PlayerKey("bar"), gameState)
    }

    "fails if the provided key is not the creator" in {
      authenticateCreator(PlayerKey("player"), gameState)
    }

    "succeeds if the provided player isthe creator" in {
      authenticateCreator(PlayerKey("foo"), gameState).isSuccessfulAttempt() shouldEqual true
    }
  }

  "playerNames" - {
    "returns list of screen names" in {
      val template = PlayerState(GameId("game"), "game name", "", Nil, Nil, None, Nil)
      val states = Map(
        PlayerKey("one") -> template.copy(screenName = "player one"),
        PlayerKey("two") -> template.copy(screenName = "player two"),
        PlayerKey("three") -> template.copy(screenName = "player three")
      )
      makePlayerNames(states) shouldEqual Map(
        PlayerKey("one") -> "player one",
        PlayerKey("two") -> "player two",
        PlayerKey("three") -> "player three"
      )
    }
  }

  "usedWords" in {
    val template = PlayerState(GameId("game"), "game name", "", Nil, Nil, None, Nil)
    val states = List(
      template.copy(
        hand = List(Word("one"), Word("two")),
        discardedWords = List(Word("three"))
      ),
      template.copy(
        hand = List(Word("four")),
        discardedWords = List(Word("five"))
      )
    )
    usedWords(states) shouldEqual Set(
      Word("one"),
      Word("two"),
      Word("three"),
      Word("four"),
      Word("five")
    )
  }

  "nextWords" - {
    "fails if no words are provided" in {
      val words = Nil
      val usedWords = Set.empty[Word]
      nextWords(1, words, usedWords).isFailedAttempt() shouldEqual true
    }

    "fails if remaining words have been used" in {
      val words = List(Word("one"), Word("two"), Word("three"))
      val usedWords = Set(Word("one"), Word("two"), Word("three"))
      nextWords(1, words, usedWords).isFailedAttempt() shouldEqual true
    }

    "returns an unused word" in {
      val words = List(Word("one"), Word("two"), Word("three"))
      val usedWords = Set(Word("one"), Word("two"))
      nextWords(1, words, usedWords).value() shouldEqual List(Word("three"))
    }
  }

  "usedRoles" in {
    val template = PlayerState(GameId("game"), "game name", "", Nil, Nil, None, Nil)
    val states = List(
      template.copy(
        points = List(Role("one"), Role("two")),
        role = Some(Role("three"))
      ),
      template.copy(
        points = List(Role("four")),
        role = Some(Role("five"))
      )
    )
    usedRoles(states) shouldEqual Set(
      Role("one"),
      Role("two"),
      Role("three"),
      Role("four"),
      Role("five")
    )
  }

  "nextRoles" - {
    "fails if no roles are provided" in {
      val roles = Nil
      val usedRoles = Set.empty[Role]
      nextRoles(1, roles, usedRoles).isFailedAttempt() shouldEqual true
    }

    "fails if remaining roles have been used" in {
      val roles = List(Role("one"), Role("two"), Role("three"))
      val usedRoles = Set(Role("one"), Role("two"), Role("three"))
      nextRoles(1, roles, usedRoles).isFailedAttempt() shouldEqual true
    }

    "returns an unused role" in {
      val roles = List(Role("one"), Role("two"), Role("three"))
      val usedRoles = Set(Role("one"), Role("two"))
      nextRoles(1, roles, usedRoles).value() shouldEqual List(Role("three"))
    }
  }

  "dealWords" - {
    "fails if there aren't enough words provided" ignore {}

    "deals each player 'hand size' words" ignore {}
  }
}
