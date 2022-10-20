package modules

import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.google.inject.AbstractModule
import infra.dbclients.v1.{DynamoDBProvider}
import infra.dbclients.v2.{DynamoDBProviderV2, UserDBClientV2}
import services.user.UserStore
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

/** resolves DI
  */
class AppDependencyModule extends AbstractModule {

  override def configure() = {
    bind(classOf[DynamoDB]).toProvider(classOf[DynamoDBProvider])
    bind(classOf[DynamoDbAsyncClient]).toProvider(
      classOf[DynamoDBProviderV2]
    )
    bind(classOf[UserStore]).to(classOf[UserDBClientV2])

  }
}
