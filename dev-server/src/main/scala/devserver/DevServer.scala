package devserver

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import devserver.ApiService.devQuackStanley
import io.circe.Json
import lol.http._
import lol.json._


object DevServer extends LazyLogging {
  implicit val ec = scala.concurrent.ExecutionContext.global

  def main(args: Array[String]): Unit = {
    val frontendClient = Client("localhost", 3000)

    Server.listen(9001) {
      case request @ POST at url"/api" =>
        for {
          json <- request.readAs[Json]
          statusAndResponse <- IO.fromFuture(IO(devQuackStanley(json)))
          (statusCode, responseBody) = statusAndResponse
        } yield Response(statusCode, Content.of(responseBody))

      case request =>
        frontendClient(request)
    }
  }
}
