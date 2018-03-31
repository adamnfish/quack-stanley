package com.adamnfish.quackstanley

import com.adamnfish.quackstanley.Logic._
import com.adamnfish.quackstanley.attempt.{Attempt, FailedAttempt, Failure}
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.persistence.GameIO
import org.joda.time.DateTime
import org.scalatest.{FreeSpec, Matchers, OptionValues}

import scala.concurrent.ExecutionContext.Implicits.global


class LogicTest extends FreeSpec with Matchers with AttemptValues with OptionValues {
  "newGame" - {
    "populates the player states map with just the creator" in {
      newGame("test-game", "test-player").players.size shouldEqual 1
    }

    "puts creator into the player states map" in {
      val (_, playerSummary) = newGame("test-game", "test-player").players.head
      playerSummary.screenName shouldEqual "test-player"
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

  "playerInfo" - {
    val gameState = newGame("game name", "Creator")
    val playerState1 = newPlayer(gameState.gameId, gameState.gameName, "Player 1")
    val playerKey1 = generatePlayerKey()
    val playerState2 = newPlayer(gameState.gameId, gameState.gameName, "Player 2")
    val playerKey2 = generatePlayerKey()
    val allPlayers = gameState.players +
      (playerKey1 -> PlayerSummary(playerState1.screenName, Nil)) +
      (playerKey2 -> PlayerSummary(playerState2.screenName, Nil))
    val gameStateWithPlayers = gameState.copy(players = allPlayers)

    "sets started from the game state" in {
      playerInfo(playerKey1, playerState1, gameStateWithPlayers).started shouldEqual false
    }

    "sets started from a started game's state" in {
      val started = gameStateWithPlayers.copy(started = true)
      playerInfo(playerKey1, playerState1, started).started shouldEqual true
    }

    "sets player state" in {
      playerInfo(playerKey1, playerState1, gameStateWithPlayers).state shouldEqual playerState1
    }

    "excludes this player from 'otherPlayers'" in {
      playerInfo(playerKey1, playerState1, gameStateWithPlayers).opponents.map(_.screenName) should not contain("Player 1")
    }

    "includes other players" in {
      playerInfo(playerKey1, playerState1, gameStateWithPlayers).opponents.map(_.screenName) should contain only("Player 2", "Creator")
    }
  }

  "authenticate" - {
    val playerKey = PlayerKey("player")

    "when user exists in player map" - {
      val gameState = GameState(GameId("game-id"), "game-name", DateTime.now(), started = true, creator = PlayerKey("foo"), None,
        Map(
          PlayerKey("player") -> PlayerSummary("player-name", Nil),
          PlayerKey("foo") -> PlayerSummary("another-player", Nil)
        )
      )

      "returns the user's summary" in {
        authenticate(playerKey, gameState).value().screenName shouldEqual "player-name"
      }
    }

    "when user does not exist in player map" - {
      val gameState = GameState(GameId("game-id"), "game-name", DateTime.now(), started = true, creator = PlayerKey("foo"), None,
        Map(
          PlayerKey("foo") -> PlayerSummary("another-player", Nil)
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
        PlayerKey("player") -> PlayerSummary("player-name", Nil),
        PlayerKey("foo") -> PlayerSummary("another-player", Nil)
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
        PlayerKey("player") -> PlayerSummary("player-name", Nil),
        PlayerKey("foo") -> PlayerSummary("another-player", Nil)
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

  "playerSummaries" - {
    "returns player summaries from player states" in {
      val template = PlayerState(GameId("game"), "game name", "", Nil, Nil, None, Nil)
      val states = Map(
        PlayerKey("one") -> template.copy(screenName = "player one"),
        PlayerKey("two") -> template.copy(screenName = "player two"),
        PlayerKey("three") -> template.copy(screenName = "player three")
      )
      playerSummaries(states) shouldEqual Map(
        PlayerKey("one") -> PlayerSummary("player one", Nil),
        PlayerKey("two") -> PlayerSummary("player two", Nil),
        PlayerKey("three") -> PlayerSummary("player three", Nil)
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

    "failure includes current buyer's name" in {
      val game = newGame("game name", "creator")
      val gameWithBuyer = game.copy(
        buyer = Some(PlayerKey("player")),
        players = game.players + (PlayerKey("player") -> PlayerSummary("player name", Nil))
      )
      verifyNoBuyer(gameWithBuyer).leftValue().failures.head.friendlyMessage should include("player name")
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

    "puts the discarded words into the returned player state" in {
      val updatedPlayerState = discardWords((Word("one"), Word("two")), playerState).value()
      updatedPlayerState.discardedWords shouldEqual List(Word("one"), Word("two"))
    }

    "adds the discarded words to the existing player state" in {
      val discardedPlayerState = playerState.copy(discardedWords = List(Word("foo"), Word("bar")))
      val updatedPlayerState = discardWords((Word("one"), Word("two")), discardedPlayerState).value()
      updatedPlayerState.discardedWords shouldEqual List(Word("foo"), Word("bar"), Word("one"), Word("two"))
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

  "makeUniquePrefix" - {
    val gameId = GameId("123456789abcdef0")
    val testPersistence = new TestPersistence
    val testConfig = Config("test", "test", testPersistence)

    "returns default prefix if it is clear" in {
      def fn(gid: GameId, n: Int, c: Config): Attempt[Boolean] = Attempt.Right(true)
      makeUniquePrefix(gameId, testConfig, fn).value() shouldEqual "1234"
    }

    "returns longer prefix if first one was not clear" in {
      def fn(gid: GameId, n: Int, c: Config): Attempt[Boolean] = Attempt.Right(n > 4)
      makeUniquePrefix(gameId, testConfig, fn).value() shouldEqual "12345"
    }

    "fails if thea unique prefix cannot be found" in {
      def fn(gid: GameId, n: Int, c: Config): Attempt[Boolean] = Attempt.Right(false)
      makeUniquePrefix(gameId, testConfig, fn).isFailedAttempt() shouldEqual true
    }

    "fails if the provided fn fails" in {
      val expected = FailedAttempt(Failure("test", "test", 500))
      def fn(gid: GameId, n: Int, c: Config): Attempt[Boolean] = Attempt.Left(expected)
      makeUniquePrefix(gameId, testConfig, fn).leftValue() shouldEqual expected
    }
  }

  "gameIdFromPrefixResults" - {
    val gameCode = "abcdef"
    val gameId = GameId("abcdef1234")

    "with valid results" - {
      val results = List(
        GameIO.playerStatePath(gameId, PlayerKey("player-key")),
        GameIO.gameStatePath(gameId)
      )

      "returns ID of matching game" in {
        gameIdFromPrefixResults(gameCode, results).value().value shouldEqual "abcdef1234"
      }

      "returns ID of game if prefix code is the entire ID" in {
        gameIdFromPrefixResults(gameId.value, results).value().value shouldEqual "abcdef1234"
      }
    }

    "fails if no matches are found" in {
      val noMatches = List("abc", "def")
      gameIdFromPrefixResults(gameCode, noMatches).isFailedAttempt() shouldEqual true
    }

    "fails if more than one game matches" in {
      val badMatch = GameId(gameCode ++ "abc")
      val multiMatches = List(
        GameIO.playerStatePath(gameId, PlayerKey("player-key")),
        GameIO.gameStatePath(gameId),
        GameIO.playerStatePath(badMatch, PlayerKey("player-key")),
        GameIO.gameStatePath(badMatch)
      )
      gameIdFromPrefixResults(gameCode, multiMatches).isFailedAttempt() shouldEqual true
    }
  }
}
