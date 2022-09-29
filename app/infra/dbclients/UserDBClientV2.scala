package infra.dbclients

import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec
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
  GetItemRequest
}

import java.net.URI
import java.util.HashMap
import javax.inject.Inject
import scala.Function.const
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters.MapHasAsScala

/** user db client
  */
class UserDBClientV2 @Inject() (dynamoDB: DynamoDB) {

  private val table = dynamoDB.getTable("users")

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
      GetItemRequest.builder().key(keyToGet).tableName("users").build()
    dynamodb.getItem(req).item().asScala
  }

}
