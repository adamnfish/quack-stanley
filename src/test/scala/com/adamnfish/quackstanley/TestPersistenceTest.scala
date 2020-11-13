package com.adamnfish.quackstanley

import io.circe.Json
import org.scalatest.{OptionValues}
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AnyFreeSpec


class TestPersistenceTest extends AnyFreeSpec with Matchers with OptionValues {
  val testConfig = new Config("test", "test", new TestPersistence)

  "test persistence" - {
    "saves value" in {
      val testDb = new TestPersistence
      val json = Json.fromInt(2)
      testDb.writeJson(json, "test/path", testConfig)
      testDb.data.get("test/path").value shouldEqual json
    }

    "retrieves value" in {
      val testDb = new TestPersistence
      val json = Json.fromInt(2)
      testDb.data.put("test/path", json)
      testDb.getJson("test/path", testConfig)
    }
  }
}
