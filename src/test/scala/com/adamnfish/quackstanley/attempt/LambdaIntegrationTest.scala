package com.adamnfish.quackstanley.attempt

import java.io.InputStream

import com.adamnfish.quackstanley.AttemptValues
import com.adamnfish.quackstanley.Utils._
import com.adamnfish.quackstanley.attempt.LambdaIntegration._
import com.adamnfish.quackstanley.models.ApiOperation
import com.adamnfish.quackstanley.models.Serialization._
import org.scalatest.Matchers

import scala.concurrent.ExecutionContext.Implicits.global


class LambdaIntegrationTest extends org.scalatest.FreeSpec with Matchers with AttemptValues {
  "parseBody" - {
    "parses a create game request" in {
      val data = """"{ \"operation\": \"create-game\", \"name\": \"test-game\" }""""
      val (operation, _) = parseBody[ApiOperation](asLambdaRequest(data)).value()
      operation should have(
        'name ("test-game")
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
