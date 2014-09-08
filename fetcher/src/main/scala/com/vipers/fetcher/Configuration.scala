package com.vipers.fetcher

import com.typesafe.config.ConfigFactory

private[fetcher] object Configuration {
  val config = ConfigFactory.load("fetcher.conf")

  val apiIndex = config.getString("api-index")
}
