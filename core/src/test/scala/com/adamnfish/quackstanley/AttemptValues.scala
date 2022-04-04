package com.adamnfish.quackstanley

import cats.effect.unsafe.IORuntime
import com.adamnfish.quackstanley.attempt.{Attempt, FailedAttempt}
import org.scalactic.source.Position
import org.scalatest.{Assertion, EitherValues, Failed, Succeeded}
import org.scalatest.exceptions.TestFailedException
import org.scalatest.matchers.should.Matchers


trait AttemptValues extends EitherValues with RightValues with Matchers {
  implicit val runtime: IORuntime = IORuntime.global

  implicit class RichAttmpt[A](attempt: Attempt[A]) {
    def run()(implicit pos: Position): A = {
      val result = attempt.value.unsafeRunSync()
      withClue {
        result.fold(
          fa => s"${fa.logString}",
          _ => ""
        )
      } {
        result match {
          case Right(value) => value
          case Left(fa) =>
            throw new TestFailedException(
              _ => Some(s"Expected successful attempt, got failure: ${fa.logString}"),
              None, pos
            )
        }
      }
    }

    def leftValue()(implicit pos: Position): FailedAttempt = {
      val result = attempt.value.unsafeRunSync()
      withClue {
        result.fold(
          fa => s"${fa.logString}",
          _ => ""
        )
      } {
        result.left.value
      }
    }

    def isSuccessfulAttempt()(implicit pos: Position): Assertion = {
      attempt.value.unsafeRunSync().fold(
        { fa =>
          Failed(s"Expected successful attempt, got failures: ${fa.logString}").toSucceeded
        },
        { _ =>
          Succeeded
        }
      )
    }

    def isFailedAttempt()(implicit pos: Position): Assertion = {
      attempt.value.unsafeRunSync().fold(
        { _ =>
          Succeeded
        },
        { a =>
          Failed(s"Expected failed attempt, got successful value: $a").toSucceeded
        }
      )
    }
  }
}
