package com.adamnfish.quackstanley.models

import com.adamnfish.quackstanley.AttemptValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AnyFreeSpec

import scala.concurrent.ExecutionContext.Implicits.global


class ResourcesTest extends AnyFreeSpec with Matchers with AttemptValues {
  "roles" - {
    "filters empty lines" in {
      Resources.roles.value().filter(_.value.isEmpty) shouldBe empty
    }

    "contains list of words" in (
      Resources.roles.value().nonEmpty shouldEqual true
    )
  }

  "words" - {
    "filters empty lines" in {
      Resources.words.value().filter(_.value.isEmpty) shouldBe empty
    }

    "contains list of words" in (
      Resources.words.value().nonEmpty shouldEqual true
    )
  }
}
