package com.adamnfish.quackstanley.models

import com.adamnfish.quackstanley.attempt.{Attempt, FailedAttempt, Failure}

import scala.concurrent.ExecutionContext


object Validation {
  type Validator[A] = (A, String) => List[Failure]

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

  private[models] def validate[A](a: A, context: String, validator: Validator[A]): Attempt[Unit] = {
    val failures = validator(a, context)
    if (failures.isEmpty) Attempt.unit
    else Attempt.Left(FailedAttempt(failures))
  }

  def validate(createGame: CreateGame)(implicit ec: ExecutionContext): Attempt[Unit] = {
    validate(createGame.gameName, "game name", nonEmpty) |@|
      validate(createGame.screenName, "screen name", nonEmpty)
  }

  def validate(registerPlayer: RegisterPlayer)(implicit ec: ExecutionContext): Attempt[Unit] = {
    val gameCodeFailures = validate(registerPlayer.gameCode, "game code", gameCode) |@|
      validate(registerPlayer.gameCode, "game code", minLength(4))

    gameCodeFailures.firstFailure() |@|
      validate(registerPlayer.screenName, "screen name", nonEmpty)
  }

  def validate(startGame: StartGame)(implicit ec: ExecutionContext): Attempt[Unit] = {
    validate(startGame.gameId.value, "game ID", isUUID) |@|
      validate(startGame.playerKey.value, "player key", isUUID)
  }

  def validate(becomeBuyer: BecomeBuyer)(implicit ec: ExecutionContext): Attempt[Unit] = {
    validate(becomeBuyer.gameId.value, "game ID", isUUID) |@|
      validate(becomeBuyer.playerKey.value, "player key", isUUID)
  }

  def validate(relinquishBuyer: RelinquishBuyer)(implicit ec: ExecutionContext): Attempt[Unit] = {
    validate(relinquishBuyer.gameId.value, "game ID", isUUID) |@|
      validate(relinquishBuyer.playerKey.value, "player key", isUUID)
  }

  def validate(startPitch: StartPitch)(implicit ec: ExecutionContext): Attempt[Unit] = {
    validate(startPitch.gameId.value, "game ID", isUUID) |@|
      validate(startPitch.playerKey.value, "player key", isUUID)
  }

  def validate(finishPitch: FinishPitch)(implicit ec: ExecutionContext): Attempt[Unit] = {
    validate(finishPitch.gameId.value, "game ID", isUUID) |@|
      validate(finishPitch.playerKey.value, "player key", isUUID) |@|
      validate(finishPitch.words._1.value, "first word", nonEmpty) |@|
      validate(finishPitch.words._2.value, "second word", nonEmpty)
  }

  def validate(awardPoint: AwardPoint)(implicit ec: ExecutionContext): Attempt[Unit] = {
    validate(awardPoint.gameId.value, "game ID", isUUID) |@|
      validate(awardPoint.playerKey.value, "player key", isUUID) |@|
      validate(awardPoint.role.value, "role", nonEmpty) |@|
      validate(awardPoint.awardToPlayerWithName, "winning player", nonEmpty)
  }

  def validate(ping: Ping)(implicit ec: ExecutionContext): Attempt[Unit] = {
    validate(ping.gameId.value, "game ID", isUUID) |@|
      validate(ping.playerKey.value, "player key", isUUID)
  }

  def validate(lobbyPing: LobbyPing)(implicit ec: ExecutionContext): Attempt[Unit] = {
    validate(lobbyPing.gameId.value, "game ID", isUUID) |@|
      validate(lobbyPing.playerKey.value, "player key", isUUID)
  }

  def validate(mulligan: Mulligan)(implicit ec: ExecutionContext): Attempt[Unit] = {
    validate(mulligan.gameId.value, "game ID", isUUID) |@|
      validate(mulligan.playerKey.value, "player key", isUUID) |@|
      validate(mulligan.role.value, "role", nonEmpty)
  }
}
