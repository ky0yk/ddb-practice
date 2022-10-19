package services.user

import domain.User

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ListUserService @Inject() (
    userStore: UserStore
) {

  def list: Future[List[User]] = for {
    users <- userStore.list
  } yield users
}
