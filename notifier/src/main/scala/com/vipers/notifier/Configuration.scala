package com.vipers.notifier

import java.util.concurrent.TimeUnit
import com.typesafe.config.ConfigFactory

private[notifier] object Configuration {
  object WebSocket {
    lazy val config = ConfigFactory.load("notifier.conf")
    val host = config.getString("websocket.host")
    val port = config.getInt("websocket.port")
    val pingInterval = config.getDuration("websocket.ping-interval", TimeUnit.MILLISECONDS)
  }
}
