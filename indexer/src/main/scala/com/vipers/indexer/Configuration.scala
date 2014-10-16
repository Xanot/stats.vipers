package com.vipers.indexer

import java.util.concurrent.TimeUnit
import com.typesafe.config.ConfigException.Missing
import com.typesafe.config.ConfigFactory

private[indexer] object Configuration {
  object Database {
    private lazy val config = ConfigFactory.load("database.conf")
    val dbms = config.getString("dbms")
    val maxPoolSize = config.getInt("max-pool-size")
    val driver = config.getString(s"$dbms.driver")
    val url = config.getString(s"$dbms.url")
    val user : Option[String] = {
      try { Some(config.getString(s"$dbms.user")) }
      catch { case _ : Missing => None }
    }
    val password : Option[String] = {
      try { Some(config.getString(s"$dbms.password")) }
      catch { case _ : Missing => None }
    }
  }

  private lazy val config = ConfigFactory.load("indexer.conf")
  val characterStaleAfter = config.getDuration("character-stale-after", TimeUnit.MILLISECONDS)
  val outfitStaleAfter = config.getDuration("outfit-stale-after", TimeUnit.MILLISECONDS)
  val weaponsStaleAfter = config.getDuration("weapons-stale-after", TimeUnit.MILLISECONDS)
}
