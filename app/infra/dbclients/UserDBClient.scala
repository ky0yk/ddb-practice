package infra.dbclients

import javax.inject.Inject
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec
import com.amazonaws.services.dynamodbv2.document.{DynamoDB, Item}
import domain.User

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
}
