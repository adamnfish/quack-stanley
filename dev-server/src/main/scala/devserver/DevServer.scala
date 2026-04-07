package devserver

import cats.effect.{ExitCode, IO, IOApp}
import devserver.ApiService.devQuackStanley
import org.http4s.blaze.server._
import org.http4s.dsl.io._
import org.http4s.headers.Origin
import org.http4s.implicits._
import org.http4s.server.middleware._
import org.http4s.{HttpRoutes, Response, Status, Uri}
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

object DevServer extends IOApp {
  implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  private val http4sApp = HttpRoutes
    .of[IO] {
      case GET -> Root / "healthcheck" =>
        IO.pure(Response(Status.Ok))

      case req @ POST -> Root / "api" =>
        for {
          body <- req.as[String]
          (statusCode, responseBody) <- devQuackStanley(body)
          status <- Status.fromInt(statusCode) match {
            case Right(status)      => IO.pure(status)
            case Left(parseFailure) => IO.raiseError(parseFailure)
          }
        } yield Response(status).withEntity(responseBody)
    }
    .orNotFound

  override def run(args: List[String]): IO[ExitCode] = for {
    corsApp <- CORS.policy.withAllowOriginHost(
      Set(Origin.Host(Uri.Scheme.http, Uri.RegName("localhost"), Some(3000)))
    )(http4sApp)
    exitCode <- BlazeServerBuilder[IO]
      .bindHttp(9001, "0.0.0.0")
      .withHttpApp(corsApp)
      .serve
      .compile
      .last
      // use the exit code produced by the server, or fall back to a default "success" code
      .map(_.getOrElse(ExitCode.Success))
  } yield exitCode
}
