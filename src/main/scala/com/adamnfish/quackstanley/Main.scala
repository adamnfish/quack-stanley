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
        tmp <- parseBody[ApiOperation](in)
        (apiOperation, request) = tmp
        response <- dispatch(apiOperation, context: Context)
      } yield response
    }, out, context)
  }

  def dispatch(apiOperation: ApiOperation, context: Context): Attempt[ApiResponse] = {
    try {
      apiOperation match {
        case data: CreateGame => createGame(data)
        case data: RegisterPlayer => registerPlayer(data)
        case data: AwardPoint => awardPoint(data)
        case data: Mulligan => mulligan(data)
        case data: Ping => ping(data)
      }
    } catch {
      case NonFatal(e) =>
        context.getLogger.log(s"Fatal error: ${e.getMessage} ${e.getStackTrace.mkString("; ")}")
        Attempt.Left(Failure(e.getMessage, "Unexpected server error", 500).asAttempt)
    }
  }
}
