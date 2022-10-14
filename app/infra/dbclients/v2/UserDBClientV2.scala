package infra.dbclients.v2

import domain.{
  InvalidUpdateInfoError,
  User,
  UserNotFoundError,
  UserUpdateRequest
}
import play.api.Logging
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model._

import java.util.{Map => JavaMap}
import javax.inject.Inject
import scala.Function.const
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters.MapHasAsJava

// AWS SDK for Javaをスムーズに利用するためにimplicit conversionを利用
import infra.dbclients.v2.TypeConverter._

/** user db client
  */
class UserDBClientV2 @Inject() (client: DynamoDbAsyncClient) extends Logging {
  val table = "users"

  def list: Future[List[User]] = {
    logger.info("DDB list user start.")
    val req = ScanRequest.builder().tableName(table).build()
    client
      .scan(req)
      .map(
        _.items()
          .map(toUser(_))
      )
  }

  def find(id: String): Future[User] = {
    val key = Map("user_id" -> toAttS(id))
    logger.info(s"DDB find user start. id=${id}")
    val req = GetItemRequest.builder().tableName(table).key(key).build()
    client
      .getItem(req)
      .map { res =>
        if (res.item().isEmpty) {
          val msg = s"user not found. id=${id}"
          logger.error(msg)
          throw UserNotFoundError(msg)
        }
        toUser(res.item())
      }
  }

  def create(user: User): Future[Unit] = {
    val item = Map(
      "user_id" -> toAttS(user.id),
      "user_name" -> toAttS(user.name),
      "user_age" -> toAttN(user.age)
    )
    logger.info(s"DDB create user start. user=${item}")

    val req = PutItemRequest.builder().tableName(table).item(item).build()
    client
      .putItem(req)
      .transform(const((): Unit), identity)
  }

  def delete(id: String): Future[Unit] = {
    val key = Map("user_id" -> toAttS(id))
    logger.info(s"DDB delete user start. id=${id}")
    val req = DeleteItemRequest.builder().tableName(table).key(key).build()
    client
      .deleteItem(req)
      .transform(const((): Unit), identity)
  }

  def update(id: String, updateInfo: UserUpdateRequest): Future[Unit] = {
    val updatedValues = toUpdatedValues(updateInfo)
    val key = Map("user_id" -> toAttS(id))
    logger.info(
      s"DDB update user start. id=${id}. updateValue=${updatedValues}"
    )

    val req =
      UpdateItemRequest
        .builder()
        .tableName(table)
        .key(key)
        .attributeUpdates(updatedValues)
        .build()
    client
      .updateItem(req)
      .transform(const((): Unit), identity)
  }

  private def toUser(item: JavaMap[String, AttributeValue]): User = {
    User(
      item.get("user_id").s(),
      item.get("user_name").s(),
      item.get("user_age").n().toInt
    )
  }

  private def toUpdatedValues(
      updateInfo: UserUpdateRequest
  ): Map[String, AttributeValueUpdate] = {

    val updatedValues =
      List(
        ("user_name", updateInfo.name),
        ("user_age", updateInfo.age)
      )
        .filter(_._2.isDefined)
        .map(_ match {
          case (s, Some(v: String)) if s == "user_name" =>
            (s -> (toUpdateAttS(v)))
          case (s, Some(v: Int)) if s == "user_age" =>
            (s -> (toUpdateAttN(v)))
          case _ => {
            val msg = s"invalid information. request=${updateInfo}"
            logger.error(msg)
            throw InvalidUpdateInfoError(msg)
          }
        })
        .toMap

    if (updatedValues.size == 0) {
      val msg =
        s"request does not have valid information. request=${updateInfo}"
      logger.error(msg)
      throw InvalidUpdateInfoError(msg)
    }

    updatedValues
  }

  private def toAttS(v: String): AttributeValue =
    AttributeValue.builder().s(v).build()

  private def toAttN(v: Int): AttributeValue =
    AttributeValue.builder().n(v.toString).build()

  private def toUpdateAttS(
      v: String
  ): AttributeValueUpdate = {
    AttributeValueUpdate
      .builder()
      .value(toAttS(v))
      .action(AttributeAction.PUT)
      .build()
  }

  private def toUpdateAttN(
      v: Int
  ): AttributeValueUpdate = {
    AttributeValueUpdate
      .builder()
      .value(toAttN(v))
      .action(AttributeAction.PUT)
      .build()
  }
}
