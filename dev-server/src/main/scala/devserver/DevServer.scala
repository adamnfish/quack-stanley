package devserver

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import devserver.ApiService.devQuackStanley
import io.circe.Json
import lol.http._
import lol.json._
import concurrent.duration._


object DevServer extends LazyLogging {
  implicit val ec = scala.concurrent.ExecutionContext.global

  def main(args: Array[String]): Unit = {
    Server.listen(9001) {
      // CORS
      case _ @ HttpMethod("OPTIONS") at url"/api" =>
        Response(
          204, Content.empty,
          headers = Map(
            h"content-type" -> h"application/json",
            h"Access-Control-Allow-Origin" -> h"*",
            h"Access-Control-Allow-Methods" -> h"POST",
            h"Access-Control-Allow-Headers" -> h"Content-Type"
          )
        )

      // API
      case request @ POST at url"/api" =>
        for {
          json <- request.readAs[Json]
          statusAndResponse <- IO.fromFuture(IO(devQuackStanley(json)))
          _ <- IO.sleep(2.seconds)
          (statusCode, responseBody) = statusAndResponse
        } yield {
          Response(
            statusCode, Content.of(responseBody),
            headers = Map(
              h"content-type" -> h"application/json",
              h"Access-Control-Allow-Origin" -> h"*"
            )
          )
        }

      case _ =>
        Response(
          404, Content.empty,
          headers = Map(
            h"content-type" -> h"application/json",
            h"Access-Control-Allow-Origin" -> h"*"
          )
        )
    }
  }
}
