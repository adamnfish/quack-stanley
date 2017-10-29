package com.adamnfish.quackstanley

import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.models.Serialization._
import com.adamnfish.quackstanley.Logic._
import com.adamnfish.quackstanley.aws.S3._

import scala.concurrent.ExecutionContext


object QuackStanley {
  /**
    * Creates a new game and automatically registers this player as the creator.
    */
  def createGame(data: CreateGame, config: Config)(implicit ec: ExecutionContext): Attempt[Registered] = {
    val gameState = newGame(data.gameName, data.screenName)
    val playerKey = gameState.creator
    for {
      playerState <- Attempt.fromOption(gameState.players.get(playerKey),
        Failure("Error creating game, could not find creator in player map", "Error creating game", 500).asAttempt
      )
      _ <- writeGameState(gameState, config)
      _ <- writePlayerState(playerState, playerKey, config)
    } yield Registered(playerState, gameState.creator)
  }

  def registerPlayer(data: RegisterPlayer, config: Config)(implicit ec: ExecutionContext): Attempt[Registered] = {
    ???
  }

  def finishPitch(data: FinishPitch, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
    ???
  }

  def awardPoint(data: AwardPoint, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
    ???
  }

  def mulligan(data: Mulligan, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
    ???
  }

  def ping(data: Ping, config: Config)(implicit ec: ExecutionContext): Attempt[PlayerInfo] = {
    ???
  }
}
