ThisBuild / organization := "com.adamnfish"
ThisBuild / version := "0.1-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.12"
ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-Xfatal-warnings",
  "-encoding", "UTF-8",
  "-Ywarn-dead-code"
)
ThisBuild / libraryDependencies +=
  compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

val awsSdkVersion = "1.12.470"
val catsEffectVersion = "3.4.8"
val http4sVersion = "1.0.0-M30"

lazy val root = (project in file("."))
  .settings(
    name := "quack-stanley",
  )
  .aggregate(core, lambda, devServer)

lazy val core = (project in file("core"))
  .settings(
    name := "core",
    libraryDependencies ++= Seq(
      "joda-time" % "joda-time" % "2.12.5",
      "io.circe" %% "circe-parser" % "0.14.5",
      "io.circe" %% "circe-generic" % "0.14.5",
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.scalatest" %% "scalatest" % "3.2.15" % Test,
    ),
  )

lazy val lambda = (project in file("lambda"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "lambda",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-core" % "1.2.2",
      "com.amazonaws" % "aws-java-sdk-iam" % awsSdkVersion,
      "com.amazonaws" % "aws-java-sdk-s3" % awsSdkVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "ch.qos.logback" % "logback-classic" % "1.4.7",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
      "org.scalatest" %% "scalatest" % "3.2.15" % Test,
    ),
    Universal / topLevelDirectory := None,
    Universal / packageName := "quack-stanley",
  )
  .dependsOn(core % "compile->compile;test->test")

lazy val devServer = (project in file("dev-server"))
  .settings(
    name := "dev-server",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "ch.qos.logback" % "logback-classic" % "1.4.7",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
    ),
    run / fork := true,
    run / connectInput := true,
    outputStrategy := Some(StdoutOutput),
  )
  .dependsOn(core)

