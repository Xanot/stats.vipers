import sbt._
import Keys._
import net.virtualvoid.sbt.graph.Plugin.graphSettings

object Settings {
  lazy val basicSettings = Seq(
    scalaVersion := "2.11.4",
    organization := "com.vipers",
    scalacOptions := Seq(
      "-encoding",
      "utf8",
      "-feature",
      "-unchecked",
      "-deprecation",
      "-language:postfixOps"
      ),
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    parallelExecution in Test := false
  ) ++ graphSettings

  lazy val scalaMeterSettings = Seq(
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
    logBuffered := false
  )

  lazy val localPublishSettings = Seq(
    publishArtifact in (Test, packageDoc) := false,
    publishArtifact in (Compile, packageDoc) := false
  )
}