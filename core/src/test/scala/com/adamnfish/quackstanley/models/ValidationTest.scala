package com.adamnfish.quackstanley.models

import com.adamnfish.quackstanley.AttemptValues
import com.adamnfish.quackstanley.models.Validation._
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.util.UUID


class ValidationTest extends AnyFreeSpec with Matchers with AttemptValues with OptionValues {
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

  "isUUIDPrefix" - {
    val uuid = UUID.randomUUID().toString

    "succeeds with valid short prefix" in {
      gameCode(uuid.take(4), "test") shouldBe empty
    }

    "succeeds with valid longer prefix" in {
      gameCode(uuid.take(8), "test") shouldBe empty
    }

    "succeeds with entire ID" in {
      gameCode(uuid, "test") shouldBe empty
    }

    "with empty input" - {
      val input = ""

      "returns failure" in {
        gameCode(input, "empty test") should have length 1
      }

      "fails with provided context" in {
        isUUID(input, "empty test").head.context.value shouldEqual "empty test"
      }
    }

    "with bad chars" - {
      val input = "wxyz"

      "returns failure" in {
        gameCode(input, "format test") should have length 1
      }

      "fails with provided context" in {
        isUUID(input, "format test").head.context.value shouldEqual "format test"
      }
    }

    "fails with misplaced hyphen" in {
      isUUID("-abc", "format test") should have length 1
    }

    "fails with missing hyphen" in {
      isUUID("123456780", "format test") should have length 1
    }
  }

  "minLength" - {
    "fails for short input" in {
      minLength(4)("a", "test") should have length 1
    }

    "fails for empty input" in {
      minLength(4)("", "test") should have length 1
    }

    "succeeds at minimum length" in {
      minLength(4)("1234", "test") shouldBe empty
    }

    "succeeds for long enough input" in {
      minLength(4)("12345678", "test") shouldBe empty
    }
  }

  "combineFailures" - {
    "for a single validation" - {
      "returns success if the input passes validation" in {
        combineFailures(nonEmpty("input", "context")).isSuccessfulAttempt()
      }

      "returns failure if the input does not pass validation" in {
        combineFailures(nonEmpty("", "context")).isFailedAttempt()
      }
    }

    "combines provided failures" - {
      "returns success if both validators pass" in {
        val result = combineFailures(
          nonEmpty("input1", "context1"),
          nonEmpty("input2", "context2"),
        )
        result.isSuccessfulAttempt()
      }

      "if both validators fail" - {
        val result = combineFailures(
          nonEmpty("", "context1"),
          nonEmpty("", "context2")
        )

        "returns failure" in {
          result.isFailedAttempt()
        }

        "contains all failures" in {
          result.leftValue().failures should have size 2
        }

        "contains both contexts" in {
          result.leftValue().failures.map(_.context) shouldEqual List(Some("context1"), Some("context2"))
        }
      }

      "if the first validator fails" - {
        val result = combineFailures(
          nonEmpty("", "context1"),
          nonEmpty("not empty", "context2")
        )

        "returns failure" in {
          result.isFailedAttempt()
        }

        "contains all failures" in {
          result.leftValue().failures should have size 1
        }

        "contains both contexts" in {
          result.leftValue().failures.map(_.context) shouldEqual List(Some("context1"))
        }
      }

      "if the second validator fails" - {
        val result = combineFailures(
          nonEmpty("not empty", "context1"),
          nonEmpty("", "context2")
        )

        "returns failure" in {
          result.isFailedAttempt()
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

  "validateRegisterPlayer" - {
    "fails if input length is < 4" in {
      validate(RegisterPlayer("123", "screen name")).isFailedAttempt()
    }

    "is not case sensitive" in {
      validate(RegisterPlayer("ABCD", "screen name")).isSuccessfulAttempt()
    }

    "does not how duplicate errors for empty game code" in {
      val result = validate(RegisterPlayer("", "screen name")).leftValue()
      result.failures.filter(_.context.contains("game code")) should have size 1
    }
  }
}
