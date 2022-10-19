package infra.dbclients.v2

import software.amazon.awssdk.core.exception.SdkServiceException

object DBClientErrorConverter {

  def translateForClientError(error: Throwable): Throwable = {
    error match {
      case e: SdkServiceException =>
        e.statusCode() match {
          case _ if 400 to 499 contains (e.statusCode()) =>
            ServiceError(e.getMessage)
          case _ if 500 to 599 contains (e.statusCode()) =>
            ClientError(e.getMessage)
          case _ => new RuntimeException(e)
        }
      case e => new RuntimeException(e)
    }
  }
}
