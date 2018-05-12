package devserver

import com.adamnfish.quackstanley.attempt.LambdaIntegration.failureToJson
import com.adamnfish.quackstanley.models.Serialization._
import com.adamnfish.quackstanley.models.{ApiOperation, Serialization}
import com.adamnfish.quackstanley.{Config, Main}
import io.circe.Json
import io.circe.syntax._

import scala.concurrent.Future


object ApiService {
  implicit val ec = scala.concurrent.ExecutionContext.global

  val quackStanley = new Main
  val fakeContext = new FakeContext
  val fakeConfig = Config("", "", new FakePersistence)

  def devQuackStanley(body: Json): Future[(Int, String)] = {
    (for {
      apiOp <- Serialization.extractJson[ApiOperation](body)
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
