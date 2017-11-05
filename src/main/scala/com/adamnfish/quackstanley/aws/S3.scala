package com.adamnfish.quackstanley.aws

import com.adamnfish.quackstanley.Config
import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.persistence.Persistence
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import io.circe.Json
import io.circe.parser.parse

import scala.collection.JavaConverters._
import scala.io.Source
import scala.util.control.NonFatal


class S3 extends Persistence {
  val s3Client = S3.client()

  // TODO maybe run in a Future? Would allow parallel reqests...
  override def getJson(path: String, config: Config): Attempt[Json] = {
    try {
      val s3obj = s3Client.getObject(config.s3Bucket, path)
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

  override def writeJson(json: Json, path: String, config: Config): Attempt[Unit] = {
    try {
      Attempt.Right {
        s3Client.putObject(config.s3Bucket, path, json.noSpaces)
      }
    } catch {
      case NonFatal(e) =>
        Attempt.Left {
          Failure(s"Error writing JSON to S3 $path", "Error writing persistent data", 500, Some(e.getMessage)).asAttempt
        }
    }
  }

  override def listFiles(path: String, config: Config): Attempt[List[String]] = {
    try {
      Attempt.Right {
        val objectListings = s3Client.listObjects(config.s3Bucket, path)
        objectListings.getObjectSummaries.asScala.toList.map(_.getKey)
      }
    } catch {
      case NonFatal(e) =>
        Attempt.Left {
          Failure(s"Error listing files in S3 $path", "Error reading from persistent data storage", 500, Some(e.getMessage))
        }
    }
  }
}
object S3 {
  def client(): AmazonS3 = {
    AmazonS3ClientBuilder
      .standard()
      .withRegion("eu-west-1")
      .build()
  }
}
