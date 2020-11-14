package com.adamnfish.quackstanley.persistence

import com.adamnfish.quackstanley.attempt.Attempt
import io.circe.Json


trait Persistence {
  def getJson(path: String): Attempt[Json]

  def writeJson(json: Json, path: String): Attempt[Unit]

  def listFiles(path: String): Attempt[List[String]]
}
