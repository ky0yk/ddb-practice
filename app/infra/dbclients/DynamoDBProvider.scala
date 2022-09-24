package infra.dbclients

import javax.inject.{Provider, Inject}

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import play.api.Configuration

/** DynamoDB provider
  */
class DynamoDBProvider @Inject() (config: Configuration)
    extends Provider[DynamoDB] {

  /** create DynamoDB instance
    */
  override def get: DynamoDB = {
    val client = new AmazonDynamoDBClient()
    client.setEndpoint(config.underlying.getString("dynamoDB.endPoint"))
    new DynamoDB(client)
  }
}
