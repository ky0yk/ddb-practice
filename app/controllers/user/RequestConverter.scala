package controllers.user

import controllers.user.vo.UserUpdateRequest
import domain.User
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Json, Reads, Writes}

/** Json converter for user endpoint
  */
object RequestConverter {

  implicit val userReads: Reads[User] = Json.reads[User]
  implicit val userUpdateRequestReads: Reads[UserUpdateRequest] =
    Json.reads[UserUpdateRequest]

  implicit val userWrites: Writes[User] = (
    (JsPath \ "id").write[String] and
      (JsPath \ "name").write[String] and
      (JsPath \ "age").write[Int]
  )(unlift(User.unapply))
}
