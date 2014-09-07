package com.vipers

import com.typesafe.config.ConfigFactory

object Configuration {
  val config = ConfigFactory.load("fetcher.conf")

  val apiIndex = config.getString("api-index")
}
