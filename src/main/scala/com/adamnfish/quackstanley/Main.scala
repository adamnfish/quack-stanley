package com.adamnfish.quackstanley

import java.io._

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.attempt.LambdaIntegration._
import com.adamnfish.quackstanley.models.Serialization._
import com.adamnfish.quackstanley.models._
import com.amazonaws.services.lambda.runtime.Context

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.NonFatal


class Main {
  def handleRequest(in: InputStream, out: OutputStream, context: Context): Unit = {
    respond[ApiResponse]({
      for {
        config <- Config.fromEnvironment()
        tmp <- parseBody[ApiOperation](in)
        (apiOperation, request) = tmp
        response <- dispatch(apiOperation, context: Context, config)
      } yield response
    }, out, context)
  }

  def dispatch(apiOperation: ApiOperation, context: Context, config: Config): Attempt[ApiResponse] = {
    try {
      apiOperation match {
        case data: CreateGame => createGame(data, config)
        case data: RegisterPlayer => registerPlayer(data, config)
        case data: FinishPitch => finishPitch(data, config)
        case data: AwardPoint => awardPoint(data, config)
        case data: Mulligan => mulligan(data, config)
        case data: Ping => ping(data, config)
      }
    } catch {
      case NonFatal(e) =>
        context.getLogger.log(s"Fatal error: ${e.getMessage} ${e.getStackTrace.mkString("; ")}")
        Attempt.Left(Failure(e.getMessage, "Unexpected server error", 500).asAttempt)
    }
  }
}
