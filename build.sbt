ThisBuild / scalaVersion := "2.13.9"

ThisBuild / version := "1.0-SNAPSHOT"

val awsSDKV2Version = "2.17.255"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """ddb-practice""",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
      "com.amazonaws" % "aws-java-sdk" % "1.10.1",
      "software.amazon.awssdk" % "dynamodb" % awsSDKV2Version
    )
  )
