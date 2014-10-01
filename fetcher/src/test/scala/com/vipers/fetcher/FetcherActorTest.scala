package com.vipers.fetcher

import akka.actor._
import akka.pattern.ask
import akka.testkit.TestKit
import com.vipers.Test
import com.vipers.fetcher.FetcherActor._
import com.vipers.model._
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

  //================================================================================
  // Character
  //================================================================================
  "Fetcher actor" should {
    "return character given character name" in {
      // Valid name
      var request = FetchCharacterRequest(Some("Xanot"), None)
      whenReady((fetcherActor ? request).mapTo[FetchCharacterResponse]) { response =>
        response.character.get.id should be("5428035526967126513")
        response.character.get.name should be("Xanot")
        response.character.get.factionId should be(1)
        response.request should be("Xanot")
      }

      // non-existing name
      request = FetchCharacterRequest(Some("Xanotetqteqgq"), None)
      whenReady((fetcherActor ? request).mapTo[FetchCharacterResponse]) { response =>
        response.character should be(None)
        response.request should be("Xanotetqteqgq")
      }
    }
    "return character given character id" in {
      // Valid id
      var request = FetchCharacterRequest(None, Some("5428035526967126513"))
      whenReady((fetcherActor ? request).mapTo[FetchCharacterResponse]) { response =>
        response.character.get.id should be("5428035526967126513")
        response.character.get.name should be("Xanot")
        response.character.get.factionId should be(1)
        response.request should be("5428035526967126513")
      }

      // non-existing id
      request = FetchCharacterRequest(None, Some("1234"))
      whenReady((fetcherActor ? request).mapTo[FetchCharacterResponse]) { response =>
        response.character should be(None)
        response.request should be("1234")
      }
    }
    "return multiple characters given character id" in {
      // Valid id
      var request = FetchMultipleCharactersByIdRequest("5428035526967126513", "5428232026854955473")
      whenReady((fetcherActor ? request).mapTo[Seq[Character]]) { response =>
        response(0).id should be("5428035526967126513")
        response(0).name should be("Xanot")
        response(1).id should be("5428232026854955473")
        response(1).name should be("ThatTinkSound")
      }

      // non-existing id
      request = FetchMultipleCharactersByIdRequest("123455", "1235413")
      whenReady((fetcherActor ? request).mapTo[Seq[Character]]) { response =>
        response.size should be(0)
      }
    }
  }

  //================================================================================
  // Outfit
  //================================================================================
  "Fetcher actor" should {
    "return basic outfit info given alias" in {
      // Valid alias
      var request = FetchSimpleOutfitRequest(Some("VIPR"), None)
      whenReady((fetcherActor ? request).mapTo[Option[Outfit]]) { outfit =>
        outfit.get.alias should be("VIPR")
        outfit.get.id should be("37523756405021402")
        outfit.get.leaderCharacterId should be("5428013610391601489")
        outfit.get.name should be("TheVipers")
        outfit.get.creationDate should be(1408310892)
      }

      // non-existing alias
      request = FetchSimpleOutfitRequest(Some("VIPRRRR"), None)
      whenReady((fetcherActor ? request).mapTo[Option[Outfit]]) { outfit =>
        outfit should be(None)
      }
    }
    "return basic outfit info given id" in {
      // Valid id
      var request = FetchSimpleOutfitRequest(None, Some("37523756405021402"))
      whenReady((fetcherActor ? request).mapTo[Option[Outfit]]) { outfit =>
        outfit.get.alias should be("VIPR")
        outfit.get.id should be("37523756405021402")
        outfit.get.leaderCharacterId should be("5428013610391601489")
        outfit.get.name should be("TheVipers")
        outfit.get.creationDate should be(1408310892)
      }

      // non-existing id
      request = FetchSimpleOutfitRequest(None, Some("1111"))
      whenReady((fetcherActor ? request).mapTo[Option[Outfit]]) { outfit =>
        outfit should be(None)
      }
    }
    "return outfit info with leader(with faction) and member characters given alias" in {
      val request = FetchOutfitRequest(Some("VIPR"), None)
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
    }
    "return outfit info with leader(with faction) and member characters given id" in {
      val request = FetchOutfitRequest(None, Some("37523756405021402"))
      whenReady((fetcherActor ? request).mapTo[FetchOutfitResponse]) { response =>
        val contents = response.contents.get
        contents._1.alias should be("VIPR")
        contents._1.id should be("37523756405021402")
        contents._1.leaderCharacterId should be("5428013610391601489")
        contents._1.name should be("TheVipers")
        contents._1.creationDate should be(1408310892)
        contents._2.size should be > 0
        contents._2(1)._1.battleRank.toInt should be > 0
        response.request should be("37523756405021402")
      }
    }
    "return outfit member characters given id" in {
      // valid id
      var request = FetchOutfitCharactersRequest(None, Some("37523756405021402"), Page.FirstFive)
      whenReady((fetcherActor ? request).mapTo[Option[FetchOutfitCharactersResponse]]) { response =>
        response.get.total should be > 50
        response.get.characters.size should be(5)
        response.get.characters(1)._1.battleRank.toInt should be > 0
      }
      // non-existent id
      request = FetchOutfitCharactersRequest(None, Some("12341"), Page.FirstFive)
      whenReady((fetcherActor ? request).mapTo[Option[FetchOutfitCharactersResponse]]) { response =>
        response should be(None)
      }
    }
    "return outfit member characters given alias" in {
      // valid alias
      var request = FetchOutfitCharactersRequest(Some("VIPR"), None, Page.FirstTen)
      whenReady((fetcherActor ? request).mapTo[Option[FetchOutfitCharactersResponse]]) { response =>
        response.get.total should be > 50
        response.get.characters.size should be(10)
        response.get.characters(1)._1.battleRank.toInt should be > 0
      }

      // non-existent alias
      request = FetchOutfitCharactersRequest(Some("VETFEQGEQ"), None, Page.FirstFive)
      whenReady((fetcherActor ? request).mapTo[Option[FetchOutfitCharactersResponse]]) { response =>
        response should be(None)
      }
    }
    "return multiple outfits given sort order and page" in {
      val request = FetchSimpleMultipleOutfitsRequest((Sort.CREATION_DATE, Sort.ASC), Page.FirstFive)
      whenReady((fetcherActor ? request).mapTo[List[Outfit]]) { response =>
        response.length should be(5)
        response(0).aliasLower should be("te")
        response(4).aliasLower should be("conz")
      }
    }
  }

  //================================================================================
  // Weapon
  //================================================================================
  "Fetcher actor" should {
    "return all weapons" in {
      whenReady((fetcherActor ? FetchAllWeaponsRequest).mapTo[FetchAllWeaponsResponse]) { response =>
        response.weapons.length should be > 100
        response.weapons.filter(_.id == "1")(0).name should be("NS AutoBlade")
      }
    }
  }
}
