package com.adamnfish.quackstanley

import com.adamnfish.quackstanley.attempt.{Attempt, FailedAttempt}
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._


trait AttemptValues extends EitherValues with RightValues with Matchers {
  implicit class RichAttmpt[A](attempt: Attempt[A]) {
    def value()(implicit ec: ExecutionContext): A = {
      val result = Await.result(attempt.asFuture, 5.seconds)
      withClue {
        result.fold(
          fa => s"${fa.logString}",
          _ => ""
        )
      } {
        result.value
      }
    }

    def leftValue()(implicit ec: ExecutionContext): FailedAttempt = {
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

    def isSuccessfulAttempt()(implicit ec: ExecutionContext): Boolean = {
      val result = Await.result(attempt.asFuture, 5.seconds)
      result.fold(
        fa => false,
        _ => true
      )
    }

    def isFailedAttempt()(implicit ec: ExecutionContext): Boolean = {
      !isSuccessfulAttempt()
    }
  }
}
