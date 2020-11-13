package devserver

import com.typesafe.scalalogging.LazyLogging
import devserver.ApiService.devQuackStanley
import io.javalin.Javalin

import scala.concurrent.Await
import scala.concurrent.duration._


object DevServer extends LazyLogging {
  implicit val ec = scala.concurrent.ExecutionContext.global

  def main(args: Array[String]): Unit = {
    val app = Javalin.create { config =>
      config.enableCorsForAllOrigins()
    }

    app.start(9001)
    app.post("/api", { ctx =>
      println("[TRACE] /api")
      val fResult = devQuackStanley(ctx.body)
      val (statusCode, responseBody) = Await.result(fResult, 10.seconds)

      ctx.status(statusCode)
      ctx.result(responseBody)
    })

    Runtime.getRuntime.addShutdownHook(new Thread(() => {
      println("[INFO] Stopping...")
      app.stop()
    }))
  }
}
