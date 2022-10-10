package controllers.user

import domain.User
import infra.dbclients.v1.UserDBClient
import infra.dbclients.v2.UserDBClientV2
import play.api.Logging
import play.api.mvc.{BaseController, ControllerComponents}

import javax.inject.Inject
import scala.Function.const
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/** controller for user endpoint
  * @param dbClient user db client
  */
class UserController @Inject() (
    dbClient: UserDBClient,
    dbClientV2: UserDBClientV2
)(
    val controllerComponents: ControllerComponents
) extends BaseController
    with Logging {

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
}
