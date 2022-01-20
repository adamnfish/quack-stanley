package com.adamnfish.quackstanley

import com.adamnfish.quackstanley.attempt.{Attempt, FailedAttempt}
import org.scalactic.source.Position
import org.scalatest.EitherValues
import org.scalatest.exceptions.TestFailedException
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._


trait AttemptValues extends EitherValues with RightValues with Matchers {
  implicit class RichAttmpt[A](attempt: Attempt[A]) {
    def value()(implicit ec: ExecutionContext, pos: Position): A = {
      val result = Await.result(attempt.asFuture, 5.seconds)
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

    def leftValue()(implicit ec: ExecutionContext, pos: Position): FailedAttempt = {
      val result = Await.result(attempt.asFuture, 5.seconds)
      withClue {
        result.fold(
          fa => s"${fa.logString}",
          _ => ""
        )
      } {
        result.left.value
      }
    }

    def isSuccessfulAttempt()(implicit ec: ExecutionContext, pos: Position): Boolean = {
      val result = Await.result(attempt.asFuture, 5.seconds)
      result.fold(
        fa => false,
        _ => true
      )
    }

    def isFailedAttempt()(implicit ec: ExecutionContext, pos: Position): Boolean = {
      !isSuccessfulAttempt()
    }
  }
}
