package appspecs.user.v1

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec
import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import org.specs2.mutable.After
import org.specs2.runner.JUnitRunner
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.http.MimeTypes.JSON
import play.api.test.{FakeRequest, PlaySpecification}
import play.twirl.api.TwirlHelperImports.twirlJavaCollectionToScala

@RunWith(classOf[JUnitRunner])
class PostAppSpec extends PlaySpecification with Mockito {

  trait UserTable {
    val dynamoDbClient = new AmazonDynamoDBClient()
    dynamoDbClient.setEndpoint("http://localhost:8000")
    val dynamoDB = new DynamoDB(dynamoDbClient)
    val userTable = dynamoDB.getTable("users")
  }

  trait ApplicationWithTable {
    self: UserTable with After =>

    val application = new GuiceApplicationBuilder().build()

    override def after: Any = {
      userTable.scan(new ScanSpec).toSeq.foreach { item =>
        userTable.deleteItem("user_id", item.getString("user_id"))
      }
    }
  }

  class Context extends After with UserTable with ApplicationWithTable

  "user post endpoint" should {

    val userJson =
      Json.obj("id" -> "someFakeKey", "name" -> "Chris", "age" -> 20)
    val fakeRequest =
      FakeRequest(POST, controllers.user.routes.UserController.post.url)
        .withHeaders((CONTENT_TYPE, JSON))
        .withJsonBody(userJson)

    "create response with 200 status code" in new Context {
      running(application) {
        val res = route(application, fakeRequest).get
        status(res) mustEqual OK
      }
    }

    "create user item" in new Context {
      running(application) {
        val res = route(application, fakeRequest).get
        await(res)
        val item = userTable.getItem("user_id", "someFakeKey")
        item.getString("name") mustEqual "Chris"
        item.getInt("age") mustEqual 20
      }
    }
  }
}
