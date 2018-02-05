package devserver

import devserver.ApiService.devQuackStanley
import fs2.{Strategy, Task}
import io.circe.Json
import org.http4s.circe._
import org.http4s.dsl.impl.EntityResponseGenerator
import org.http4s.{HttpService, Status}
import org.http4s.dsl.{Root, _}
import org.http4s.server.blaze._
import org.http4s.util.StreamApp
import org.http4s.client.blaze._


object DevServer extends StreamApp {
  implicit val ec = scala.concurrent.ExecutionContext.global
  implicit val strategy = Strategy.sequential

  val httpClient = PooledHttp1Client()

  val elmFrontend = HttpService {
    case request @ GET -> Root =>
//      httpClient.expect("http://localhost:8080/")
      Ok("test")
  }

  class ArbitraryStatus(val status: Status) extends AnyVal with EntityResponseGenerator

  val lambdaApi = HttpService {
    case request @ POST -> Root =>
      for {
        json <- request.as[Json]
        statusAndResponse <- Task.fromFuture(devQuackStanley(json))
        (statusCode, responseBody) = statusAndResponse
        status = new ArbitraryStatus(Status.fromInt(statusCode).right.get)
        response <- status(responseBody)
      } yield response
  }

  override def stream(args: List[String]) = {
    BlazeBuilder
      .bindHttp(9001, "localhost")
      .mountService(elmFrontend, "/")
      .mountService(lambdaApi, "/api")
      .serve
  }
}
