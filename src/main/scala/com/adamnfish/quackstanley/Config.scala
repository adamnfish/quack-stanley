package com.adamnfish.quackstanley

import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.aws.S3
import com.adamnfish.quackstanley.persistence.Persistence

import scala.concurrent.ExecutionContext
import scala.util.Properties


case class Config(
  s3Bucket: String,
  stage: String,
  ioClient: Persistence
)
object Config {
  def fromEnvironment()(implicit ec: ExecutionContext): Attempt[Config] = {
    val io = new S3
    for {
      bucket <- Attempt.fromOption(Properties.envOrNone("APP_DATA_S3_BUCKET"), {
        Failure("Couldn't read S3 bucket name for configuration", "Application failed due to missing configuration", 500, Some("APP_DATA_S3_BUCKET")).asAttempt
      })
      stage <- Attempt.fromOption(Properties.envOrNone("APP_STAGE"), {
        Failure("Couldn't read stage configuration", "Application failed due to missing configuration", 500, Some("APP_STAGE")).asAttempt
      })
    } yield Config(bucket, stage, io)
  }
}
