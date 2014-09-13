package com.vipers.indexer

import com.typesafe.config.ConfigException.Missing
import com.typesafe.config.ConfigFactory

private[indexer] object Configuration {
  object Database {
    private lazy val config = ConfigFactory.load("database.conf")
    val dbms = config.getString("dbms")
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
}
