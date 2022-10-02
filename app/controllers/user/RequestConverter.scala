package controllers.user

import domain.{User, UserUpdateRequest}
import play.api.libs.json.{Json, Reads}

/** Json converter for user endpoint
  */
object RequestConverter {

  implicit val userReads: Reads[User] = Json.reads[User]
  implicit val userUpdateRequestReads: Reads[UserUpdateRequest] =
    Json.reads[UserUpdateRequest]
}
