package com.adamnfish.quackstanley.aws

import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import io.circe.{Json, ParsingFailure}
import io.circe.parser.parse

import scala.io.Source
import scala.util.control.NonFatal

object S3 {
  def client(): AmazonS3 = {
    AmazonS3ClientBuilder
      .standard()
      .withRegion("eu-west-1")
      .build()
  }

  def getJson(bucketName: String, path: String, client: AmazonS3): Attempt[Json] = {
    try {
      val s3obj = client.getObject(bucketName, path)
      val objStream = s3obj.getObjectContent
      Attempt.fromEither(parse(Source.fromInputStream(objStream, "UTF-8").mkString).left.map { parsingFailure =>
        Failure(s"Failed to parse JSON from S3 object $path", "Failed to parse persistent data", 500).asAttempt
      })
    } catch {
      case NonFatal(e) =>
        Attempt.Left {
          Failure(s"Error fetching file from S3 $path", "Error fetching persistent data", 500, Some(e.getMessage)).asAttempt
        }
    }
  }

  def writeJson(json: Json, bucktName: String, path: String, client: AmazonS3): Attempt[Unit] = {
    try {
      Attempt.Right {
        client.putObject(bucktName, path, json.noSpaces)
      }
    } catch {
      case NonFatal(e) =>
        Attempt.Left {
          Failure(s"Error writing JSON to S3 $path", "Error writing persistent data", 500, Some(e.getMessage)).asAttempt
        }
    }
  }
}
