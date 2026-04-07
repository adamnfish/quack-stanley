ThisBuild / organization := "com.adamnfish"
ThisBuild / version := "0.1-SNAPSHOT"
ThisBuild / scalaVersion := "3.8.3"
ThisBuild / scalacOptions ++= Seq(
  // format: off
  "-deprecation",
  "-Werror",
  "-encoding", "UTF-8",
  // format: on
)

val awsSdkVersion = "2.42.29"
val catsEffectVersion = "3.7.0"
val http4sVersion = "1.0.0-M44"
val http4sBlazeVersion = "1.0.0-M41"
val log4catsVersion = "2.8.0"

lazy val root = (project in file("."))
  .settings(
    name := "quack-stanley"
  )
  .aggregate(core, lambda, devServer)

lazy val core = (project in file("core"))
  .settings(
    name := "core",
    libraryDependencies ++= Seq(
      "joda-time" % "joda-time" % "2.14.1",
      "io.circe" %% "circe-parser" % "0.14.15",
      "io.circe" %% "circe-generic" % "0.14.15",
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.scalatest" %% "scalatest" % "3.2.20" % Test
    )
  )

lazy val lambda = (project in file("lambda"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "lambda",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-core" % "1.4.0",
      "software.amazon.awssdk" % "s3" % awsSdkVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "ch.qos.logback" % "logback-classic" % "1.5.32",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.6",
      "org.scalatest" %% "scalatest" % "3.2.20" % Test
    ),
    Universal / topLevelDirectory := None,
    Universal / packageName := "quack-stanley"
  )
  .dependsOn(core % "compile->compile;test->test")

lazy val devServer = (project in file("dev-server"))
  .settings(
    name := "dev-server",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sBlazeVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sBlazeVersion,
      "org.typelevel" %% "log4cats-slf4j" % log4catsVersion,
      "ch.qos.logback" % "logback-classic" % "1.5.32",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.6"
    ),
    run / fork := true,
    run / connectInput := true,
    outputStrategy := Some(StdoutOutput)
  )
  .dependsOn(core)
