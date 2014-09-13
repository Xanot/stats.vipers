package com.vipers.indexer

import akka.actor._
import akka.testkit.TestKit
import com.vipers.Test
import org.scalatest.WordSpecLike
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}

class IndexerActorTest(_system : ActorSystem) extends TestKit(_system) with WordSpecLike with Test with ScalaFutures {
  def this() = this(ActorSystem("IndexerActorTest"))

  private var indexerActor: ActorRef = _
  private implicit val timeout = IndexerActor.timeout
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(Span(5000, Millis))

  override def beforeAll(): Unit = {
    indexerActor = system.actorOf(Props[IndexerActor])
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
}
