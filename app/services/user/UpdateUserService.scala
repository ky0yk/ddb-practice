package services.user

import domain.UserUpdateRequest

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class UpdateUserService @Inject() (
    userStore: UserStore
) {
  def updateById(id: String, updateInfo: UserUpdateRequest): Future[Unit] = {
    for {
      _ <- userStore.findById(id)
      _ <- userStore.updateById(id, updateInfo)
    } yield ()
  }
}
