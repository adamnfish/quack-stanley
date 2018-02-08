package com.adamnfish.quackstanley.models

import com.adamnfish.quackstanley.attempt.{Attempt, FailedAttempt, Failure}


object Validation {
  type Validator[A] = (A, String) => List[Failure]

  val nonEmpty: Validator[String] = { (str, context) =>
    if (str.isEmpty) {
      List(
        Failure("Validation failure, got empty string", s"Invalid data $context was empty", 400, Some(context))
      )
    } else Nil
  }

  def validate[A](a: A, context: String, validators: Validator[A]*): Attempt[Unit] = {
    val failures = validators.flatMap(_(a, context))
    if (failures.isEmpty) Attempt.Right(())
    else Attempt.Left(FailedAttempt(failures))
  }
}
