package com.adamnfish.quackstanley

import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.persistence.Persistence
import io.circe.Json

import scala.collection.mutable.{Map => MutableMap}


class TestPersistence extends Persistence {
  private[quackstanley] val data = MutableMap.empty[String, Json]

  override def getJson(path: String): Attempt[Json] = {
    Attempt.fromOption(data.get(path),
      Failure("Test data not found", "Test data not found", 500, Some(path), None).asAttempt
    )
  }

  override def writeJson(json: Json, path: String): Attempt[Unit] = {
    Attempt.Right(data.put(path, json))
  }

  override def listFiles(path: String): Attempt[List[String]] = {
    Attempt.Right {
      data.keys.toList.filter(_.startsWith(path))
    }
  }
}
