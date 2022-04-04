package com.adamnfish.quackstanley.models

import cats.data.EitherT
import com.adamnfish.quackstanley.attempt.{Attempt, Failure}

import scala.io.Source
import scala.util.control.NonFatal


object Resources {
  lazy val words: Attempt[List[Word]] = {
    try {
      EitherT.pure {
        Source.fromResource("words.txt").getLines()
          .filter(_.nonEmpty)
          .map(Word)
          .toList
      }
    } catch {
      case NonFatal(e) =>
        EitherT.leftT(
          Failure("Failed to open words resource file", "Couldn't load words", 500, Some("words"), Some(e)).asFailedAttempt
        )
    }
  }

  lazy val roles: Attempt[List[Role]] = {
    try {
      EitherT.pure {
        Source.fromResource("roles.txt").getLines()
          .filter(_.nonEmpty)
          .map(Role)
          .toList
      }
    } catch {
      case NonFatal(e) =>
        EitherT.leftT(
          Failure("Failed to open words resource file", "Couldn't load roles", 500, Some("roles"), Some(e)).asFailedAttempt
        )
    }
  }
}
