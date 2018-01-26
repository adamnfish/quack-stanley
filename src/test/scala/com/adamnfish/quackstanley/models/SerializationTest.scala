package com.adamnfish.quackstanley.models

import com.adamnfish.quackstanley.models.Serialization._
import io.circe.parser._
import io.circe.syntax._
import org.scalatest.{EitherValues, FreeSpec, Matchers}


class SerializationTest extends FreeSpec with Matchers with EitherValues {
  "ApiOperation" - {
    "parses a create game request" in {
      val data = """{
                   |  "operation": "create-game",
                   |  "gameName": "test-game",
                   |  "screenName": "player-one"
                   |}""".stripMargin
      parse(data).right.value.as[CreateGame]
        .right.value should have(
          'gameName ("test-game"),
          'screenName ("player-one")
        )
    }

    "parses a create game request from an ApiOperation instance" in {
      val data = """{
                   |  "operation": "create-game",
                   |  "gameName": "test-game",
                   |  "screenName": "player-one"
                   |}""".stripMargin
      parse(data).right.value.as[ApiOperation]
        .right.value should have(
          'gameName ("test-game"),
          'screenName ("player-one")
        )
    }

    "parses an (empty) wake request" in {
      val data = """{
                   |  "operation": "wake"
                   |}""".stripMargin
      parse(data).right.value.as[ApiOperation]
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
      parse("\"player-key\"").right.value.as[PlayerKey]
        .right.value shouldEqual PlayerKey("player-key")
    }

    "game ID" in {
      parse("\"game-id\"").right.value.as[GameId]
        .right.value shouldEqual GameId("game-id")
    }

    "word" in {
      parse("\"word\"").right.value.as[Word]
        .right.value shouldEqual Word("word")
    }

    "role" in {
      parse("\"role\"").right.value.as[Role]
        .right.value shouldEqual Role("role")
    }
  }
}
