package com.vipers.indexer.dao

import com.vipers.dbms.SlickDB
import com.vipers.indexer.Configuration
import com.zaxxer.hikari.HikariDataSource
import org.scalatest.BeforeAndAfterAll
import scala.slick.driver.{H2Driver, JdbcProfile}
import scala.slick.jdbc.JdbcBackend.Database

private[dao] trait SlickDBTest extends SlickDB { this: BeforeAndAfterAll =>
  override protected val db: Database = {
    Database.forDataSource {
      val dbUrl : String = Configuration.Database.url
      val driver : String = Configuration.Database.driver

      val ds = new HikariDataSource
      ds.setDriverClassName(driver)
      ds.setJdbcUrl(dbUrl)
      ds.setMaximumPoolSize(Configuration.Database.maxPoolSize)
      ds
    }
  }

  override protected val driver: JdbcProfile = H2Driver

  protected val ddl : driver.DDL

  import driver.simple._
  override def beforeAll(): Unit = {
    withTransaction { implicit s =>
      ddl.create
    }
  }

  override def afterAll(): Unit = {
    withTransaction { implicit s =>
      ddl.drop
    }
  }
}
