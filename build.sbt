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

lazy val root = (project in file(".")).
  settings(
    name := "quack-stanley",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
      "joda-time" % "joda-time" % "2.9.9",
      "io.circe" %% "circe-parser" % "0.7.0",
      "io.circe" %% "circe-generic-extras_sjs0.6" % "0.7.0"
        exclude("org.typelevel", "cats-core_sjs0.6_2.11" )
        exclude("com.chuusai", "shapeless_sjs0.6_2.11"),
      "org.scalatest" %% "scalatest" % "3.0.4" % Test
    )
  )

enablePlugins(JavaAppPackaging)
topLevelDirectory in Universal := None
packageName in Universal := normalizedName.value
