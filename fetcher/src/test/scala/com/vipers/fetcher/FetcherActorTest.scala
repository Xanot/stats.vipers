package com.vipers.fetcher

import akka.actor._
import akka.pattern.ask
import akka.testkit.TestKit
import com.vipers.Test
import com.vipers.fetcher.FetcherActor._
import org.scalatest.WordSpecLike
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}

class FetcherActorTest(_system : ActorSystem) extends TestKit(_system) with WordSpecLike with Test with ScalaFutures {
  def this() = this(ActorSystem("FetcherActorTest"))

  private var fetcherActor : ActorRef = _
  private implicit val timeout = FetcherActor.timeout
  override implicit val patienceConfig : PatienceConfig = PatienceConfig(Span(5000, Millis))

  override def beforeAll(): Unit = {
    fetcherActor = system.actorOf(Props[FetcherActor])
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Fetcher actor" should {
    //================================================================================
    // Character
    //================================================================================
    "return character given character name" in {
      // Valid name without stats
      var request = FetchCharacterRequest("Xanot", false, None)
      whenReady((fetcherActor ? request).mapTo[FetchCharacterResponse]) { response =>
        response.contents.get._1.id should be("5428035526967126513")
        response.contents.get._1.name should be("Xanot")
        response.contents.get._1.factionId should be(1)
        response.contents.get._2 should not be(None)
        response.contents.get._3 should be(None)
        response.contents.get._4 should be(None)
        response.request._1 should be("Xanot")
        response.request._2 should be(false)
      }

      // Valid name with stats
      request = FetchCharacterRequest("Xanot", true, None)
      whenReady((fetcherActor ? request).mapTo[FetchCharacterResponse]) { response =>
        response.contents.get._1.id should be("5428035526967126513")
        response.contents.get._1.name should be("Xanot")
        response.contents.get._1.factionId should be(1)
        response.contents.get._2 should not be(None)
        response.contents.get._3 should not be(None)
        response.contents.get._4 should not be(None)
        response.request._1 should be("Xanot")
        response.request._2 should be(true)
      }

      // With stats (with stats last saved date)
      request = FetchCharacterRequest("Xanot", true, Some(1411669674L))
      whenReady((fetcherActor ? request).mapTo[FetchCharacterResponse]) { response =>
        response.contents.get._1.id should be("5428035526967126513")
        response.contents.get._1.name should be("Xanot")
        response.contents.get._1.factionId should be(1)
        response.contents.get._2 should not be(None)
        response.contents.get._3 should not be(None)
        response.contents.get._4 should not be(None)
        response.request._1 should be("Xanot")
        response.request._2 should be(true)
      }

      // non-existing name
      request = FetchCharacterRequest("Xanotetqteqgq", false, None)
      whenReady((fetcherActor ? request).mapTo[FetchCharacterResponse]) { response =>
        response.contents should be(None)
        response.request._1 should be("Xanotetqteqgq")
        response.request._2 should be(false)
      }
    }

    //================================================================================
    // Outfit
    //================================================================================
    "return outfit info with leader(with faction) and member characters given alias" in {
      var request = FetchOutfitRequest("VIPR")
      whenReady((fetcherActor ? request).mapTo[FetchOutfitResponse]) { response =>
        val contents = response.contents.get
        contents._1.alias should be("VIPR")
        contents._1.id should be("37523756405021402")
        contents._1.leaderCharacterId should be("5428013610391601489")
        contents._1.name should be("TheVipers")
        contents._1.creationDate should be(1408310892)
        contents._2.size should be > 0
        contents._2(1)._1.battleRank.toInt should be > 0
        response.request should be("VIPR")
      }

      // non-existing name
      request = FetchOutfitRequest("VIPRR")
      whenReady((fetcherActor ? request).mapTo[FetchOutfitResponse]) { response =>
        response.contents should be(None)
        response.request should be("VIPRR")
      }
    }

    //================================================================================
    // Weapon
    //================================================================================
    "return all weapons" in {
      whenReady((fetcherActor ? FetchAllWeaponsRequest).mapTo[FetchAllWeaponsResponse]) { response =>
        response.weapons.length should be > 100
      }
    }
  }
}
