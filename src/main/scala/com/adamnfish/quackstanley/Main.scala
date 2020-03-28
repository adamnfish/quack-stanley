package com.adamnfish.quackstanley

import java.io._

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.attempt.LambdaIntegration._
import com.adamnfish.quackstanley.models.Serialization._
import com.adamnfish.quackstanley.models._
import com.amazonaws.services.lambda.runtime.Context

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Properties
import scala.util.control.NonFatal


class Main {
  def handleRequest(in: InputStream, out: OutputStream, context: Context): Unit = {
    val allowedOrigin = Properties.envOrNone("ORIGIN_LOCATION")
    respond[ApiResponse]({
      for {
        config <- Config.fromEnvironment()
        opAndRequest <- parseBody[ApiOperation](in)
        (apiOperation, _) = opAndRequest
        response <- dispatch(apiOperation, context, config)
      } yield response
    }, out, context, allowedOrigin)
  }

  def dispatch(apiOperation: ApiOperation, context: Context, config: Config): Attempt[ApiResponse] = {
    try {
      apiOperation match {
        case data: CreateGame => createGame(data, config)
        case data: RegisterPlayer => registerPlayer(data, config)
        case data: StartGame => startGame(data, config)
        case data: BecomeBuyer => becomeBuyer(data, config)
        case data: RelinquishBuyer => relinquishBuyer(data, config)
        case data: StartPitch => startPitch(data, config)
        case data: FinishPitch => finishPitch(data, config)
        case data: AwardPoint => awardPoint(data, config)
        case data: Mulligan => mulligan(data, config)
        case data: Ping => ping(data, config)
        case data: LobbyPing => lobbyPing(data, config)
        case data: Wake => wake(data, config)
      }
    } catch {
      case NonFatal(e) =>
        context.getLogger.log(s"Fatal error: ${e.getMessage} ${e.getStackTrace.mkString("; ")}")
        Attempt.Left(Failure(e.getMessage, "Unexpected server error", 500).asAttempt)
    }
  }
}
