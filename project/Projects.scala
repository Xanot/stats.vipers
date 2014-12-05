import sbt._
import Keys._

object Projects extends Build {
  import Settings._
  import Dependencies._

  lazy val root = Project("root", file("."))
    .aggregate(common, fetcher, indexer, notifier, web)

  lazy val fetcher = Project("fetcher", file("fetcher"))
    .settings(basicSettings:_*)
    .settings(libraryDependencies ++= akkaDependencies ++ commonDependencies ++ sprayDependencies)
    .dependsOn(common % "test->test;compile->compile")

  lazy val indexer = Project("indexer", file("indexer"))
    .settings(basicSettings:_*)
    .settings(libraryDependencies ++= akkaDependencies ++ commonDependencies ++ databaseDependencies)
    .dependsOn(common % "test->test;compile->compile", fetcher, notifier)

  lazy val notifier = Project("notifier", file("notifier"))
    .settings(basicSettings:_*)
    .settings(libraryDependencies ++= akkaDependencies ++ commonDependencies ++ websocketDependencies ++ Seq(json4sNative))
    .dependsOn(common % "test->test;compile->compile")

  lazy val web = Project("web", file("web"))
    .settings(basicSettings:_*)
    .settings(libraryDependencies ++= akkaDependencies ++ commonDependencies ++ sprayDependencies)
    .dependsOn(common % "test->test;compile->compile", fetcher, notifier, indexer)

  lazy val common = Project("common", file("common"))
    .settings(basicSettings:_*)
    .settings(publishArtifact in (Test, packageBin) := true)
    .settings(libraryDependencies ++= akkaDependencies ++ databaseDependencies ++ Seq(scalaTest, logback))
}