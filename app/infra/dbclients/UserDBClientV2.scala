package infra.dbclients

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{
  AttributeAction,
  AttributeValue,
  AttributeValueUpdate,
  DeleteItemRequest,
  DynamoDbException,
  GetItemRequest,
  PutItemRequest,
  PutItemResponse,
  ScanRequest,
  UpdateItemRequest
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
import scala.collection.IterableOnce.iterableOnceExtensionMethods
import scala.collection.mutable
import scala.jdk.CollectionConverters.{MapHasAsJava, MapHasAsScala}

/** user db client
  */
class UserDBClientV2 @Inject() (dynamoDB: DynamoDB) {

  val table = "users"

  val dynamodb = DynamoDbClient
    .builder()
    .endpointOverride(URI.create("http://localhost:8000"))
    .credentialsProvider(
      StaticCredentialsProvider.create(
        AwsBasicCredentials.create("a", "a")
      )
    )
    .region(Region.AP_NORTHEAST_1)
    .build()

  def get(id: String): mutable.Map[String, AttributeValue] = {
    val keyToGet = new HashMap[String, AttributeValue]()
    keyToGet.put("user_id", AttributeValue.builder().s(id).build())

    val req =
      GetItemRequest.builder().key(keyToGet).tableName(table).build()
    dynamodb.getItem(req).item().asScala
  }

  def put(user: User): PutItemResponse = {
    val keyToPut = Map(
      "user_id" -> AttributeValue.builder().s(user.id).build(),
      "user_name" -> AttributeValue.builder().s(user.name).build(),
      "user_age" -> AttributeValue.builder().n(user.age.toString).build()
    ).asJava

    val req =
      PutItemRequest.builder().item(keyToPut).tableName(table).build()
    dynamodb.putItem(req)
  }

  def list = {
    val req = ScanRequest.builder().tableName(table).build()
    dynamodb.scan(req).items()
  }

  def delete(id: String) = {
    val keyToGet = Map(
      "user_id" -> AttributeValue.builder().s(id).build()
    ).asJava

    val req = DeleteItemRequest.builder().key(keyToGet).tableName(table).build()
    dynamodb.deleteItem(req)
  }

  def update(id: String, u: UserUpdateRequest) = {

    val key = Map("user_id" -> AttributeValue.builder().s(id).build()).asJava

    val name =
      u.name match {
        case Some(v) => {
          val updateVal = AttributeValue.builder().s(v).build()
          ("user_name" -> AttributeValueUpdate
            .builder()
            .value(updateVal)
            .action(AttributeAction.PUT)
            .build())
        }
        case None => None
      }

    val age =
      u.age match {
        case Some(v) =>
          ("user_age" -> AttributeValueUpdate
            .builder()
            .value(AttributeValue.builder().s(v.toString).build()))
        case None => None
      }

    val updatedValue = List(name, age)
      .filter(_ != None)
      .map { case (k: String, v: AttributeValueUpdate) =>
        (k, v)
      }
      .toMap
      .asJava

    val req =
      UpdateItemRequest
        .builder()
        .tableName(table)
        .key(key)
        .attributeUpdates(updatedValue)
        .build()
    dynamodb.updateItem(req)
  }
}
