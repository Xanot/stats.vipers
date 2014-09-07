package com.vipers

import org.scalatest.{BeforeAndAfterAll, Matchers, Suite}

trait Test extends BeforeAndAfterAll with Matchers { this: Suite => }
