package com.adamnfish.quackstanley.models

import com.adamnfish.quackstanley.AttemptValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers


class ResourcesTest extends AnyFreeSpec with Matchers with AttemptValues {
  "roles" - {
    "filters empty lines" in {
      Resources.roles.run().filter(_.value.isEmpty) shouldBe empty
    }

    "contains list of words" in (
      Resources.roles.run().nonEmpty shouldEqual true
    )
  }

  "words" - {
    "filters empty lines" in {
      Resources.words.run().filter(_.value.isEmpty) shouldBe empty
    }

    "contains list of words" in (
      Resources.words.run().nonEmpty shouldEqual true
    )
  }
}
