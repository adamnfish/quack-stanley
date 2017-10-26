package com.adamnfish.quackstanley

import com.adamnfish.quackstanley.attempt.Attempt
import com.adamnfish.quackstanley.models._


object QuackStanley {
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
