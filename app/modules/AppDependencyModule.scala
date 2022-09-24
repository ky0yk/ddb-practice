package modules

import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.google.inject.AbstractModule
import controllers.user.UserController
import infra.dbclients.{DynamoDBProvider, UserDBClient}

/** resolves DI
  */
class AppDependencyModule extends AbstractModule {

  override def configure() = {
    bind(classOf[DynamoDB]).toProvider(classOf[DynamoDBProvider]) // -- (1)
  }
}
