package com.adamnfish.quackstanley

import java.io._

import com.adamnfish.quackstanley.LambdaIntegration._
import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.aws.S3
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
        config <- configFromEnvironment()
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

  def configFromEnvironment(): Attempt[Config] = {
    for {
      bucket <- Attempt.fromOption(Properties.envOrNone("APP_DATA_S3_BUCKET"), {
        Failure("Couldn't read S3 bucket name for configuration", "Quack Stanley failed because it is missing configuration", 500, Some("APP_DATA_S3_BUCKET")).asAttempt
      })
      stage <- Attempt.fromOption(Properties.envOrNone("APP_STAGE"), {
        Failure("Couldn't read stage configuration", "Quack Stanley failed because it is missing configuration", 500, Some("APP_STAGE")).asAttempt
      })
      persistence = new S3(bucket)
    } yield Config(stage, persistence)
  }
}
