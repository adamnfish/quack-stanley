package devserver

import cats.data.EitherT
import cats.effect.IO
import com.adamnfish.quackstanley.QuackStanley
import com.adamnfish.quackstanley.attempt.Failure
import com.adamnfish.quackstanley.models.Serialization._
import com.adamnfish.quackstanley.models.{ApiOperation, Config, Serialization}
import com.typesafe.scalalogging.LazyLogging
import io.circe._
import io.circe.parser._
import io.circe.syntax._


object ApiService extends LazyLogging {
  val fakeConfig = Config("dev", new FakePersistence)

  def devQuackStanley(body: String): IO[(Int, String)] = {
    (for {
      jsonBody <- EitherT.fromEither[IO](parse(body).left.map { e =>
        Failure(e.getMessage(), "Failed to parse request", 400).asFailedAttempt
      })
      apiOp <- Serialization.extractJson[ApiOperation](jsonBody)
      statusAndResponse <- QuackStanley.dispatch(apiOp, fakeConfig)
    } yield statusAndResponse).value.map {
      case Right(out) =>
        (200, out.asJson.noSpaces)
      case Left(failures) =>
        logger.info(s"Failure: ${failures.logString}")
        val body = Json.obj(
          "errors" -> Json.fromValues(failures.failures.map(failureToJson))
        )
        (failures.statusCode, body.noSpaces)
    }
  }
}
