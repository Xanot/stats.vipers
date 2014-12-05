import sbt._
import Keys._

object Settings {
  lazy val basicSettings = Seq(
    scalaVersion := "2.11.4",
    version := "0.5-SNAPSHOT",
    organization := "com.vipers",
    scalacOptions := Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-unchecked",
      "-Xfatal-warnings",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Xfuture"
    ),
    parallelExecution in Test := false
  )
}