organization := "com.adamnfish"
version := "0.1-SNAPSHOT"
scalaVersion := "2.12.3"
scalacOptions ++= Seq(
  "-deprecation",
  "-Xfatal-warnings",
  "-encoding", "UTF-8",
  "-target:jvm-1.8",
  "-Ywarn-dead-code"
)

val awsSdkVersion = "1.11.185"
val http4sVersion = "0.17.6"

lazy val root = (project in file(".")).
  settings(
    name := "quack-stanley",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
      "com.amazonaws" % "aws-java-sdk-iam" % awsSdkVersion,
      "com.amazonaws" % "aws-java-sdk-s3" % awsSdkVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
      "joda-time" % "joda-time" % "2.9.9",
      "io.circe" %% "circe-parser" % "0.8.0",
      "io.circe" %% "circe-generic" % "0.8.0",
      "org.scalatest" %% "scalatest" % "3.0.4" % Test
    )
  )

lazy val devMode = (project in file("dev-server")).
  settings(
    name := "quack-stanley-dev-server",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    )
  ).
  dependsOn(root)

enablePlugins(JavaAppPackaging)
topLevelDirectory in Universal := None
packageName in Universal := normalizedName.value
