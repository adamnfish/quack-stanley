package devserver

import java.io.FileNotFoundException

import com.typesafe.scalalogging.LazyLogging
import cats.implicits._
import devserver.ApiService.devQuackStanley
import fs2.{Strategy, Task}
import fs2.util.Functor
import fs2.interop.cats._
import io.circe.Json
import org.http4s._
import org.http4s.dsl._
import org.http4s.StaticFile
import org.http4s.StaticFile.getClass
import org.http4s.circe._
import org.http4s.client.blaze._
import org.http4s.dsl.impl.EntityResponseGenerator
import org.http4s.dsl.{Root, _}
import org.http4s.server.blaze._
import org.http4s.util.StreamApp
import org.http4s.{HttpService, Status}

import scala.io.Source
import scala.util.Try


object DevServer extends StreamApp with LazyLogging {
  implicit val ec = scala.concurrent.ExecutionContext.global
  implicit val strategy = Strategy.sequential

  class ArbitraryStatus(val status: Status) extends AnyVal with EntityResponseGenerator

  val httpClient = PooledHttp1Client()
  val elmFrontend = HttpService {
    case request @ GET -> _ =>
      logger.info(s"request: ${request.pathInfo}")
      val uri = "http://localhost:8000" + request.pathInfo
      httpClient.get(uri) { response =>
        for {
          body <- response.as[String]
          result <- new ArbitraryStatus(response.status)(body).putHeaders(response.headers.toSeq:_*)
        } yield result
      }
  }

  val static = HttpService {
    case request @ GET -> _ =>
      logger.info(s"request: ${request.pathInfo}")
      val path = "/static" + request.pathInfo
      StaticFile.fromResource(path, Some(request))
        .map(Task.now) // This one is require to make the types match up
        .getOrElse(NotFound()) // In case the file doesn't exist
        .flatten
  }

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
      .mountService(lambdaApi, "/api")
      .mountService(static, "/static")
      .mountService(elmFrontend, "/")
      .serve
  }
}
