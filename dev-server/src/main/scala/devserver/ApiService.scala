package devserver

import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.attempt.LambdaIntegration.failureToJson
import com.adamnfish.quackstanley.models.Serialization._
import com.adamnfish.quackstanley.models.{ApiOperation, Serialization}
import com.adamnfish.quackstanley.{Config, Main}

import io.circe.parser._
import io.circe.syntax._
import io.circe._

import scala.concurrent.Future


object ApiService {
  implicit val ec = scala.concurrent.ExecutionContext.global

  val quackStanley = new Main
  val fakeContext = new FakeContext
  val fakeConfig = Config("", "", new FakePersistence)

  def devQuackStanley(body: String): Future[(Int, String)] = {
    (for {
      jsonBody <- Attempt.fromEither(parse(body).left.map { e =>
        Failure(e.getMessage(), "Failed to parse request", 400).asAttempt
      })
      apiOp <- Serialization.extractJson[ApiOperation](jsonBody)
      statusAndResponse <- quackStanley.dispatch(apiOp, fakeContext, fakeConfig)
    } yield statusAndResponse).asFuture.map {
      case Right(out) =>
        (200, out.asJson.noSpaces)
      case Left(failures) =>
        fakeContext.getLogger.log(s"Failure: ${failures.logString}")
        val body = Json.obj(
          "errors" -> Json.fromValues(failures.failures.map(failureToJson))
        )
        (failures.statusCode, body.noSpaces)
    }
  }
}
