package com.adamnfish.quackstanley.aws

import cats.data.EitherT
import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.persistence.Persistence
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, PutObjectRequest, ListObjectsV2Request}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.core.sync.RequestBody
import io.circe.Json
import io.circe.parser.parse

import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal


class S3(s3Bucket: String, val s3Client: S3Client) extends Persistence {

  // TODO maybe run in a Future? Would allow parallel reqests...
  override def getJson(path: String): Attempt[Json] = {
    try {
      val getRequest = GetObjectRequest.builder()
        .bucket(s3Bucket)
        .key(path)
        .build()
      val responseBytes = s3Client.getObjectAsBytes(getRequest)
      val content = responseBytes.asUtf8String()
      EitherT.fromEither(parse(content).left.map { _ =>
        Failure(s"Failed to parse JSON from S3 object $path", "Failed to parse persistent data", 500).asFailedAttempt
      })
    } catch {
      case NonFatal(e) =>
        EitherT.leftT {
          Failure(s"Error fetching file from S3 $path", "Error fetching persistent data", 500, Some(e.getMessage)).asFailedAttempt
        }
    }
  }

  override def writeJson(json: Json, path: String): Attempt[Unit] = {
    try {
      EitherT.pure {
        val putRequest = PutObjectRequest.builder()
          .bucket(s3Bucket)
          .key(path)
          .build()
        s3Client.putObject(putRequest, RequestBody.fromString(json.noSpaces))
      }
    } catch {
      case NonFatal(e) =>
        EitherT.leftT {
          Failure(s"Error writing JSON to S3 $path", "Error writing persistent data", 500, Some(e.getMessage)).asFailedAttempt
        }
    }
  }

  override def listFiles(path: String): Attempt[List[String]] = {
    try {
      EitherT.pure {
        val listRequest = ListObjectsV2Request.builder()
          .bucket(s3Bucket)
          .prefix(path)
          .build()
        val listResponse = s3Client.listObjectsV2(listRequest)
        listResponse.contents().asScala.toList.map(_.key())
      }
    } catch {
      case NonFatal(e) =>
        EitherT.leftT {
          Failure(s"Error listing files in S3 $path", "Error reading from persistent data storage", 500, Some(e.getMessage)).asFailedAttempt
        }
    }
  }
}
object S3 {
  def client(): S3Client = {
    S3Client.builder()
      .region(Region.EU_WEST_1)
      .build()
  }
}
