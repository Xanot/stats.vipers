package com.vipers.dbms

import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.driver.JdbcProfile

trait SlickDB extends DB {
  import driver.simple.{Session => SlickSession}

  protected val db : Database
  protected val driver : JdbcProfile

  override type Session = SlickSession

  override def withSession[T](f: (Session) => T): T = db.withSession(f)
  override def withTransaction[T](f: (Session) => T): T = db.withTransaction(f)
}