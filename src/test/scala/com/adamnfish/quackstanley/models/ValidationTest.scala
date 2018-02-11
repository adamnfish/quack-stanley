package com.adamnfish.quackstanley.models

import java.util.UUID

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

  "isUUID" - {
    val uuid = UUID.randomUUID().toString

    "succeeds with a valid UUID" in {
      isUUID(uuid, "test") shouldBe empty
    }

    "fails with an empty string" in {
      isUUID("", "empty test") should have length 1
    }

    "fails with provided context for empty string" in {
      isUUID("", "empty test").head.context.value shouldEqual "empty test"
    }

    "fails with a non-UUID" in {
      isUUID("not a UUID", "format test") should have length 1
    }

    "fails with provided context for non-UUID" in {
      isUUID("not a UUID", "format test").head.context.value shouldEqual "format test"
    }
  }

  "validate" - {
    "for a single validation" - {
      "returns success if the input passes validation" in {
        validate("input" -> "context")(nonEmpty).isSuccessfulAttempt() shouldBe true
      }

      "returns failure if the input does not pass validation" in {
        validate("" -> "context")(nonEmpty).isFailedAttempt() shouldBe true
      }
    }

    "can check multiple properties at once" - {
      "returns success if both inputs pass" in {
        val result = validate("input1" -> "context1", "input2" -> "context2")(nonEmpty)
        result.isSuccessfulAttempt() shouldBe true
      }
    }
  }

  "validate2" - {
    "returns success if both the inputs pass validation" in {
      val result = validate2("input1", "context1", nonEmpty)("input2", "context2", nonEmpty)
      result.isSuccessfulAttempt() shouldBe true
    }

    "returns failure if the first input fails validation" in {
      val result = validate2("", "context1", nonEmpty)("input2", "context2", nonEmpty)
      result.leftValue().failures should have length 1
    }

    "returns first context in failure if the first input fails validation" in {
      val failure = validate2("", "context1", nonEmpty)("input2", "context2", nonEmpty).leftValue()
      failure.failures.head.context.value shouldEqual "context1"
    }

    "returns single failure if the second input fails validation" in {
      val result = validate2("input1", "context1", nonEmpty)("", "context2", nonEmpty)
      result.leftValue().failures should have length 1
    }

    "returns second context in failure if the second input fails validation" in {
      val failure = validate2("input1", "context1", nonEmpty)("", "context2", nonEmpty).leftValue()
      failure.failures.head.context.value shouldEqual "context2"
    }

    "returns failures if both inputs fails validation" in {
      val result = validate2("", "context1", nonEmpty)("", "context2", nonEmpty)
      result.leftValue().failures should have length 2
    }

    "returns both contexts in failure if the both inputs fails validation" in {
      val failure = validate2("", "context1", nonEmpty)("", "context2", nonEmpty).leftValue()
      val contexts = failure.failures.map(_.context)
      contexts shouldEqual List(Some("context1"), Some("context2"))
    }
  }
}
