package controllers.user

import controllers.user.RequestConverter.{
  userReads,
  userUpdateRequestReads,
  userWrites
}
import domain.{
  InvalidUpdateInfoError,
  JsValueConvertError,
  User,
  UserNotFoundError,
  UserUpdateRequest
}
import play.api.Logging
import play.api.libs.json.{JsValue, Reads}
import play.api.libs.json.Json.toJson
import play.api.mvc.{BaseController, ControllerComponents}
import services.user.errors.ResourceNotFoundError
import services.user.{
  CreateUserService,
  DeleteUserService,
  FindUserService,
  ListUserService,
  UpdateUserService
}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserControllerV2 @Inject() (
    listUserService: ListUserService,
    findUserService: FindUserService,
    deleteUserService: DeleteUserService,
    updateUserService: UpdateUserService,
    createUserService: CreateUserService
)(
    val controllerComponents: ControllerComponents
) extends BaseController
    with Logging {

  def list = Action.async {
    logger.info("UserControllerV2#list start")
    val future = for {
      userList <- listUserService.list
    } yield Ok(toJson(userList))

    future.recover { case _ => InternalServerError }
  }

  def find(id: String) = Action.async { _ =>
    logger.info("UserControllerV2#find start")

    val future = for {
      user <- findUserService.findByUserId(id)
      result = user match {
        case Some(_) => Ok(toJson(user))
        case None    => NotFound
      }
    } yield result

    future.recover { case _ => InternalServerError }
  }

  def create = Action.async(parse.json) { req =>
    logger.info("UserControllerV2#create start")

    val future = for {
      userInfo <- JsValueToFuture[User](req.body)
      _ <- createUserService.create(userInfo)
    } yield Ok

    future.recover {
      case _: JsValueConvertError => BadRequest
      case _                      => InternalServerError
    }
  }

  def update(id: String) = Action.async(parse.json) { req =>
    logger.info("UserControllerV2#update start")

    val future = for {
      updateReq <- JsValueToFuture[UserUpdateRequest](req.body)
      _ <- updateUserService.updateById(id, updateReq)
    } yield Ok

    future.recover {
      case _: JsValueConvertError | _: InvalidUpdateInfoError => BadRequest
      case _: ResourceNotFoundError                           => NotFound
      case _                                                  => InternalServerError
    }
  }

  def delete(id: String) = Action.async {
    logger.info("UserControllerV2#delete start")

    val future = for {
      _ <- deleteUserService.deleteById(id)
    } yield NoContent

    future.recover {
      case _: ResourceNotFoundError => NotFound
      case _                        => InternalServerError
    }
  }

  private def JsValueToFuture[T](
      body: JsValue
  )(implicit rds: Reads[T]): Future[T] = {
    Future {
      body
        .validate[T]
        .getOrElse {
          val msg = s"invalid request body. request body=${body}"
          logger.error(msg)
          throw JsValueConvertError(msg)
        }
    }
  }
}
