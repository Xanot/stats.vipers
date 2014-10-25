package com.vipers

import com.typesafe.config.ConfigFactory

object Configuration {
  object Web {
    private lazy val config = ConfigFactory.load()
    val host = config.getString("web.host")
    val port = config.getInt("web.port")
    val allowOrigin = config.getString("web.allow-origin")
  }
}
