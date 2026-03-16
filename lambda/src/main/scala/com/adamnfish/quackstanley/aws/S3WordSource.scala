package com.adamnfish.quackstanley.aws

import com.adamnfish.quackstanley.models.{Role, Word}
import com.adamnfish.quackstanley.persistence.WordSource
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest

import scala.util.control.NonFatal


class S3WordSource(s3Bucket: String, s3Client: S3Client) extends WordSource {
  private val wordsPath = "config/words.txt"
  private val rolesPath = "config/roles.txt"

  override val words: List[Word] = loadLines(wordsPath).map(Word)
  override val roles: List[Role] = loadLines(rolesPath).map(Role)

  private def loadLines(path: String): List[String] = {
    try {
      val getRequest = GetObjectRequest.builder()
        .bucket(s3Bucket)
        .key(path)
        .build()
      s3Client.getObjectAsBytes(getRequest).asUtf8String()
        .linesIterator.filter(_.nonEmpty).toList
    } catch {
      case NonFatal(e) =>
        throw new RuntimeException(s"Failed to load word list from S3 at startup: $path", e)
    }
  }
}
