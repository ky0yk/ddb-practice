package modules

import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.google.inject.AbstractModule
import controllers.user.UserController
import infra.dbclients.v1.{DynamoDBProvider, UserDBClient}
import infra.dbclients.v2.DynamoDBProviderV2
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

/** resolves DI
  */
class AppDependencyModule extends AbstractModule {

  override def configure() = {
    bind(classOf[DynamoDB]).toProvider(classOf[DynamoDBProvider]) // -- (1)
    bind(classOf[DynamoDbAsyncClient]).toProvider(
      classOf[DynamoDBProviderV2]
    )

  }
}
