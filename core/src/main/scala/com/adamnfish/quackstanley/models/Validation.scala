package com.adamnfish.quackstanley.models

import cats.data.EitherT
import com.adamnfish.quackstanley.attempt.{Attempt, FailedAttempt, Failure}


object Validation {
  type Validator[A] = (A, String) => List[Failure]

  private[models] def combineFailures(fss: List[Failure]*): Attempt[Unit] = {
    val failures = fss.flatten.toList
    if (failures.isEmpty) EitherT.pure(())
    else EitherT.leftT(FailedAttempt(fss.flatten.toList))
  }

  val nonEmpty: Validator[String] = { (iter, context) =>
    if (iter.isEmpty) {
      List(
        Failure("Validation failure: empty", s"$context is required", 400, Some(context))
      )
    } else Nil
  }

  private val uuidPattern = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
  val isUUID: Validator[String] = { (str, context) =>
    val wasEmpty = nonEmpty(str, context).headOption
    val wasUUID =
      if (uuidPattern.pattern.matcher(str).matches) {
        None
      } else {
        Some(
          Failure(s"Validation failure: $str not UUID", s"$context was not in the correct format", 400, Some(context))
        )
      }
    wasEmpty.orElse(wasUUID).toList
  }

  /**
    * Game codes are a case-insensitive UUID prefix
    */
  val gameCode: Validator[String] = { (str, context) =>
    val wasEmpty = nonEmpty(str, context).headOption
    val ValidChar = "([0-9a-fA-F\\-])".r
    val valid = str.zipWithIndex.forall {
      case (ValidChar(c), i) =>
        if (i == 8 || i == 13 || i == 18 || i == 23) {
          c == '-'
        } else true
      case _ =>
        false
    }
    val wasUUIDPrefix =
      if (valid) None
      else Some(Failure(s"$str is not a UUID prefix", "Invalid game code", 400, Some(context)))
    wasEmpty.orElse(wasUUIDPrefix).toList
  }

  def minLength(min: Int): Validator[String] = { (str, context) =>
    if (str.length < min)
      List(
        Failure("Failed min length", s"$context must be at least $min characters", 400, Some(context))
      )
    else Nil
  }

  def validate(createGame: CreateGame): Attempt[Unit] = {
    combineFailures(
      nonEmpty(createGame.gameName, "game name"),
      nonEmpty(createGame.screenName, "screen name"),
    )
  }

  def validate(registerPlayer: RegisterPlayer): Attempt[Unit] = {
    val gameCodeFailures =
      gameCode(registerPlayer.gameCode, "game code") ++ minLength(4)(registerPlayer.gameCode, "game code")

    combineFailures(
      gameCodeFailures.headOption.toList,
      nonEmpty(registerPlayer.screenName, "screen name"),
    )
  }

  def validate(startGame: StartGame): Attempt[Unit] = {
    combineFailures(
      isUUID(startGame.gameId.value, "game ID"),
      isUUID(startGame.playerKey.value, "player key"),
    )
  }

  def validate(becomeBuyer: BecomeBuyer): Attempt[Unit] = {
    combineFailures(
      isUUID(becomeBuyer.gameId.value, "game ID"),
      isUUID(becomeBuyer.playerKey.value, "player key"),
    )
  }

  def validate(relinquishBuyer: RelinquishBuyer): Attempt[Unit] = {
    combineFailures(
      isUUID(relinquishBuyer.gameId.value, "game ID"),
      isUUID(relinquishBuyer.playerKey.value, "player key"),
    )
  }

  def validate(startPitch: StartPitch): Attempt[Unit] = {
    combineFailures(
      isUUID(startPitch.gameId.value, "game ID"),
      isUUID(startPitch.playerKey.value, "player key"),
    )
  }

  def validate(finishPitch: FinishPitch): Attempt[Unit] = {
    combineFailures(
      isUUID(finishPitch.gameId.value, "game ID"),
      isUUID(finishPitch.playerKey.value, "player key"),
      nonEmpty(finishPitch.words._1.value, "first word"),
      nonEmpty(finishPitch.words._2.value, "second word"),
    )
  }

  def validate(awardPoint: AwardPoint): Attempt[Unit] = {
    combineFailures(
      isUUID(awardPoint.gameId.value, "game ID"),
      isUUID(awardPoint.playerKey.value, "player key"),
      nonEmpty(awardPoint.role.value, "role"),
      nonEmpty(awardPoint.awardToPlayerWithName, "winning player"),
    )
  }

  def validate(ping: Ping): Attempt[Unit] = {
    combineFailures(
      isUUID(ping.gameId.value, "game ID"),
      isUUID(ping.playerKey.value, "player key"),
    )
  }

  def validate(lobbyPing: LobbyPing): Attempt[Unit] = {
    combineFailures(
      isUUID(lobbyPing.gameId.value, "game ID"),
      isUUID(lobbyPing.playerKey.value, "player key"),
    )
  }

  def validate(mulligan: Mulligan): Attempt[Unit] = {
    combineFailures(
      isUUID(mulligan.gameId.value, "game ID"),
      isUUID(mulligan.playerKey.value, "player key"),
      nonEmpty(mulligan.role.value, "role")
    )
  }
}
