package com.adamnfish.quackstanley.integration

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.models._
import com.adamnfish.quackstanley.{AttemptValues, Config, TestPersistence}
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}

import scala.concurrent.ExecutionContext.Implicits.global


class WakeIntegrationTest extends FreeSpec with Matchers with OneInstancePerTest with AttemptValues {
  val persistence = new TestPersistence
  val testConfig = Config("test", "test", persistence)

  "wake" - {
    "returns ok" in {
      val data = Wake()
      val response = wake(data, testConfig).value()
      response.status shouldEqual "ok"
    }
  }
}
