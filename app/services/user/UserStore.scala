package services.user

import domain.{User, UserUpdateRequest}

import scala.concurrent.Future

trait UserStore {
  def list: Future[List[Option[User]]]
  def findById(id: String): Future[Option[User]]
  def create(user: User): Future[Unit]
  def deleteById(id: String): Future[Unit]
  def updateById(id: String, updateInfo: UserUpdateRequest): Future[Unit]
}
