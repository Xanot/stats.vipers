import sbt._
import Keys._
import sbtassembly.Plugin.assemblySettings

object Projects extends Build {
  import Settings._
  import Dependencies._

  lazy val root = Project("root", file("."))
    .aggregate(fetcher, common)

  lazy val fetcher = Project("fetcher", file("fetcher"))
    .settings(basicSettings ++ localPublishSettings ++ scalaMeterSettings:_*)
    .settings(libraryDependencies ++= akkaDependencies ++ commonDependencies ++ sprayDependencies ++ Seq(scalaMeter))

  lazy val indexer = Project("indexer", file("indexer"))
    .settings(basicSettings ++ localPublishSettings ++ scalaMeterSettings:_*)
    .settings(libraryDependencies ++= akkaDependencies ++ commonDependencies ++ Seq(slick, scalaMeter, _fetcher))

  lazy val web = Project("web", file("web"))
    .settings(basicSettings ++ assemblySettings ++ scalaMeterSettings:_*)
    .settings(libraryDependencies ++= akkaDependencies ++ commonDependencies ++ sprayDependencies ++ Seq(scalaMeter, _fetcher, _indexer))

  lazy val common = Project("common", file("common"))
    .settings(basicSettings ++ localPublishSettings:_*)
    .settings(publishArtifact in (Test, packageBin) := true)
    .settings(libraryDependencies ++= akkaDependencies ++ Seq(scalaTest, scalaMeter, logback, slick))
}