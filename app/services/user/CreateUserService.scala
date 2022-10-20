package services.user

import domain.User
import play.api.Logging

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CreateUserService @Inject() (
    userStore: UserStore
) extends Logging {
  // fixme 何回もcreateできてしまうのを直す
  def create(user: User): Future[Unit] = {
    logger.info("CreateUserService#create start")

    for {
      _ <- userStore.create(user)
    } yield ()

  }
}
