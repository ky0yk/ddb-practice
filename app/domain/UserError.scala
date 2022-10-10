package domain

final case class UserNotFoundError(message: String) extends RuntimeException

final case class JsValueConvertError(message: String) extends RuntimeException
