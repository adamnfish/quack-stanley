package com.adamnfish.quackstanley

import com.adamnfish.quackstanley.Logic._
import com.adamnfish.quackstanley.models._
import org.joda.time.DateTime
import org.scalatest.{FreeSpec, Matchers, OptionValues}

import scala.concurrent.ExecutionContext.Implicits.global


class LogicTest extends FreeSpec with Matchers with AttemptValues with OptionValues {
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

    "sets buyer to None" in {
      newGame("test-game", "test-player").buyer shouldEqual None
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
      val gameState = GameState(GameId("game-id"), "game-name", DateTime.now(), started = true, creator = PlayerKey("foo"), None,
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
      val gameState = GameState(GameId("game-id"), "game-name", DateTime.now(), started = true, creator = PlayerKey("foo"), None,
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
    val gameState = GameState(GameId("game-id"), "game-name", DateTime.now(), started = true, creator = PlayerKey("foo"), None,
      Map(
        PlayerKey("player") -> "player-name",
        PlayerKey("foo") -> "another-player"
      )
    )

    "fails if the key is not in the group" in {
      authenticateCreator(PlayerKey("bar"), gameState).isFailedAttempt() shouldEqual true
    }

    "fails if the provided key is not the creator" in {
      authenticateCreator(PlayerKey("player"), gameState).isFailedAttempt() shouldEqual true
    }

    "succeeds if the provided player is the creator" in {
      authenticateCreator(PlayerKey("foo"), gameState).isSuccessfulAttempt() shouldEqual true
    }
  }

  "authenticateBuyer" - {
    val gameState = GameState(GameId("game-id"), "game-name", DateTime.now(), started = true, creator = PlayerKey("foo"),
      buyer = Some(PlayerKey("foo")),
      Map(
        PlayerKey("player") -> "player-name",
        PlayerKey("foo") -> "another-player"
      )
    )

    "fails if the key is not in the group" in {
      authenticateBuyer(PlayerKey("bar"), gameState).isFailedAttempt() shouldEqual true
    }

    "fails if the provided key is not the buyer" in {
      authenticateBuyer(PlayerKey("player"), gameState).isFailedAttempt() shouldEqual true
    }

    "succeeds if the provided player is the buyer" in {
      authenticateBuyer(PlayerKey("foo"), gameState).isSuccessfulAttempt() shouldEqual true
    }
  }

  "lookupPlayerByName" - {
    val playerState = PlayerState(GameId("game"), "game name", "screen-name", Nil, Nil, None, Nil)
    val playerKey = PlayerKey("player")
    val players = Map(
      playerKey -> playerState,
      PlayerKey("player-2") -> PlayerState(GameId("game"), "game name", "another-screen-name", Nil, Nil, None, Nil)
    )

    "fails if the name is not found" in {
      lookupPlayerByName(players, "name-not-in-game").isFailedAttempt() shouldEqual true
    }

    "returns the player details if the player is found" in {
      val (key, playerState) = lookupPlayerByName(players, "screen-name").value()
      key shouldEqual playerKey
      playerState shouldEqual playerState
    }
  }

  "playerHasRole" - {
    "fails if the player has no role" in {
      val playerState = PlayerState(GameId("game"), "game name", "", Nil, Nil, None, Nil)
      playerHasRole(playerState, Role("role")).isFailedAttempt() shouldEqual true
    }

    "fails if the player does not have the provided role" in {
      val playerState = PlayerState(GameId("game"), "game name", "", Nil, Nil, Some(Role("role")), Nil)
      playerHasRole(playerState, Role("test")).isFailedAttempt() shouldEqual true
    }

    "succeeds if we provide the player's current role" in {
      val playerState = PlayerState(GameId("game"), "game name", "", Nil, Nil, Some(Role("role")), Nil)
      playerHasRole(playerState, Role("role")).isSuccessfulAttempt() shouldEqual true
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

  "verifyNoBuyer" - {
    "returns sucessful attempt if there is no buyer" in {
      val game = newGame("game name", "creator").copy(buyer = None)
      verifyNoBuyer(game).isFailedAttempt() shouldEqual false
    }

    "returns failed attempt if there is already a buyer" in {
      val game = newGame("game name", "creator").copy(buyer = Some(PlayerKey("player")))
      verifyNoBuyer(game).isFailedAttempt() shouldEqual true
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

  "nextRole" - {
    "fails if no roles are provided" in {
      val roles = Nil
      val usedRoles = Set.empty[Role]
      nextRole(roles, usedRoles).isFailedAttempt() shouldEqual true
    }

    "fails if remaining roles have been used" in {
      val roles = List(Role("one"), Role("two"), Role("three"))
      val usedRoles = Set(Role("one"), Role("two"), Role("three"))
      nextRole(roles, usedRoles).isFailedAttempt() shouldEqual true
    }

    "returns an unused role" in {
      val roles = List(Role("one"), Role("two"), Role("three"))
      val usedRoles = Set(Role("one"), Role("two"))
      nextRole(roles, usedRoles).value() shouldEqual Role("three")
    }
  }

  "dealWordsToAllPlayers" - {
    val template = PlayerState(GameId("game-id"), "game-name", "sreen-name", Nil, Nil, None, Nil)
    val players = Map(
      PlayerKey("one") -> template.copy(screenName = "player one"),
      PlayerKey("two") -> template.copy(screenName = "player two")
    )

    "fails if there aren't enough words provided" in {
      dealWordsToAllPlayers(Nil, players).isFailedAttempt() shouldEqual true
    }

    "deals each player 'hand size' words" in {
      val words = List.fill(QuackStanley.handSize * 2)(Word("test"))
      dealWordsToAllPlayers(words, players).value().forall { case (_, state) =>
        state.hand.size == QuackStanley.handSize
      } shouldEqual true
    }
  }

  "discardWords" - {
    val hand = List(Word("one"), Word("two"), Word("three"), Word("four"))
    val playerState = PlayerState(GameId("game-id"), "game name", "screen name", hand, Nil, None, Nil)

    "if both words are missing, returns multiple failures" in {
      val failure = discardWords((Word("foo"), Word("bar")), playerState).leftValue()
      failure.failures.size shouldEqual 2
      failure.failures.map(_.context.value) shouldEqual List("foo", "bar")
    }

    "if the first word is missing, returns that failure" in {
      val failure = discardWords((Word("foo"), Word("one")), playerState).leftValue()
      failure.failures.size shouldEqual 1
      failure.failures.map(_.context.value) shouldEqual List("foo")
    }

    "if the second word is missing, returns that failure" in {
      val failure = discardWords((Word("one"), Word("bar")), playerState).leftValue()
      failure.failures.size shouldEqual 1
      failure.failures.map(_.context.value) shouldEqual List("bar")
    }

    "if both words are present, returns the updated player state" in {
      val updatedPlayerState = discardWords((Word("one"), Word("two")), playerState).value()
      updatedPlayerState.hand shouldEqual List(Word("three"), Word("four"))
    }
  }

  "fillHand" - {
    "fails if we don't provide enough words" in {
      val result = fillHand(Nil, PlayerState(GameId("game-id"), "game name", "screen name", Nil, Nil, None, Nil))
      result.isFailedAttempt() shouldEqual true
    }

    "returns filled hand if we provide enough words" in {
      val words = List.fill(QuackStanley.handSize)(Word("one"))
      val updatedPlayerState = fillHand(words, PlayerState(GameId("game-id"), "game name", "screen name", Nil, Nil, None, Nil)).value()
      updatedPlayerState.hand shouldEqual words
    }
  }

  "addRoleToPoints" - {
    val points = List(Role("one"), Role("two"))
    val playerState = PlayerState(GameId("game-id"), "game name", "screen name", Nil, Nil, None, points)

    "adds the provided role to the already-received points" in {
      addRoleToPoints(playerState, Role("test-role")).points shouldEqual (points :+ Role("test-role"))
    }
  }
}
