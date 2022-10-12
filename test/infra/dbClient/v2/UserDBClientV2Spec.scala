package infra.dbClient.v2

import software.amazon.awssdk.services.dynamodb.model._
import domain.User
import org.specs2.mock.Mockito
import play.api.test.PlaySpecification
import infra.dbclients.v2.UserDBClientV2
import org.specs2.mutable.BeforeAfter
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

import java.net.URI
import scala.jdk.CollectionConverters.MapHasAsJava

// AWS SDK for Javaをスムーズに利用するためにimplicit conversionを利用
import infra.dbclients.v2.TypeConverter._

class UserDBClientV2Spec extends PlaySpecification with Mockito {

  trait DBClients {
    val dynamoDbClient = DynamoDbAsyncClient
      .builder()
      .endpointOverride(URI.create("http://localhost:8000"))
      .build()
    val userDBClient = new UserDBClientV2(dynamoDbClient)
    val table = "users"
    val ScanReq = ScanRequest.builder().tableName(table).build()
  }

  trait DBClientBeforeAfter extends BeforeAfter {
    self: DBClients =>

    override def before = ()

    override def after = {
      await(dynamoDbClient.scan(ScanReq)).items().toSeq.foreach { item =>
        val deleteReq = DeleteItemRequest
          .builder()
          .tableName(table)
          .key(Map("user_id" -> item.get("user_id")))
          .build()
        dynamoDbClient.deleteItem(deleteReq)
      }
    }
  }

  trait UserArgs {
    val user = User("userKey", "John", 50)
  }

  "ユーザーの作成" >> {

    "テーブルに同じIDが存在しない場合" should {

      class Context extends DBClientBeforeAfter with UserArgs with DBClients

      "成功する" in new Context {
        val future = userDBClient.create(user)
        await(future) must not(throwA[Throwable])
      }

      "アイテムが1つDDBに作成される" in new Context {
        val future = userDBClient.create(user)
        await(future)
        await(dynamoDbClient.scan(ScanReq)).items().toSeq.length mustEqual 1
      }

      "ユーザーと一致するアイテムを作成する" in new Context {
        val future = userDBClient.create(user)
        await(future)

        val getReq = GetItemRequest
          .builder()
          .tableName(table)
          .key(Map("user_id" -> AttributeValue.builder().s(user.id).build()))
          .build()
        val item = await(dynamoDbClient.getItem(getReq))

        item.item().get("user_name").s() mustEqual user.name
        item.item().get("user_age").n().toInt mustEqual user.age
      }
    }
  }
}
