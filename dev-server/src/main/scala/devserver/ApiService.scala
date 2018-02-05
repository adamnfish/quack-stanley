package devserver

import java.io.ByteArrayInputStream

import com.adamnfish.quackstanley.{Config, Main}
import com.adamnfish.quackstanley.attempt.Attempt
import com.adamnfish.quackstanley.attempt.LambdaIntegration.{failureToJson, parseBody, parseLambdaRequestBody}
import com.adamnfish.quackstanley.models.{ApiOperation, Serialization}
import com.adamnfish.quackstanley.models.Serialization._
import fs2.Task
import io.circe.Json
import io.circe.syntax._
import org.http4s.dsl.impl.EntityResponseGenerator
import org.http4s.dsl.{->, /, POST, Root}
import org.http4s.{HttpService, Status}

import scala.concurrent.Future


object ApiService {
  implicit val ec = scala.concurrent.ExecutionContext.global

  val quackStanley = new Main
  val fakeContext = new FakeContext
  val fakeConfig = Config("", "", new FakePersistence)

  def devQuackStanley(body: Json)(): Future[(Int, String)] = {
    (for {
      apiOp <- extract(body)
      statusAndResponse <- executeOperation(apiOp)
    } yield statusAndResponse).asFuture.map(_.left.map(fa => (fa.statusCode, fa.logString)).merge)
  }

  def extract(body: Json): Attempt[ApiOperation] = {
    for {
      a <- Serialization.extractJson[ApiOperation](body)
    } yield a
  }

  def executeOperation(apiOperation: ApiOperation): Attempt[(Int, String)] = {
    Attempt.Async.Right {
      quackStanley.dispatch(apiOperation, fakeContext, fakeConfig).asFuture.map {
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
}
