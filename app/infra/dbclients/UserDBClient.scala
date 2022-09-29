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
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters.MapHasAsScala

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

}
