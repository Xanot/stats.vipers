package com.vipers.indexer.dao

import com.jolbox.bonecp.BoneCPDataSource
import com.vipers.dao.DAOTest
import com.vipers.dbms.SlickDB
import com.vipers.indexer.Configuration
import org.h2.tools.Server
import org.scalatest.WordSpecLike
import scala.slick.driver.{H2Driver, JdbcProfile}
import scala.slick.jdbc.JdbcBackend.Database

class SlickOutfitDAOTest extends WordSpecLike with DAOTest with SlickDB {
  override protected val db: Database = {
    Database.forDataSource {
      val dbUrl : String = Configuration.Database.url
      val driver : String = "org.h2.Driver"
      Server.createTcpServer().start()

      val ds : BoneCPDataSource = new BoneCPDataSource
      ds.setDriverClass(driver)
      ds.setJdbcUrl(dbUrl)
      ds.setPartitionCount(3)
      ds.setMinConnectionsPerPartition(5)
      ds.setMaxConnectionsPerPartition(20)
      ds
    }
  }

  override protected val driver: JdbcProfile = H2Driver

  "" should {
    "connect" in {
      withSession { implicit s =>
        s.conn.isClosed should be(false)
      }
    }
  }
}
