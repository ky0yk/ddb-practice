package infra.dbclients

import com.amazonaws.services.dynamodbv2.document.{DynamoDB, Item}
import domain.User
import software.amazon.awssdk.auth.credentials.{
  AwsBasicCredentials,
  StaticCredentialsProvider
}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{
  AttributeValue,
  GetItemRequest,
  PutItemRequest,
  PutItemResponse,
  ScanRequest
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

  // NOTE: 以下を参考に実装
  // https://www.letitride.jp/entry/2020/06/06/225514
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

  def delete = ???

  def patch = ???
}
