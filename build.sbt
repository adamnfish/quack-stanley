ThisBuild / organization := "com.adamnfish"
ThisBuild / version := "0.1-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.3"
ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-Xfatal-warnings",
  "-encoding", "UTF-8",
  "-target:jvm-1.8",
  "-Ywarn-dead-code"
)

val awsSdkVersion = "1.11.900"

lazy val root = (project in file("."))
  .settings(
    name := "quack-stanley",
  )
  .aggregate(core, lambda, devServer)

lazy val core = (project in file("core"))
  .settings(
    name := "core",
    libraryDependencies ++= Seq(
      "joda-time" % "joda-time" % "2.10.8",
      "io.circe" %% "circe-parser" % "0.12.3",
      "io.circe" %% "circe-generic" % "0.12.3",
      "org.scalatest" %% "scalatest" % "3.2.2" % Test,
    ),
  )

lazy val lambda = (project in file("lambda"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "lambda",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-core" % "1.2.1",
      "com.amazonaws" % "aws-java-sdk-iam" % awsSdkVersion,
      "com.amazonaws" % "aws-java-sdk-s3" % awsSdkVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      "org.scalatest" %% "scalatest" % "3.2.2" % Test,
    ),
    topLevelDirectory in Universal := None,
    packageName in Universal := "quack-stanley",
  )
  .dependsOn(core % "compile->compile;test->test")

lazy val devServer = (project in file("dev-server"))
  .settings(
    name := "dev-server",
    libraryDependencies ++= Seq(
      "io.javalin" % "javalin" % "3.11.0",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    ),
    fork in run := true,
    connectInput in run := true,
    outputStrategy := Some(StdoutOutput),
  )
  .dependsOn(core)

