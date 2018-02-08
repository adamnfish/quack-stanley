package com.adamnfish.quackstanley.models

import com.adamnfish.quackstanley.AttemptValues
import com.adamnfish.quackstanley.models.Validation._
import org.scalatest.{FreeSpec, Matchers, OptionValues}

import scala.concurrent.ExecutionContext.Implicits.global


class ValidationTest extends FreeSpec with Matchers with AttemptValues with OptionValues {
  "nonEmpty" - {
    "returns no errors if the input is not empty" in {
      nonEmpty("non-empty", "context") shouldBe empty
    }

    "fails if provided input is empty" in {
      nonEmpty("", "context") should have length 1
    }

    "uses the provided context in case of a failure" in {
      nonEmpty("", "context").head.context.value shouldEqual "context"
    }
  }

  "validate" - {
    "returns success if the input passes validation" in {
      validate("input", "context", nonEmpty).isSuccessfulAttempt() shouldBe true
    }

    "returns failure if the input does not pass validation" in {
      validate("", "context", nonEmpty).isFailedAttempt() shouldBe true
    }
  }
}
