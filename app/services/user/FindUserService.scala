package services.user

import domain.User
import play.api.Logging
import services.user.errors.ResourceNotFoundError

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class FindUserService @Inject() (
    userStore: UserStore
) extends Logging {
  def findByUserId(id: String): Future[Option[User]] = {
    logger.info("FindUserService#findById start")

    for {
      maybeUser <- userStore.findById(id)
    } yield maybeUser
  }
}
