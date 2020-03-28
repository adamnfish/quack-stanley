package com.adamnfish.quackstanley.persistence

import com.adamnfish.quackstanley.Config
import com.adamnfish.quackstanley.attempt.Attempt
import io.circe.Json

trait Persistence {
  def getJson(path: String, config: Config): Attempt[Json]

  def writeJson(json: Json, path: String, config: Config): Attempt[Unit]

  def listFiles(path: String, config: Config): Attempt[List[String]]
}
