package infra.dbClient.v1

package infrastructure.dbclients

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec
import domain.User
import infra.dbclients.v1.UserDBClient
import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import org.specs2.mutable.BeforeAfter
import org.specs2.runner.JUnitRunner
import play.api.test.PlaySpecification
import play.twirl.api.TwirlHelperImports.twirlJavaCollectionToScala

@RunWith(classOf[JUnitRunner])
class UserDBClientSpec extends PlaySpecification with Mockito {

  trait DBClients {
    val dynamoDBClient = new AmazonDynamoDBClient()
    dynamoDBClient.setEndpoint("http://localhost:8000")
    val dynamoDB = new DynamoDB(dynamoDBClient)
    val userTable = dynamoDB.getTable("users")
    val userDBClient = new UserDBClient(dynamoDB)
  }

  trait DBClientBeforeAfter extends BeforeAfter {
    self: DBClients =>

    override def before = ()

    override def after = {
      userTable.scan(new ScanSpec).toSeq.foreach { item =>
        userTable.deleteItem("user_id", item.getString("user_id"))
      }
    }
  }

  trait UserArgs {
    val user = User("userKey", "John", 50)
  }

  "Creating user" >> {

    "if table han no item with the same key as user" should {

      class Context extends DBClientBeforeAfter with UserArgs with DBClients

      "succeed" in new Context {
        val future = userDBClient.createIfNotExist(user)
        await(future) must not(throwA[Throwable])
      }

      "create one item to dynamoDB" in new Context {
        val future = userDBClient.createIfNotExist(user)
        await(future)
        userTable.scan(new ScanSpec).toSeq.length mustEqual 1
      }

      "create item that matches with user" in new Context {
        val future = userDBClient.createIfNotExist(user)
        await(future)
        val item = userTable.getItem("user_id", user.id)

        item.getString("name") mustEqual user.name
        item.getInt("age") mustEqual user.age
      }
    }

    "if table has item with the same key as user" should {

      class OneItemContext
          extends DBClientBeforeAfter
          with UserArgs
          with DBClients {
        override def before = {
          val otherUser = User(user.id, "Chris", 40)
          val future = userDBClient.createIfNotExist(otherUser)
          await(future)
        }
      }

      "fail" in new OneItemContext {
        val future = userDBClient.createIfNotExist(user)
        await(future) must throwA[Throwable]
      }
    }
  }
}
