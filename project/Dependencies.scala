import sbt._

object Dependencies {
  private val akkaVersion      = "2.3.7"
  private val sprayVersion     = "1.3.2"
  private val jettyVersion     = "9.2.3.v20140905"
  private val scalaMockVersion = "3.2"
  private val selfVersion      = "0.5-SNAPSHOT"

  val _common         = "com.vipers"             %% "common"                 %  selfVersion
  val _commonTest     = "com.vipers"             %% "common"                 %  selfVersion        % "test" classifier "tests"
  val _fetcher        = "com.vipers"             %% "fetcher"                %  selfVersion
  val _indexer        = "com.vipers"             %% "indexer"                %  selfVersion
  val _notifier       = "com.vipers"             %% "notifier"               %  selfVersion

  val logback        = "ch.qos.logback"          %  "logback-classic"        %  "[1.0.13,)"
  val scalaMeter     = "com.storm-enroute"       %% "scalameter"             %  "0.7-SNAPSHOT"     % "test"
  val json4sNative   = "org.json4s"              %% "json4s-native"          %  "[3.2.11,)"
  val slick          = "com.typesafe.slick"      %% "slick"                  %  "2.1.0"
  val hikariCp       = "com.zaxxer"              %  "HikariCP-java6"         %  "2.2.4"
  val h2             = "com.h2database"          %  "h2"                     %  "1.4.182"
  val akkaActor      = "com.typesafe.akka"       %% "akka-actor"             %  akkaVersion
  val akkaSlf4j      = "com.typesafe.akka"       %% "akka-slf4j"             %  akkaVersion
  val scalaTest      = "org.scalatest"           %% "scalatest"              %  "2.2.2"            % "test"
  val akkaTest       = "com.typesafe.akka"       %% "akka-testkit"           %  akkaVersion        % "test"

  val sprayCan       = "io.spray"                %% "spray-can"              %  sprayVersion
  val sprayCaching   = "io.spray"                %% "spray-caching"          %  sprayVersion
  val sprayRouting   = "io.spray"                %% "spray-routing"          %  sprayVersion
  val sprayHttp      = "io.spray"                %% "spray-http"             %  sprayVersion
  val sprayTestkit   = "io.spray"                %% "spray-testkit"          %  sprayVersion       % "test"

  val jettyWebsocketServer = "org.eclipse.jetty.websocket" % "websocket-server"  % jettyVersion
  val jettyWebsocketApi    = "org.eclipse.jetty.websocket" % "websocket-api"     % jettyVersion
  val websocketTest        = "com.ning"                    % "async-http-client" % "1.8.14"        % "test"

  val scalaMock            = "org.scalamock" %% "scalamock-core"              % scalaMockVersion % "test"
  val scalaMockSupport     = "org.scalamock" %% "scalamock-scalatest-support" % scalaMockVersion % "test"

  val akkaDependencies = Seq(akkaActor, akkaSlf4j, akkaTest)
  val sprayDependencies = Seq(sprayCan, sprayRouting, sprayCaching, sprayHttp, sprayTestkit, json4sNative)
  val commonDependencies = Seq(_common, _commonTest, scalaTest, scalaMock, scalaMockSupport, logback)
  val databaseDependencies = Seq(slick, hikariCp, h2)
  val websocketDependencies = Seq(jettyWebsocketApi, jettyWebsocketServer, websocketTest)
}