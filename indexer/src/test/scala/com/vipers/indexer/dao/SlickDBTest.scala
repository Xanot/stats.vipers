package com.vipers.indexer.dao

import com.jolbox.bonecp.BoneCPDataSource
import com.vipers.dbms.SlickDB
import com.vipers.indexer.Configuration
import scala.slick.driver.{H2Driver, JdbcProfile}
import scala.slick.jdbc.JdbcBackend.Database

private[dao] trait SlickDBTest extends SlickDB {
  override protected val db: Database = {
    Database.forDataSource {
      val dbUrl : String = Configuration.Database.url
      val driver : String = Configuration.Database.driver

      val ds : BoneCPDataSource = new BoneCPDataSource
      ds.setDriverClass(driver)
      ds.setJdbcUrl(dbUrl)
      ds.setPartitionCount(3)
      ds.setMinConnectionsPerPartition(5)
      ds.setMaxConnectionsPerPartition(20)
      ds.setAcquireIncrement(5)
      ds
    }
  }

  override protected val driver: JdbcProfile = H2Driver
}
