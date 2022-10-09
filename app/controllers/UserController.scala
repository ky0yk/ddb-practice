package controllers.user

import javax.inject.Inject
import domain.{User, UserUpdateRequest}
import infra.dbclients.{UserDBClient, UserDBClientV2}
import play.api.Logging
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

  def list = Action.async {
    logger.info("start list")
    dbClientV2.list
      .map(res => Ok(toJson(res)))
      .recover(_ => InternalServerError)
  }

  def find(id: String) = Action.async { _ =>
    logger.info("start find.")
    dbClientV2
      .find(id)
      .map {
        case Some(v) => Ok(toJson(v))
        case None => {
          NotFound
        }
      }
      .recover(_ => InternalServerError)
  }

  // fixme 何回もpostできてしまうのを直す
  def postV2 = Action.async(parse.json) { req =>
    logger.info("start post")
    req.body
      .validate[User]
      .fold(
        invalid => Future(BadRequest),
        user => dbClientV2.put(user).map(const(Ok))
      )
      .recover(_ => InternalServerError)
  }

  def update(id: String) = Action.async(parse.json) { req =>
    logger.info("start update.")
    req.body
      .validate[UserUpdateRequest]
      .fold(
        invalid => Future(NotFound),
        updateReq =>
          dbClientV2.find(id).flatMap {
            case Some(_) => dbClientV2.update(id, updateReq).map(const(Ok))
            case _       => Future(BadRequest)
          }
      )
      .recover(_ => InternalServerError)
  }

  // fixme 存在しなかった場合の対応を入れる
  def delete(id: String) = Action.async {
    logger.info("start delete.")
    dbClientV2
      .delete(id)
      .map(const(NoContent))
      .recover(_ => InternalServerError)
  }
}
