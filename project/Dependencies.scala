import sbt._

object Dependencies {
  private val akkaVersion  = "2.3.5"
  private val sprayVersion = "1.3.1"

  val commonDeps     = "com.vipers"              %% "common"                 %  "[0.1-SNAPSHOT,)"
  val commonTestDeps = "com.vipers"              %% "common"                 %  "[0.1-SNAPSHOT,)"  % "test" classifier "tests"

  val logback        = "ch.qos.logback"          %  "logback-classic"        %  "[1.0.13,)"
  val scalaMeter     = "com.storm-enroute"       %% "scalameter"             % "0.7-SNAPSHOT"      % "test"
  val jbcrypt        = "org.mindrot"             %  "jbcrypt"                %  "[0.3m,)"
  val json4sNative   = "org.json4s"              %% "json4s-native"          % "3.2.10"
  val slick          = "com.typesafe.slick"      %% "slick"                  %  "[2.1.0,)"

  val akkaActor      = "com.typesafe.akka"       %% "akka-actor"             %  akkaVersion
  val akkaSlf4j      = "com.typesafe.akka"       %% "akka-slf4j"             %  akkaVersion
  val scalaTest      = "org.scalatest"           %% "scalatest"              %  "[2.1.3,)"         % "test"
  val akkaTest       = "com.typesafe.akka"       %% "akka-testkit"           %  akkaVersion        % "test"

  val sprayCan       = "io.spray"                %% "spray-can"              %  sprayVersion
  val sprayCaching   = "io.spray"                %% "spray-caching"          %  sprayVersion
  val sprayRouting   = "io.spray"                %% "spray-routing"          %  sprayVersion
  val sprayHttp      = "io.spray"                %% "spray-http"             %  sprayVersion
  val sprayTestkit   = "io.spray"                %% "spray-testkit"          %  sprayVersion       % "test"

  val akkaDependencies = Seq(akkaActor, akkaSlf4j, akkaTest)
  val sprayDependencies = Seq(sprayCan, sprayRouting, sprayCaching, sprayHttp, sprayTestkit, json4sNative)
  val commonDependencies = Seq(commonDeps, commonTestDeps, scalaTest, logback)
}