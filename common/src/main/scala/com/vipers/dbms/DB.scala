package com.vipers.dbms

trait DB {
  type Session

  def withSession[T](f : Session => T) : T
  def withTransaction[T](f : Session => T) : T
}
