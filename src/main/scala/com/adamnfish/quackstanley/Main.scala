package com.adamnfish.quackstanley

import java.io._

import com.adamnfish.quackstanley.attempt.Attempt
import com.adamnfish.quackstanley.attempt.LambdaIntegration._
import com.amazonaws.services.lambda.runtime.Context

import scala.concurrent.ExecutionContext.Implicits.global


class Main {
  def handleRequest[ApiOperation, ApiResponse](in: InputStream, out: OutputStream, context: Context): Unit = {
    for {
      (apiOperation, request) <- parseBody[ApiOperation](in)
    } yield 1

//    bodyAction(in, out, context) { (apiOperation: ApiOperation, lambdaRequest, context) =>
//      apiOperation match {
//        case data: CreateGame => createGame(data)
//        case data: RegisterPlayer => registerPlayer(data)
//        case data: AwardPoint => awardPoint(data)
//        case data: Mulligan => mulligan(data)
//        case data: Ping => ping(data)
//      }
//    }
  }

  def createGame(data: CreateGame): Attempt[Registered] = {
    Attempt.Right(Registered("game-id", PlayerKey("player-key")))
  }

  def registerPlayer(data: RegisterPlayer): Attempt[Registered] = {
    Attempt.Right(Registered("game-id", PlayerKey("player-key")))
  }

  def awardPoint(data: AwardPoint): Attempt[PlayerInfo] = {
    ???
  }

  def mulligan(data: Mulligan): Attempt[PlayerInfo] = {
    ???
  }

  def ping(data: Ping): Attempt[PlayerInfo] = {
    ???
  }

}
