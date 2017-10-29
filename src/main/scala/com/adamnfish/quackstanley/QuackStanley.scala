package com.adamnfish.quackstanley

import com.adamnfish.quackstanley.attempt.Attempt
import com.adamnfish.quackstanley.models._


object QuackStanley {
  def createGame(data: CreateGame): Attempt[Registered] = {
    Attempt.Right(Registered(GameId(data.name), PlayerKey("player-key")))
  }

  def registerPlayer(data: RegisterPlayer): Attempt[Registered] = {
    Attempt.Right(Registered(GameId("game-id"), PlayerKey("player-key")))
  }

  def finishPitch(data: FinishPitch): Attempt[PlayerInfo] = {
    ???
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
