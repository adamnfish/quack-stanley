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
        validate("input", "context", nonEmpty).isSuccessfulAttempt() shouldBe true
      }

      "returns failure if the input does not pass validation" in {
        validate("", "context", nonEmpty).isFailedAttempt() shouldBe true
      }
    }

    "can be combined with |@|" - {
      "returns success if both validators pass" in {
        val result = validate("input1", "context1", nonEmpty) |@| validate("input2", "context2", nonEmpty)
        result.isSuccessfulAttempt() shouldBe true
      }

      "if both validators fail" - {
        val result = validate("", "context1", nonEmpty) |@| validate("", "context2", nonEmpty)

        "returns failure" in {
          result.isFailedAttempt() shouldBe true
        }

        "contains all failures" in {
          result.leftValue().failures should have size 2
        }

        "contains both contexts" in {
          result.leftValue().failures.map(_.context) shouldEqual List(Some("context1"), Some("context2"))
        }
      }

      "if the first validator fails" - {
        val result = validate("", "context1", nonEmpty) |@| validate("not empty", "context2", nonEmpty)

        "returns failure" in {
          result.isFailedAttempt() shouldBe true
        }

        "contains all failures" in {
          result.leftValue().failures should have size 1
        }

        "contains both contexts" in {
          result.leftValue().failures.map(_.context) shouldEqual List(Some("context1"))
        }
      }

      "if the second validator fails" - {
        val result = validate("not empty", "context1", nonEmpty) |@| validate("", "context2", nonEmpty)

        "returns failure" in {
          result.isFailedAttempt() shouldBe true
        }

        "contains all failures" in {
          result.leftValue().failures should have size 1
        }

        "contains both contexts" in {
          result.leftValue().failures.map(_.context) shouldEqual List(Some("context2"))
        }
      }
    }
  }
}
