package com.adamnfish.quackstanley

import org.scalactic.source
import org.scalactic.source.Position
import org.scalatest.exceptions.TestFailedException
import org.scalatest.matchers.HavePropertyMatcher
import org.scalatest.matchers.should.Matchers


trait HaveMatchers extends Matchers {
  def having[A](propertyName: String, propertyValue: A): HavePropertyMatcher[AnyRef, Any] = {
    Symbol(propertyName) (propertyValue)
  }

  implicit class HavingTestHelperString(propertyName: String) {
    def as[A](propertyValue: A): HavePropertyMatcher[AnyRef, Any] = {
      Symbol(propertyName) (propertyValue)
    }
  }
}

trait RightValues {
  implicit class RichEither[L, R](e: Either[L, R]) {
    def value(implicit pos: Position): R = {
      e.fold(
        { l =>
          throw new TestFailedException(
            _ => Some(s"The Either on which value was invoked was not a Right, got Left($l)"),
            None, pos
          )
        },
        identity
      )
    }
  }
}
