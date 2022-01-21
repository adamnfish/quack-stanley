package com.adamnfish.quackstanley.attempt


case class FailedAttempt(failures: List[Failure]) {
  def statusCode: Int = failures.map(_.statusCode).max
  def logString: String = failures.map { failure =>
    List(
      Some(failure.message),
      failure.context.map(c => s"context: $c"),
      failure.exception.map(e => "err: " + e.getStackTrace.mkString("; "))
    ).flatten.mkString(" ")
  }.mkString(", ")
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
  context: Option[String] = None,
  exception: Option[Throwable] = None
) {
  def asFailedAttempt = FailedAttempt(this)
}
