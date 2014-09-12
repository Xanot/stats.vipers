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

  lazy val web = Project("web", file("web"))
    .settings(basicSettings ++ assemblySettings ++ scalaMeterSettings:_*)
    .settings(libraryDependencies ++= akkaDependencies ++ commonDependencies ++ sprayDependencies ++ Seq(scalaMeter, _fetcher))

  lazy val common = Project("common", file("common"))
    .settings(basicSettings ++ localPublishSettings:_*)
    .settings(publishArtifact in (Test, packageBin) := true) // enable publishing the test API jar
    .settings(libraryDependencies ++= akkaDependencies ++ Seq(scalaTest, scalaMeter, logback, slick))
}