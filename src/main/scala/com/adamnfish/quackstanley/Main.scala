package com.adamnfish.quackstanley

import java.io._
import scala.concurrent.ExecutionContext.Implicits.global

import com.adamnfish.quackstanley.attempt.Attempt
import com.adamnfish.quackstanley.attempt.LambdaIntegration.lambdaAction


class Main {
  def handleRequest(in: InputStream, out: OutputStream): Unit = {
    lambdaAction(in, out) { lambdaRequest =>
      Attempt.Right(s"Success!\npath: ${lambdaRequest.path}, method: ${lambdaRequest.httpMethod}\n")
    }
  }
}
