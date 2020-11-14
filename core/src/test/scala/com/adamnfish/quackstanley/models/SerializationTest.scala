package com.adamnfish.quackstanley.models

import com.adamnfish.quackstanley.{HaveMatchers, RightValues}
import com.adamnfish.quackstanley.models.Serialization._
import io.circe.parser._
import io.circe.syntax._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers


class SerializationTest extends AnyFreeSpec with Matchers with HaveMatchers with RightValues {
  "ApiOperation" - {
    "parses a create game request" in {
      val data = """{
                   |  "operation": "create-game",
                   |  "gameName": "test-game",
                   |  "screenName": "player-one"
                   |}""".stripMargin
      parse(data).value.as[CreateGame]
        .value should have(
          "gameName" as ("test-game"),
          "screenName" as ("player-one")
        )
    }

    "parses a create game request from an ApiOperation instance" in {
      val data = """{
                   |  "operation": "create-game",
                   |  "gameName": "test-game",
                   |  "screenName": "player-one"
                   |}""".stripMargin
      parse(data).value.as[ApiOperation]
        .value should have(
          "gameName" as ("test-game"),
          "screenName" as ("player-one")
        )
    }

    "parses an (empty) wake request" in {
      val data = """{
                   |  "operation": "wake"
                   |}""".stripMargin
      parse(data).value.as[ApiOperation]
        .value shouldEqual Wake()
    }

    "parses an awardPointRequest" in {
      val data =
        """{
          |  "operation": "award-point",
          |  "gameId": "abcdefgh-1234-5678-9abc-abcdefghijkl",
          |  "playerKey": "12345678-abcd-efgh-ijkl-123456789abc",
          |  "role": "role",
          |  "awardToPlayerWithName": "player"
          |}""".stripMargin
      parse(data).value.as[ApiOperation]
        .value should have(
          "gameId" as ("abcdefgh-1234-5678-9abc-abcdefghijkl"),
          "playerKey" as ("12345678-abcd-efgh-ijkl-123456789abc"),
          "role" as ("role"),
          "awardToPlayerWithName" as ("player")
        )
    }
  }

  "Value classes are encoded as values" - {
    "player key" in {
      PlayerKey("player-key").asJson.noSpaces shouldEqual "\"player-key\""
    }

    "game id" in {
      GameId("123-456").asJson.noSpaces shouldEqual "\"123-456\""
    }

    "word" in {
      Word("word").asJson.noSpaces shouldEqual "\"word\""
    }

    "role" in {
      Role("role").asJson.noSpaces shouldEqual "\"role\""
    }
  }

  "Value classes are decoded from values" - {
    "player key" in {
      parse("\"player-key\"").value.as[PlayerKey]
        .value shouldEqual PlayerKey("player-key")
    }

    "game ID" in {
      parse("\"game-id\"").value.as[GameId]
        .value shouldEqual GameId("game-id")
    }

    "word" in {
      parse("\"word\"").value.as[Word]
        .value shouldEqual Word("word")
    }

    "role" in {
      parse("\"role\"").value.as[Role]
        .value shouldEqual Role("role")
    }
  }
}
