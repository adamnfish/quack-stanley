package devserver

import cats.data.EitherT
import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.persistence.Persistence
import io.circe.Json

import scala.collection.mutable.{Map => MutableMap}


class FakePersistence extends Persistence {
  private val data = MutableMap.empty[String, Json]

  override def getJson(path: String): Attempt[Json] = {
    EitherT.fromOption(data.get(path),
      Failure("Game data not found", "Couldn't find your game", 500, Some(path), None).asFailedAttempt
    )
  }

  override def writeJson(json: Json, path: String): Attempt[Unit] = {
    EitherT.pure(data.put(path, json))
  }

  override def listFiles(path: String): Attempt[List[String]] = {
    EitherT.pure {
      data.keys.toList.filter(_.startsWith(path))
    }
  }
}
