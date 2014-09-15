package com.vipers.indexer.dao.slick

import com.jolbox.bonecp.BoneCPDataSource
import com.vipers.dbms.SlickDB
import com.vipers.indexer.Configuration
import scala.slick.driver._
import scala.slick.jdbc.JdbcBackend
import scala.slick.jdbc.JdbcBackend.Database

private[indexer] class SlickDBComponent extends SlickDB with SlickOutfitDAOComponent {
  override protected val db: JdbcBackend.Database = {
    Database.forDataSource {
      val dbUrl : String = Configuration.Database.url
      val driver : String = Configuration.Database.driver

      val ds : BoneCPDataSource = new BoneCPDataSource
      ds.setDriverClass(driver)
      ds.setJdbcUrl(dbUrl)
      ds.setPartitionCount(3)
      ds.setMinConnectionsPerPartition(5)
      ds.setMaxConnectionsPerPartition(20)
      ds
    }
  }
  override protected val driver: JdbcProfile = {
    Configuration.Database.dbms match {
      case "h2" => H2Driver
      case "mysql" => MySQLDriver
      case "postgres" => PostgresDriver
      case "sqlite" => SQLiteDriver
      case "derby" => DerbyDriver
      case "hsqldb" => HsqldbDriver
    }
  }

  { // Create tables
    import driver.simple._
    try {
      withTransaction { implicit s =>
        outfitDAO.table.ddl.create
      }
    } catch {
      case _ : Exception => // Tables already created
    }
  }
}
