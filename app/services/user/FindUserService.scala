package services.user

import domain.User

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class FindUserService @Inject() (
    userStore: UserStore
) {
  def findByUserId(id: String): Future[Option[User]] = for {
    maybeUser <- userStore.findById(id)
  } yield maybeUser
}
