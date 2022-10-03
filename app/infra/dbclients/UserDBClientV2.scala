package infra.dbclients

import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

import software.amazon.awssdk.services.dynamodb.model.{
  AttributeAction,
  AttributeValue,
  AttributeValueUpdate,
  DeleteItemRequest,
  DeleteItemResponse,
  DynamoDbException,
  GetItemRequest,
  GetItemResponse,
  PutItemRequest,
  PutItemResponse,
  ScanRequest,
  ScanResponse,
  UpdateItemRequest,
  UpdateItemResponse
}

import domain.{User, UserUpdateRequest}
import javax.inject.Inject
import scala.concurrent.Future
import scala.jdk.CollectionConverters.MapHasAsJava
import scala.jdk.FutureConverters.CompletionStageOps

/** user db client
  */
class UserDBClientV2 @Inject() (client: DynamoDbAsyncClient) {

  private val table = "users"

  def list: Future[ScanResponse] = {
    val req = ScanRequest.builder().tableName(table).build()
    client.scan(req).asScala
  }

  def find(id: String): Future[GetItemResponse] = {
    val key = Map("user_id" -> toAttS(id)).asJava
    val req =
      GetItemRequest.builder().tableName(table).key(key).build()
    client.getItem(req).asScala
  }

  def put(user: User): Future[PutItemResponse] = {
    val item = Map(
      "user_id" -> toAttS(user.id),
      "user_name" -> toAttS(user.name),
      "user_age" -> toAttN(user.age)
    ).asJava

    val req =
      PutItemRequest.builder().tableName(table).item(item).build()
    client.putItem(req).asScala
  }

  def delete(id: String): Future[DeleteItemResponse] = {
    val key = Map("user_id" -> toAttS(id)).asJava
    val req = DeleteItemRequest.builder().tableName(table).key(key).build()
    client.deleteItem(req).asScala
  }

  def update(id: String, u: UserUpdateRequest): Future[UpdateItemResponse] = {
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
        .asJava

    val key = Map("user_id" -> toAttS(id)).asJava
    val req =
      UpdateItemRequest
        .builder()
        .tableName(table)
        .key(key)
        .attributeUpdates(updatedValues)
        .build()
    client.updateItem(req).asScala
  }

  private def toAttS(v: String): AttributeValue =
    AttributeValue.builder().s(v).build()

  private def toAttN(v: Int): AttributeValue =
    AttributeValue.builder().s(v.toString).build()

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
