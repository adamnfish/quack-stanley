package devserver

import cats.effect.{ExitCode, IO, IOApp, Resource}
import devserver.ApiService.devQuackStanley
import io.javalin.Javalin
import io.javalin.http.{Context, Handler}

import scala.concurrent.Await
import scala.concurrent.duration._


object DevServer extends IOApp {
  private val javalinApplication =
    Resource.make {
      IO {
        Javalin.create { config =>
          config.enableCorsForAllOrigins()
        }.start(9001)
      }
    } { app =>
      IO(app.stop())
    }

  override def run(args: List[String]): IO[ExitCode] = {
    javalinApplication.use { app =>
      app.post("/api", new Handler {
        def handle(ctx: Context): Unit = {
          println("[TRACE] /api")
          val fResult = devQuackStanley(ctx.body).unsafeToFuture()(runtime)
          val (statusCode, responseBody) = Await.result(fResult, 10.seconds)

          ctx.status(statusCode)
          ctx.result(responseBody)
        }
      })
      IO.never
    }
  }
}
