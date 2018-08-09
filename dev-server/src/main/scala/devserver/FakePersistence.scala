package devserver

import com.adamnfish.quackstanley.Config
import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.persistence.Persistence
import io.circe.Json

import scala.collection.mutable.{Map => MutableMap}


class FakePersistence extends Persistence {
  private val data = MutableMap.empty[String, Json]

  override def getJson(path: String, config: Config): Attempt[Json] = {
    Attempt.fromOption(data.get(path),
      Failure("Game data not found", "Couldn't find your game", 500, Some(path), None).asAttempt
    )
  }

  override def writeJson(json: Json, path: String, config: Config): Attempt[Unit] = {
    Attempt.Right(data.put(path, json))
  }

  override def listFiles(path: String, config: Config): Attempt[List[String]] = {
    Attempt.Right {
      data.keys.toList.filter(_.startsWith(path))
    }
  }
}
