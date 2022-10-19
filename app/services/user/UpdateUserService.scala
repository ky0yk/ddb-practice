package services.user

import domain.UserUpdateRequest
import play.api.Logging
import services.user.errors.ResourceNotFoundError

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class UpdateUserService @Inject() (
    userStore: UserStore
) extends Logging {
  def updateById(id: String, updateInfo: UserUpdateRequest): Future[Unit] = {
    logger.info("UpdateUserService#updateById start")

    for {
      maybeUser <- userStore.findById(id)
      _ <- maybeUser match {
        case None    => Future.failed(new ResourceNotFoundError)
        case Some(_) => Future.successful((): Unit)
      }
      _ <- userStore.updateById(id, updateInfo)
    } yield ()
  }
}
