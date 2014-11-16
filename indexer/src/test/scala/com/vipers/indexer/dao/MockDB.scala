package com.vipers.indexer.dao

import com.vipers.dbms.DB

trait MockDB extends DB {
  override type Session = Option[AnyRef]
  override def withSession[T](f: (Session) => T): T = f(None)
  override def withTransaction[T](f: (Session) => T): T = f(None)
}
