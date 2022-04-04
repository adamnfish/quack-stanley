package com.adamnfish.quackstanley

import com.adamnfish.quackstanley.LambdaIntegration.{headers, parseBody}
import com.adamnfish.quackstanley.Utils._
import com.adamnfish.quackstanley.models.ApiOperation
import com.adamnfish.quackstanley.models.Serialization._
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.InputStream


class LambdaIntegrationTest extends AnyFreeSpec with Matchers with AttemptValues with OptionValues with HaveMatchers {
  "headers" - {
    "includes default headers (just Content-Type) if no origin is supplied" in {
      headers(None).keys.toList shouldEqual List("Content-Type")
    }

    "includes Content-Type header if origin is supplied" in {
      headers(Some("origin")).keys should contain("Content-Type")
    }

    "includes Access-Control-Allow-Origin header if origin is supplied" in {
      headers(Some("origin")).get("Access-Control-Allow-Origin").value shouldEqual "origin"
    }
  }

  "parseBody" - {
    "parses a create game request" in {
      val data = """"{ \"operation\": \"create-game\", \"gameName\": \"test-game\", \"screenName\": \"player-one\" }""""
      val (operation, _) = parseBody[ApiOperation](asLambdaRequest(data)).run()
      operation should have(
        "gameName" as ("test-game"),
        "screenName" as ("player-one")
      )
    }
  }

  def asLambdaRequest(json: String): InputStream = {
    s"""{
       |  "body": $json,
       |  "httpMethod": "post",
       |  "path": "path",
       |  "queryStringParameters": {},
       |  "headers": {}
       |}""".stripMargin.asStream()
  }
}
