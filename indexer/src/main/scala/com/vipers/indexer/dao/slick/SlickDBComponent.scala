package com.vipers.indexer.dao.slick

import com.vipers.dbms.SlickDB
import com.vipers.indexer.Configuration
import com.vipers.indexer.dao.DBComponent
import com.zaxxer.hikari.HikariDataSource
import scala.slick.driver._
import scala.slick.jdbc.JdbcBackend
import scala.slick.jdbc.JdbcBackend.Database

private[indexer] trait SlickDBComponent extends DBComponent with SlickDB
  with SlickOutfitDAOComponent
  with SlickCharacterDAOComponent
  with SlickOutfitMembershipDAOComponent
  with SlickWeaponDAOComponent
  with SlickWeaponPropsDAOComponent
  with SlickWeaponStatDAOComponent
  with SlickCharacterStatDAOComponent {

  override protected val db: JdbcBackend.Database = {
    Database.forDataSource {
      val dbUrl : String = Configuration.Database.url
      val driver : String = Configuration.Database.driver

      val ds = new HikariDataSource
      ds.setDriverClassName(driver)

      Configuration.Database.user.map { u => ds.setUsername(u) }
      Configuration.Database.password.map { p => ds.setPassword(p) }

      ds.setJdbcUrl(dbUrl)
      ds.setMaximumPoolSize(Configuration.Database.maxPoolSize)
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

    withTransaction { implicit  s =>
      try { characterDAO.table.ddl.create }
      catch { case _ : Exception => }

      try { characterStatDAO.table.ddl.create }
      catch { case _ : Exception => }

      try { outfitDAO.table.ddl.create }
      catch { case _ : Exception => }

      try { outfitMembershipDAO.table.ddl.create }
      catch { case _ : Exception => }

      try { weaponDAO.table.ddl.create }
      catch { case _ : Exception => }

      try { weaponPropsDAO.table.ddl.create }
      catch { case _ : Exception => }

      try { weaponStatDAO.weaponStatsTable.ddl.create }
      catch { case _ : Exception => }

      try { weaponStatDAO.weaponStatsIndexed.ddl.create }
      catch { case _ : Exception => }

      try { weaponStatDAO.weaponStatsTimeSeriesTable.ddl.create }
      catch { case _ : Exception => }
    }
  }
}
