package infra.dbclients.v2

trait DBClientError extends RuntimeException

case class ServiceError(message: String) extends DBClientError

case class ClientError(message: String) extends DBClientError
