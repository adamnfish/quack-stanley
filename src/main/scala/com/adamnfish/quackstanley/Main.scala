package com.adamnfish.quackstanley

import java.io._

import scala.concurrent.ExecutionContext.Implicits.global
import com.adamnfish.quackstanley.attempt.Attempt
import com.adamnfish.quackstanley.attempt.LambdaIntegration._
import com.amazonaws.services.lambda.runtime.Context


class Main {
  def handleRequest(in: InputStream, out: OutputStream, context: Context): Unit = {
    bodyAction(in, out, context) { (testArgs: TestArgs, lambdaRequest, context) =>
      context.getLogger.log(s"key: ${testArgs.key}, key2: ${testArgs.key2}")
      Attempt.Right(s"Success! path: ${lambdaRequest.path}, method: ${lambdaRequest.httpMethod}")
    }
  }
}
