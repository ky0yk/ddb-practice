package controllers.user.vo

case class UserUpdateRequest(
    name: Option[String],
    age: Option[Int]
)
