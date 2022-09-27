package controllers.user

import javax.inject.Inject
import domain.User
import infra.dbclients.UserDBClient
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.Function.const

/** controller for user endpoint
  * @param dbClient user db client
  */
class UserController @Inject() (dbClient: UserDBClient)(
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

  def get(id: String) = Action.async { _ =>
    val res = dbClient.getByV2(id)
    Future(Ok(res.toString))
  }
}
