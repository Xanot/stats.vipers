package com.vipers.fetcher.model

import scala.{None => OptionNone}

case class Page(records : Option[Int], start : Option[Int])

object Page {
  val None = Page(OptionNone, OptionNone)
  val FirstFive = Page(Some(5), OptionNone)
  val FirstTen = Page(Some(10), OptionNone)
  val FirstFifty = Page(Some(50), OptionNone)
  val FirstHundred = Page(Some(100), OptionNone)
}

object Sort {
  type Sort = (Criteria, Order)

  sealed trait Order
  case object DESC extends Order
  case object ASC  extends Order

  sealed trait Criteria
  case object CREATION_DATE extends Criteria
}
