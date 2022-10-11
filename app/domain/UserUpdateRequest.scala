package domain

case class UserUpdateRequest(
    name: Option[String],
    age: Option[Int]
)
