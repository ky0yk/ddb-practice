package infra.dbclients.v2

import java.util.concurrent.CompletableFuture
import scala.concurrent.Future
import scala.jdk.FutureConverters.CompletionStageOps
import java.util.{List => JavaList, Map => JavaMap}
import scala.jdk.CollectionConverters.{
  CollectionHasAsScala,
  MapHasAsJava,
  MapHasAsScala
}

object TypeConverter {
  implicit def javaFutureToScalaFuture[T](
      arg: CompletableFuture[T]
  ): Future[T] =
    arg.asScala

  implicit def scalaMapToJavaMap[T, U](
      arg: Map[T, U]
  ): JavaMap[T, U] =
    arg.asJava

  implicit def javaMapToScalaMap[T, U](
      arg: JavaMap[T, U]
  ): Map[T, U] =
    arg.asScala.toMap

  implicit def JavaListToScalaList[T](
      arg: JavaList[T]
  ): List[T] = arg.asScala.toList
}
