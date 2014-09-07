package com.vipers.model

import scala.{None => OptionNone}

case class Page(records : Option[Int], start : Option[Int])

object Page {
  val None = Page(OptionNone, OptionNone)
  val FirstFive = Page(Some(5), OptionNone)
  val FirstTen = Page(Some(10), OptionNone)
  val FirstFifty = Page(Some(50), OptionNone)
  val FirstHundred = Page(Some(100), OptionNone)
}
