package com.adamnfish.quackstanley

import java.io._

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.attempt.LambdaIntegration._
import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.models.Serialization._
import com.adamnfish.quackstanley.models._
import com.amazonaws.services.lambda.runtime.Context

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Properties, Try}


class Main {
  def handleRequest(in: InputStream, out: OutputStream, context: Context): Unit = {
    val allowedOrigin = Properties.envOrNone("ORIGIN_LOCATION")
    val result = Try {
      for {
        config <- Config.fromEnvironment()
        opAndRequest <- parseBody[ApiOperation](in)
        (apiOperation, _) = opAndRequest
        response <- dispatch(apiOperation, config)
      } yield response
    }.fold(
      { e =>
        context.getLogger.log(s"Fatal error: ${e.getMessage} ${e.getStackTrace.mkString("; ")}")
        Attempt.Left(Failure(e.getMessage, "Unexpected server error", 500).asAttempt)
      },
      identity
    )
    respond[ApiResponse](result, out, context, allowedOrigin)
  }
}
