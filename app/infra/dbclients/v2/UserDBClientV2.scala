package infra.dbclients.v2

import controllers.user.vo.UserUpdateRequest
import domain.User
import play.api.Logging
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model._

import java.util.concurrent.CompletableFuture
import java.util.{List => JavaList, Map => JavaMap}
import javax.inject.Inject
import scala.Function.const
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters.{ListHasAsScala, MapHasAsJava}
import scala.jdk.FutureConverters.CompletionStageOps

/** user db client
  */
class UserDBClientV2 @Inject() (client: DynamoDbAsyncClient) extends Logging {

  // AWS SDK for Javaをスムーズに利用するためにimplicit conversionを利用
  implicit def javaFutureToScalaFuture[T](
      arg: CompletableFuture[T]
  ): Future[T] =
    arg.asScala

  implicit def scalaMapToJavaMap[T, U](
      arg: Map[T, U]
  ): JavaMap[T, U] =
    arg.asJava

  implicit def JavaListToScalaList[T](
      arg: JavaList[T]
  ): List[T] = arg.asScala.toList

  val table = "users"

  def list: Future[List[User]] = {
    logger.info("DDB list user.")
    val req = ScanRequest.builder().tableName(table).build()
    client
      .scan(req)
      .map(
        _.items()
          .map(convertToUser(_))
      )
  }

  def find(id: String): Future[Option[User]] = {
    val key = Map("user_id" -> toAttS(id)).asJava
    logger.info(s"DDB find user. id=${id}")
    val req = GetItemRequest.builder().tableName(table).key(key).build()
    client
      .getItem(req)
      .map(res =>
        if (res.item().isEmpty) {
          logger.warn(s"user not found.")
          None
        } else Some(convertToUser(res.item()))
      )
  }

  def put(user: User): Future[Unit] = {
    val item = Map(
      "user_id" -> toAttS(user.id),
      "user_name" -> toAttS(user.name),
      "user_age" -> toAttN(user.age)
    )
    logger.info(s"DDB put user. user=${item}")

    val req = PutItemRequest.builder().tableName(table).item(item).build()
    client
      .putItem(req)
      .transform(const((): Unit), identity)
  }

  def delete(id: String): Future[Unit] = {
    val key = Map("user_id" -> toAttS(id))
    logger.info(s"DDB delete user. id=${id}")
    val req = DeleteItemRequest.builder().tableName(table).key(key).build()
    client
      .deleteItem(req)
      .transform(const((): Unit), identity)
  }

  def update(id: String, u: UserUpdateRequest): Future[Unit] = {
    val updatedValues =
      List(
        ("user_name", u.name),
        ("user_age", u.age)
      )
        .filter(_._2.isDefined)
        // fixme 例外になるケースを追加する
        .map(_ match {
          case (s, Some(v: String)) if s == "user_name" =>
            (s -> (toUpdateAttS(v)))
          case (s, Some(v: Int)) if s == "user_age" =>
            (s -> (toUpdateAttN(v)))
        })
        .toMap

    val key = Map("user_id" -> toAttS(id)).asJava
    logger.info(
      s"DDB update user. id=${id}. updateValue=${updatedValues}"
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

  private def convertToUser(item: JavaMap[String, AttributeValue]): User = {
    User(
      item.get("user_id").s(),
      item.get("user_name").s(),
      item.get("user_age").n().toInt
    )
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
