package controllers.user

import javax.inject.Inject
import domain.User
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

  def get(id: String) = Action.async { _ =>
    val res = dbClientV2.get(id)
    Future(Ok(res.toString))
  }
}
