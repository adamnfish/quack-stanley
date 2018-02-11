package com.adamnfish.quackstanley.models

import com.adamnfish.quackstanley.attempt.{Attempt, FailedAttempt, Failure}


object Validation {
  type Validator[A] = (A, String) => List[Failure]

  val nonEmpty: Validator[String] = { (str, context) =>
    if (str.isEmpty) {
      List(
        Failure("Validation failure: empty", s"Invalid data $context was empty", 400, Some(context))
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

  def validate[A](as: (A, String)*)(validators: Validator[A]*): Attempt[Unit] = {
    val failures = as.flatMap { case (a, context) =>
      validators.flatMap(_(a, context))
    }
    if (failures.isEmpty) Attempt.Right(())
    else Attempt.Left(FailedAttempt(failures))
  }

  def validate2[A, B]
    (a: A, aContext: String, aValidators: Validator[A]*)
    (b: B, bContext: String, bValidators: Validator[B]*): Attempt[Unit] = {
    val failures = aValidators.flatMap(_(a, aContext)) ++ bValidators.flatMap(_(b, bContext))
    if (failures.isEmpty) Attempt.Right(())
    else Attempt.Left(FailedAttempt(failures))
  }
}
