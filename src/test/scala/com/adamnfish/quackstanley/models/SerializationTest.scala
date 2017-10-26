package com.adamnfish.quackstanley.models

import org.scalatest.{EitherValues, FreeSpec, Matchers}
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}
import io.circe.parser._
import io.circe.generic.extras.semiauto._
import Serialization._


class SerializationTest extends FreeSpec with Matchers with EitherValues {
  "ApiOperation" - {
    "parses a create game request" in {
      val data = """{
                   |  "operation": "create-game",
                   |  "name": "test-game"
                   |}""".stripMargin
      parse(data).right.value.as[CreateGame]
        .right.value should have(
          'name ("test-game")
        )
    }

    "parses a create game request from an ApiOperation instance" in {
      val data = """{
                   |  "operation": "create-game",
                   |  "name": "test-game"
                   |}""".stripMargin
      parse(data).right.value.as[ApiOperation]
        .right.value should have(
          'name ("test-game")
        )
    }
  }

  "Value classes are encoded as values" - {
    "player key" in {
      PlayerKey("player-key").asJson.noSpaces shouldEqual "\"player-key\""
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
  }
}
