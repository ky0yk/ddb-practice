package services.user

import services.user.errors.ResourceNotFoundError

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteUserService @Inject() (
    userStore: UserStore
) {

  def delete(id: String): Future[Unit] = for {
    maybeUser <- userStore.findById(id)
    _ <- maybeUser match {
      case None    => Future.failed(new ResourceNotFoundError)
      case Some(_) => Future.successful((): Unit)
    }
    _ <- userStore.deleteById(id)
  } yield ()

}
