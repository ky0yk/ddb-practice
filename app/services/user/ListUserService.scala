package services.user

import domain.User
import play.api.Logging

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ListUserService @Inject() (
    userStore: UserStore
) extends Logging {

  def list: Future[List[Option[User]]] = {
    logger.info("ListUserService#list start")

    for {
      users <- userStore.list
    } yield users
  }
}
