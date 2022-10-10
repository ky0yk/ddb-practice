package controllers.user

import controllers.user.RequestConverter.{
  userReads,
  userUpdateRequestReads,
  userWrites
}
import controllers.user.vo.UserUpdateRequest
import domain.User
import infra.dbclients.v1.UserDBClient
import infra.dbclients.v2.UserDBClientV2
import play.api.Logging
import play.api.libs.json.Json.toJson
import play.api.mvc.{BaseController, ControllerComponents}

import javax.inject.Inject
import scala.Function.const
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserControllerV2 @Inject() (
    dbClientV2: UserDBClientV2
)(
    val controllerComponents: ControllerComponents
) extends BaseController
    with Logging {

  def list = Action.async {
    logger.info("UserControllerV2#list start")
    dbClientV2.list
      .map(res => Ok(toJson(res)))
      .recover(_ => InternalServerError)
  }

  def find(id: String) = Action.async { _ =>
    logger.info("UserControllerV2#find start")
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

  // fixme 何回もcreateできてしまうのを直す
  def create = Action.async(parse.json) { req =>
    logger.info("UserControllerV2#create start")
    req.body
      .validate[User]
      .fold(
        invalid => Future(BadRequest),
        user => dbClientV2.create(user).map(const(Ok))
      )
      .recover(_ => InternalServerError)
  }

  def update(id: String) = Action.async(parse.json) { req =>
    logger.info("UserControllerV2#update start")
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
    logger.info("UserControllerV2#delete start")
    dbClientV2
      .delete(id)
      .map(const(NoContent))
      .recover(_ => InternalServerError)
  }
}
