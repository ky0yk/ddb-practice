package infra.dbclients

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
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
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import domain.{User, UserUpdateRequest}
import software.amazon.awssdk.auth.credentials.{
  AwsBasicCredentials,
  StaticCredentialsProvider
}

import java.net.URI
import java.util.HashMap
import javax.inject.Inject
import scala.collection.mutable
import scala.jdk.CollectionConverters.{MapHasAsJava, MapHasAsScala}

/** user db client
  */
class UserDBClientV2 @Inject() (dynamoDB: DynamoDB) {

  val table = "users"

  val client = DynamoDbClient
    .builder()
    .endpointOverride(URI.create("http://localhost:8000"))
    .credentialsProvider(
      StaticCredentialsProvider.create(
        AwsBasicCredentials.create("dummy", "dummy")
      )
    )
    .region(Region.AP_NORTHEAST_1)
    .build()

  def list: ScanResponse = {
    val req = ScanRequest.builder().tableName(table).build()
    client.scan(req)
  }

  def find(id: String): GetItemResponse = {
    val key = Map("user_id" -> toAttS(id)).asJava
    val req =
      GetItemRequest.builder().tableName(table).key(key).build()
    client.getItem(req)
  }

  def put(user: User): PutItemResponse = {
    val item = Map(
      "user_id" -> toAttS(user.id),
      "user_name" -> toAttS(user.name),
      "user_age" -> toAttN(user.age)
    ).asJava

    val req =
      PutItemRequest.builder().tableName(table).item(item).build()
    client.putItem(req)
  }

  def delete(id: String): DeleteItemResponse = {
    val key = Map("user_id" -> toAttS(id)).asJava
    val req = DeleteItemRequest.builder().tableName(table).key(key).build()
    client.deleteItem(req)
  }

  def update(id: String, u: UserUpdateRequest) = {
    val updatedValues =
      List(
        toUpdateAttTuple("user_name", u.name),
        toUpdateAttTuple("user_age", u.age)
      )
        .filter(_ != None)
        .map { case (k: String, v: AttributeValueUpdate) =>
          (k, v)
        }
        .toMap
        .asJava

    // 更新する項目がない場合は何もしない
    if (updatedValues.isEmpty) {
      println("no update...")
    } else {
      val key = Map("user_id" -> AttributeValue.builder().s(id).build()).asJava
      val req =
        UpdateItemRequest
          .builder()
          .tableName(table)
          .key(key)
          .attributeUpdates(updatedValues)
          .build()
      client.updateItem(req)
      println("updated!")
    }
  }

  private def toAttS(v: String): AttributeValue =
    AttributeValue.builder().s(v).build()

  private def toAttN(v: Int): AttributeValue =
    AttributeValue.builder().s(v.toString).build()

  private def toUpdateAtt(
      attVal: AttributeValue
  ): AttributeValueUpdate = {
    AttributeValueUpdate
      .builder()
      .value(attVal)
      .action(AttributeAction.PUT)
      .build()
  }

  private def toUpdateAttTuple(field: String, value: Option[Any]) = {
    value match {
      case Some(v: String) =>
        (field -> toUpdateAtt(toAttS(v)))
      case Some(v: Int) =>
        (field -> toUpdateAtt(toAttN(v)))
      case _ => None
    }
  }

}
