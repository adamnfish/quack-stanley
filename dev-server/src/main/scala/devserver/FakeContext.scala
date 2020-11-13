package devserver

import com.amazonaws.services.lambda.runtime.{ClientContext, CognitoIdentity, Context, LambdaLogger}
import com.typesafe.scalalogging.LazyLogging


class FakeContext extends Context with LazyLogging {
  override def getLogStreamName = "dev-server-logs"

  class DevLogger extends LambdaLogger {
    def log(string: String): Unit = {
      logger.info(string)
    }

    override def log(message: Array[Byte]): Unit = {
      logger.info(new String(message))
    }
  }

  override def getFunctionName = "function-name"
  override def getRemainingTimeInMillis = 1000
  override def getLogger = new DevLogger
  override def getFunctionVersion: String = "dev"
  override def getMemoryLimitInMB = 512
  override def getClientContext: ClientContext = null
  override def getInvokedFunctionArn = "dev::arn"
  override def getIdentity: CognitoIdentity = null
  override def getLogGroupName = "log-gropu"
  override def getAwsRequestId = "dev-request"
}
