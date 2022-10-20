package services.user

import play.api.Logging
import services.user.errors.ResourceNotFoundError

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteUserService @Inject() (
    userStore: UserStore
) extends Logging {

  def deleteById(id: String): Future[Unit] = {
    logger.info("DeleteUserService#deleteById start")

    for {
      maybeUser <- userStore.findById(id)
      _ <- maybeUser match {
        case None    => Future.failed(new ResourceNotFoundError)
        case Some(_) => Future.successful((): Unit)
      }
      _ <- userStore.deleteById(id)
    } yield ()
  }

}
