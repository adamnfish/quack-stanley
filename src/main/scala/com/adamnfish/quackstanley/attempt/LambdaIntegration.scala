package com.adamnfish.quackstanley.attempt

import java.io.{InputStream, OutputStream}

import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}
import io.circe.parser._
import io.circe.generic.extras.semiauto._

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.io.Source


object LambdaIntegration {
  case class LambdaRequest(
    httpMethod: String,
    path: String,
    queryStringParameters: Option[Map[String, String]],
    headers: Option[Map[String, String]],
    body: Option[String]
  )

  case class LambdaResponse(
    statusCode: Int,
    headers: Map[String, String],
    body: String
  )

  private implicit val requestDecoder: Decoder[LambdaRequest] = deriveDecoder
  private implicit val responseEncoder: Encoder[LambdaResponse] = deriveEncoder

  val headers = Map(
    "Content-Type" -> "application/json"
  )

  def failureToJson(failure: Failure): Json = {
    Json.obj(
      "message" -> Json.fromString(failure.friendlyMessage),
      "context" -> Json.fromString(failure.context.getOrElse(""))
    )
  }

  def lambdaAction[Out](inputStream: InputStream, outputStream: OutputStream)
                       (action: (LambdaRequest) => Attempt[Out])
                       (implicit outEncoder: Encoder[Out], ec: ExecutionContext): Unit = {
    val resultAttempt = for {
      requestJson <- parseLambdaRequest(inputStream)
      lambdaRequest <- extractLambdaRequest(requestJson)
      out <- action(lambdaRequest)
    } yield out

    handleAttempt[Out](resultAttempt, outputStream)
  }

  def lambdaBodyAction[In, Out](inputStream: InputStream, outputStream: OutputStream)
                               (action: (In, LambdaRequest) => Attempt[Out])
                               (implicit inDecoder: Decoder[In], outEncoder: Encoder[Out], ec: ExecutionContext): Unit = {
    val resultAttempt = for {
      requestJson <- parseLambdaRequest(inputStream)
      lambdaRequest <- extractLambdaRequest(requestJson)
      body <- extractLambdaRequestBody(lambdaRequest)
      bodyJson <- parseLambdaRequestBody(body)
      in <- extractJson[In](bodyJson)
      out <- action(in, lambdaRequest)
    } yield out

    handleAttempt[Out](resultAttempt, outputStream)
  }

  private def handleAttempt[A](attempt: Attempt[A], outputStream: OutputStream)
    (implicit encoder: Encoder[A], ec: ExecutionContext): Unit = {
    val resultFuture = attempt.asFuture.map {
      case Right(out) =>
        LambdaResponse(200, headers, out.asJson.noSpaces)
      case Left(failures) =>
        val body = Json.obj(
          "errors" -> Json.fromValues(failures.failures.map(failureToJson))
        )
        LambdaResponse(failures.statusCode, headers, body.noSpaces)
    }
    // wait for result
    val lambdaResponse = Await.result(resultFuture, 20.seconds)
    // write output stream
    outputStream.write(lambdaResponse.asJson.noSpaces.getBytes("UTF-8"))
    outputStream.flush()
  }

  private def extractLambdaRequest(json: Json): Attempt[LambdaRequest] = {
    Attempt.fromEither(json.as[LambdaRequest].left.map { decodingFailure =>
      Failure(s"Failed to parse lambda request ${decodingFailure.message}", "Failed to parse request JSON", 400).asAttempt
    })
  }

  private def parseLambdaRequest(inputStream: InputStream): Attempt[Json] = {
    Attempt.fromEither(parse(Source.fromInputStream(inputStream, "UTF-8").mkString).left.map { parseFailure =>
      Failure(s"Could not parse request as JSON: ${parseFailure.message}", "Could not parse request JSON", 400).asAttempt
    })
  }

  private def extractLambdaRequestBody(lambdaRequest: LambdaRequest): Attempt[String] = {
    Attempt.fromOption(lambdaRequest.body, Failure("No body on request", "Could not parse missing request body", 400).asAttempt)
  }

  private def parseLambdaRequestBody(body: String): Attempt[Json] = {
    Attempt.fromEither(parse(body).left.map { parsingFailure =>
      Failure(s"Could not parse request body as JSON: ${parsingFailure.message}", "Could not parse request body JSON", 400).asAttempt
    })
  }

  private def extractJson[A](json: Json)(implicit decoder: Decoder[A]): Attempt[A] = {
    Attempt.fromEither(json.as[A].left.map { decodingFailure =>
      Failure(s"Failed to parse request body as expected type: ${decodingFailure.message}", "Failed to parse request JSON", 400).asAttempt
    })
  }
}
