package com.vipers.indexer

import java.util.concurrent.TimeUnit
import akka.actor.Actor
import akka.util.Timeout
import com.vipers.Logging

class IndexerActor extends Actor with Logging {
  def receive = {
    case _ =>
  }
}

object IndexerActor {
  val timeout = Timeout(5000, TimeUnit.MILLISECONDS)
}
