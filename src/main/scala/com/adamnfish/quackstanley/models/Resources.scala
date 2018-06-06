package com.adamnfish.quackstanley.models

import com.adamnfish.quackstanley.attempt.{Attempt, Failure}

import scala.io.Source
import scala.util.control.NonFatal


object Resources {
  def words(): Attempt[List[Word]] = {
    try {
      Attempt.Right {
        Source.fromResource("words.txt").getLines()
          .filter(_.nonEmpty)
          .map(Word)
          .toList
      }
    } catch {
      case NonFatal(e) =>
        Attempt.Left(
          Failure("Failed to open words resource file", "Couldn't load words", 500, Some("words"), Some(e)).asAttempt
        )
    }
  }

  def roles(): Attempt[List[Role]] = {
    try {
      Attempt.Right {
        Source.fromResource("roles.txt").getLines()
          .filter(_.nonEmpty)
          .map(Role)
          .toList
      }
    } catch {
      case NonFatal(e) =>
        Attempt.Left(
          Failure("Failed to open words resource file", "Couldn't load roles", 500, Some("roles"), Some(e)).asAttempt
        )
    }
  }
}
