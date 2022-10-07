package controllers.user

import javax.inject.Inject
import domain.{User, UserUpdateRequest}
import infra.dbclients.{UserDBClient, UserDBClientV2}
import play.api.libs.json.Json.toJson
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.Function.const

/** controller for user endpoint
  * @param dbClient user db client
  */
class UserController @Inject() (
    dbClient: UserDBClient,
    dbClientV2: UserDBClientV2
)(
    val controllerComponents: ControllerComponents
) extends BaseController {

  import RequestConverter._ // -- (1)

  /** action for creating user
    * @return
    */
  def post = Action.async(parse.json) { req =>
    req.body
      .validate[User]
      .fold( // -- (2)
        invalid => Future(BadRequest), // -- (3)
        user => dbClient.createIfNotExist(user).map(const(Ok)) // -- (4)
      )
  }

  def list = Action.async {
    dbClientV2.list.map(res => Ok(toJson(res)))
  }

  def find(id: String) = Action.async { _ =>
    dbClientV2
      .find(id)
      .map {
        case Some(v) => Ok(toJson(v))
        case None    => NotFound
      }
  }

  def postV2 = Action.async(parse.json) { req =>
    req.body
      .validate[User]
      .fold(
        invalid => Future(BadRequest),
        user => dbClientV2.put(user).map(const(Ok))
      )
  }

  def update(id: String) = Action.async(parse.json) { req =>
    req.body
      .validate[UserUpdateRequest]
      .fold(
        invalid => Future(BadRequest),
        req => dbClientV2.update(id, req).map(const(Ok))
      )
  }

  def delete(id: String) = Action.async {
    dbClientV2.delete(id).map(const(NoContent))
  }
}
