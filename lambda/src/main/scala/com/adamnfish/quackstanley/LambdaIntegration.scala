package com.adamnfish.quackstanley

import cats.data.EitherT
import cats.effect.unsafe.IORuntime
import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.models.Serialization
import com.adamnfish.quackstanley.models.Serialization.failureToJson
import com.amazonaws.services.lambda.runtime.Context
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.parser.parse
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}

import java.io.{InputStream, OutputStream}
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

  def parseBody[A](inputStream: InputStream)
                  (implicit inDecoder: Decoder[A]): Attempt[(A, LambdaRequest)] = {
    for {
      requestJson <- parseLambdaRequest(inputStream)
      lambdaRequest <- extractLambdaRequest(requestJson)
      body <- extractLambdaRequestBody(lambdaRequest)
      bodyJson <- parseLambdaRequestBody(body)
      a <- Serialization.extractJson[A](bodyJson)
    } yield (a, lambdaRequest)
  }

  def parseJson(inputStream: InputStream): Attempt[(Json, LambdaRequest)] = {
    for {
      requestJson <- parseLambdaRequest(inputStream)
      lambdaRequest <- extractLambdaRequest(requestJson)
      body <- extractLambdaRequestBody(lambdaRequest)
      bodyJson <- parseLambdaRequestBody(body)
    } yield (bodyJson, lambdaRequest)
  }

  def respond[A](attempt: Attempt[A], outputStream: OutputStream, context: Context, allowedOrigin: Option[String])
                (implicit encoder: Encoder[A], runtime: IORuntime): Unit = {
    val lambdaResponse = attempt.value.unsafeRunSync() match {
      case Right(out) =>
        LambdaResponse(200, headers(allowedOrigin), out.asJson.noSpaces)
      case Left(failures) =>
        context.getLogger.log(s"Failure: ${failures.logString}")
        val body = Json.obj(
          "errors" -> Json.fromValues(failures.failures.map(failureToJson))
        )
        LambdaResponse(failures.statusCode, headers(allowedOrigin), body.noSpaces)
    }

    // write output stream
    outputStream.write(lambdaResponse.asJson.noSpaces.getBytes("UTF-8"))
    outputStream.flush()
  }

  private def parseLambdaRequest(inputStream: InputStream): Attempt[Json] = {
    EitherT.fromEither(parse(Source.fromInputStream(inputStream, "UTF-8").mkString).left.map { parseFailure =>
      Failure(s"Could not parse request as JSON: ${parseFailure.message}", "Invalid request", 400).asFailedAttempt
    })
  }

  private def extractLambdaRequest(json: Json): Attempt[LambdaRequest] = {
    EitherT.fromEither(json.as[LambdaRequest].left.map { decodingFailure =>
      Failure(s"Failed to extract lambda request ${decodingFailure.message}", "Request could not be understood", 400, Some(decodingFailure.history.mkString("|"))).asFailedAttempt
    })
  }

  private def extractLambdaRequestBody(lambdaRequest: LambdaRequest): Attempt[String] = {
    EitherT.fromOption(lambdaRequest.body,
      Failure("No body on request", "The request was empty", 400).asFailedAttempt
    )
  }

  private def parseLambdaRequestBody(body: String): Attempt[Json] = {
    EitherT.fromEither(parse(body).left.map { parsingFailure =>
      Failure(s"Could not parse request body as JSON: ${parsingFailure.message}", "Invalid request content", 400).asFailedAttempt
    })
  }
}
