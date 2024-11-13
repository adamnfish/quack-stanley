package com.adamnfish.quackstanley

import cats.data.EitherT
import cats.effect.IO
import cats.effect.unsafe.IORuntime

import java.io._
import com.adamnfish.quackstanley.LambdaIntegration._
import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.aws.S3
import com.adamnfish.quackstanley.models.Serialization._
import com.adamnfish.quackstanley.models._
import com.amazonaws.services.lambda.runtime.Context

import scala.util.{Properties, Try}


class Main {
  implicit val runtime: IORuntime = IORuntime.global

  def handleRequest(in: InputStream, out: OutputStream, context: Context): Unit = {
    val allowedOrigin = Properties.envOrNone("ORIGIN_LOCATION")
    val result = Try {
      for {
        config <- configFromEnvironment()
        (apiOperation, _) <- parseBody[ApiOperation](in)
        response <- dispatch(apiOperation, config)
      } yield response
    }.fold(
      { e =>
        context.getLogger.log(s"Fatal error: ${e.getMessage} ${e.getStackTrace.mkString("; ")}")
        EitherT.leftT[IO, ApiResponse](Failure(e.getMessage, "Unexpected server error", 500).asFailedAttempt)
      },
      identity
    )
    respond[ApiResponse](result, out, context, allowedOrigin)
  }

  def configFromEnvironment(): Attempt[Config] = {
    for {
      bucket <- EitherT.fromOption[IO](Properties.envOrNone("APP_DATA_S3_BUCKET"), {
        Failure("Couldn't read S3 bucket name for configuration", "Quack Stanley failed because it is missing configuration", 500, Some("APP_DATA_S3_BUCKET")).asFailedAttempt
      })
      stage <- EitherT.fromOption[IO](Properties.envOrNone("APP_STAGE"), {
        Failure("Couldn't read stage configuration", "Quack Stanley failed because it is missing configuration", 500, Some("APP_STAGE")).asFailedAttempt
      })
      persistence = new S3(bucket)
    } yield Config(stage, persistence)
  }
}
