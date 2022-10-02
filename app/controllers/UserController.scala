package controllers.user

import javax.inject.Inject
import domain.{User, UserUpdateRequest}
import infra.dbclients.{UserDBClient, UserDBClientV2}
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
    val res = dbClientV2.list
    Future(Ok(res.toString))
  }

  def find(id: String) = Action.async { _ =>
    val res = dbClientV2.find(id)
    Future(Ok(res.toString))
  }

  def postV2 = Action.async(parse.json) { req =>
    req.body
      .validate[User]
      .fold(
        invalid => Future(BadRequest),
        user => {
          val res = dbClientV2.put(user)
          Future(Ok(res.toString))
        }
      )
  }

  def update(id: String) = Action.async(parse.json) { req =>
    req.body
      .validate[UserUpdateRequest]
      .fold(
        invalid => Future(BadRequest),
        req => {
          val res = dbClientV2.update(id, req)
          Future(Ok(res.toString))
        }
      )
  }

  def delete(id: String) = Action.async {
    val res = dbClientV2.delete(id)
    Future(Ok(res.toString))
  }
}
