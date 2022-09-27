package infra.dbclients

import javax.inject.Inject
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
  GetItemRequest,
  GetItemResponse
}

import java.net.URI
import java.util.HashMap
import scala.concurrent.Future
import scala.Function.const
import scala.concurrent.ExecutionContext.Implicits.global

/** user db client
  */
class UserDBClient @Inject() (dynamoDB: DynamoDB) {

  private val table = dynamoDB.getTable("users")

  /** create item of user to dynamodb
    * @param user user entity
    * @return empty Future
    */
  def createIfNotExist(user: User): Future[Unit] = {
    Future {
      table.putItem(
        new PutItemSpec()
          .withItem(toItem(user))
          .withConditionExpression("attribute_not_exists(user_id)")
      )
    }.transform(const((): Unit), identity)
  }

  def toItem(user: User): Item =
    new Item()
      .withPrimaryKey("user_id", user.id)
      .withString("name", user.name)
      .withInt("age", user.age)

  // NOTE: 以下を参考に実装
  // https://www.letitride.jp/entry/2020/06/06/225514
  def getByV2(id: String): GetItemResponse = {
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

    val keyToGet = new HashMap[String, AttributeValue]()
    keyToGet.put("user_id", AttributeValue.builder().s(id).build())

    val request =
      GetItemRequest.builder().key(keyToGet).tableName("users").build()
    dynamodb.getItem(request)
  }

}
