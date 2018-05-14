package com.adamnfish.quackstanley.attempt

import java.io.{InputStream, OutputStream}

import com.adamnfish.quackstanley.models.Serialization
import com.amazonaws.services.lambda.runtime.Context
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}
import io.circe.parser._
import io.circe.generic.semiauto._

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

  def headers(allowedOrigin: Option[String]): Map[String, String] = {
    val defaultHeaders = Map(
      "Content-Type" -> "application/json"
    )
    val extraHeaders = for {
      origin <- allowedOrigin
    } yield Map(
      "Access-Control-Allow-Origin" -> origin
    )
    defaultHeaders ++ extraHeaders.getOrElse(Map.empty)
  }

  def failureToJson(failure: Failure): Json = {
    Json.obj(
      "message" -> Json.fromString(failure.friendlyMessage),
      "context" -> Json.fromString(failure.context.getOrElse(""))
    )
  }

  def parseBody[A](inputStream: InputStream)
                  (implicit inDecoder: Decoder[A], ec: ExecutionContext): Attempt[(A, LambdaRequest)] = {
    for {
      requestJson <- parseLambdaRequest(inputStream)
      lambdaRequest <- extractLambdaRequest(requestJson)
      body <- extractLambdaRequestBody(lambdaRequest)
      bodyJson <- parseLambdaRequestBody(body)
      a <- Serialization.extractJson[A](bodyJson)
    } yield (a, lambdaRequest)
  }

  def parseJson(inputStream: InputStream)
    (implicit ec: ExecutionContext): Attempt[(Json, LambdaRequest)] = {
    for {
      requestJson <- parseLambdaRequest(inputStream)
      lambdaRequest <- extractLambdaRequest(requestJson)
      body <- extractLambdaRequestBody(lambdaRequest)
      bodyJson <- parseLambdaRequestBody(body)
    } yield (bodyJson, lambdaRequest)
  }

  def respond[A](attempt: Attempt[A], outputStream: OutputStream, context: Context, allowedOrigin: Option[String])
                (implicit encoder: Encoder[A], ec: ExecutionContext): Unit = {
    val resultFuture = attempt.asFuture.map {
      case Right(out) =>
        LambdaResponse(200, headers(allowedOrigin), out.asJson.noSpaces)
      case Left(failures) =>
        context.getLogger.log(s"Failure: ${failures.logString}")
        val body = Json.obj(
          "errors" -> Json.fromValues(failures.failures.map(failureToJson))
        )
        LambdaResponse(failures.statusCode, headers(allowedOrigin), body.noSpaces)
    }
    // wait for result
    val lambdaResponse = Await.result(resultFuture, 20.seconds)
    // write output stream
    outputStream.write(lambdaResponse.asJson.noSpaces.getBytes("UTF-8"))
    outputStream.flush()
  }

  private def parseLambdaRequest(inputStream: InputStream): Attempt[Json] = {
    Attempt.fromEither(parse(Source.fromInputStream(inputStream, "UTF-8").mkString).left.map { parseFailure =>
      Failure(s"Could not parse request as JSON: ${parseFailure.message}", "Could not parse request JSON", 400).asAttempt
    })
  }

  private def extractLambdaRequest(json: Json): Attempt[LambdaRequest] = {
    Attempt.fromEither(json.as[LambdaRequest].left.map { decodingFailure =>
      Failure(s"Failed to extract lambda request ${decodingFailure.message}", "Failed to extract request JSON", 400, Some(decodingFailure.history.mkString("|"))).asAttempt
    })
  }

  private def extractLambdaRequestBody(lambdaRequest: LambdaRequest): Attempt[String] = {
    Attempt.fromOption(lambdaRequest.body,
      Failure("No body on request", "Could not parse missing request body", 400).asAttempt
    )
  }

  private def parseLambdaRequestBody(body: String): Attempt[Json] = {
    Attempt.fromEither(parse(body).left.map { parsingFailure =>
      Failure(s"Could not parse request body as JSON: ${parsingFailure.message}", "Could not parse request body JSON", 400).asAttempt
    })
  }
}
