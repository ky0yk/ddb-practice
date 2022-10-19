package services.user

import domain.User

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CreateUserService @Inject() (
    userStore: UserStore
) {
  // fixme 何回もcreateできてしまうのを直す
  def create(user: User): Future[Unit] =
    for {
      _ <- userStore.create(user)
    } yield ()
}
