package com.adamnfish.quackstanley.integration

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.{AttemptValues, TestPersistence}
import org.scalatest.{OneInstancePerTest}
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AnyFreeSpec

import scala.concurrent.ExecutionContext.Implicits.global


class WakeIntegrationTest extends AnyFreeSpec with Matchers with OneInstancePerTest with AttemptValues {
  val persistence = new TestPersistence
  val testConfig = Config("test", persistence)

  "wake" - {
    "returns ok" in {
      val data = Wake()
      val response = wake(data, testConfig).value()
      response.status shouldEqual "ok"
    }
  }
}
