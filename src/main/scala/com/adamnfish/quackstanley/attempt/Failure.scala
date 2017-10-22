package com.adamnfish.quackstanley.attempt


case class FailedAttempt(failures: List[Failure]) {
  def statusCode: Int = failures.map(_.statusCode).max
  def logString: String = failures.map(_.message).mkString(", ")
}
object FailedAttempt {
  def apply(error: Failure): FailedAttempt = {
    FailedAttempt(List(error))
  }
  def apply(errors: Seq[Failure]): FailedAttempt = {
    FailedAttempt(errors.toList)
  }
}

case class Failure(
  message: String,
  friendlyMessage: String,
  statusCode: Int,
  context: Option[String] = None
) {
  def asAttempt = FailedAttempt(this)
}
