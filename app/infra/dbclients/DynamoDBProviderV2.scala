package infra.dbclients

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import play.api.Configuration
import software.amazon.awssdk.auth.credentials.{
  AwsBasicCredentials,
  StaticCredentialsProvider
}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

import java.net.URI
import javax.inject.{Inject, Provider}

/** DynamoDB provider
  */
class DynamoDBProviderV2 @Inject() (config: Configuration)
    extends Provider[DynamoDbAsyncClient] {

  /** create DynamoDB instance
    */
  override def get: DynamoDbAsyncClient = {
    DynamoDbAsyncClient
      .builder()
      .endpointOverride(URI.create("http://localhost:8000"))
      .credentialsProvider(
        StaticCredentialsProvider.create(
          AwsBasicCredentials.create("dummy", "dummy")
        )
      )
      .region(Region.AP_NORTHEAST_1)
      .build()
  }
}
