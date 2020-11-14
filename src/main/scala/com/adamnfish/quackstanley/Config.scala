package com.adamnfish.quackstanley

import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import com.adamnfish.quackstanley.aws.S3
import com.adamnfish.quackstanley.persistence.Persistence

import scala.concurrent.ExecutionContext
import scala.util.Properties


case class Config(
  stage: String,
  persistence: Persistence
)
object Config {
  def fromEnvironment()(implicit ec: ExecutionContext): Attempt[Config] = {
    for {
      bucket <- Attempt.fromOption(Properties.envOrNone("APP_DATA_S3_BUCKET"), {
        Failure("Couldn't read S3 bucket name for configuration", "Quack Stanley failed because it is missing configuration", 500, Some("APP_DATA_S3_BUCKET")).asAttempt
      })
      stage <- Attempt.fromOption(Properties.envOrNone("APP_STAGE"), {
        Failure("Couldn't read stage configuration", "Quack Stanley failed because it is missing configuration", 500, Some("APP_STAGE")).asAttempt
      })
      persistence = new S3(bucket)
    } yield Config(stage, persistence)
  }
}
