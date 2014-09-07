package com.vipers

import akka.actor._
import akka.testkit.TestKit
import com.vipers.model._
import org.scalatest.WordSpecLike
import akka.pattern.ask
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
      var request = FetchCharacterRequest(Some("Xanot"), None, None)
      whenReady((fetcherActor ? request).mapTo[Option[Character]]) { character =>
        character.get.id should be("5428035526967126513")
        character.get.name should be("Xanot")
      }

      // non-existing name
      request = FetchCharacterRequest(Some("Xanotetqteqgq"), None, None)
      whenReady((fetcherActor ? request).mapTo[Option[Character]]) { character =>
        character should be(None)
      }
    }
    "return character given character id" in {
      // Valid id
      var request = FetchCharacterRequest(None, Some("5428035526967126513"), None)
      whenReady((fetcherActor ? request).mapTo[Option[Character]]) { character =>
        character.get.id should be("5428035526967126513")
        character.get.name should be("Xanot")
      }

      // non-existing id
      request = FetchCharacterRequest(None, Some("1234"), None)
      whenReady((fetcherActor ? request).mapTo[Option[Character]]) { character =>
        character should be(None)
      }
    }
    "return multiple characters given character id" in {
      // Valid id
      var request = FetchMultipleCharactersByIdRequest(None, "5428035526967126513", "5428232026854955473")
      whenReady((fetcherActor ? request).mapTo[Seq[Character]]) { response =>
        response(0).id should be("5428035526967126513")
        response(0).name should be("Xanot")
        response(1).id should be("5428232026854955473")
        response(1).name should be("ThatTinkSound")
      }

      // non-existing id
      request = FetchMultipleCharactersByIdRequest(None, "123455", "1235413")
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
      var request = FetchOutfitRequest(Some("VIPR"), None, None)
      whenReady((fetcherActor ? request).mapTo[Option[Outfit]]) { outfit =>
        outfit.get.alias should be("VIPR")
        outfit.get.id should be("37523756405021402")
        outfit.get.leaderCharacterId should be("5428013610391601489")
        outfit.get.name should be("TheVipers")
        outfit.get.creationDate should be(1408310892)
      }

      // non-existing alias
      request = FetchOutfitRequest(Some("VIPRRRR"), None, None)
      whenReady((fetcherActor ? request).mapTo[Option[Outfit]]) { outfit =>
        outfit should be(None)
      }
    }
    "return basic outfit info given id" in {
      // Valid id
      var request = FetchOutfitRequest(None, Some("37523756405021402"), None)
      whenReady((fetcherActor ? request).mapTo[Option[Outfit]]) { outfit =>
        outfit.get.alias should be("VIPR")
        outfit.get.id should be("37523756405021402")
        outfit.get.leaderCharacterId should be("5428013610391601489")
        outfit.get.name should be("TheVipers")
        outfit.get.creationDate should be(1408310892)
      }

      // non-existing id
      request = FetchOutfitRequest(None, Some("1111"), None)
      whenReady((fetcherActor ? request).mapTo[Option[Outfit]]) { outfit =>
        outfit should be(None)
      }
    }
    "return outfit info with leader character(with faction) info given alias" in {
      val enrichOutfit = EnrichOutfit(Some(EnrichCharacter(withFaction = true)), None)
      val request = FetchOutfitRequest(Some("VIPR"), None, Some(enrichOutfit))
      whenReady((fetcherActor ? request).mapTo[Option[Outfit]]) { outfit =>
        outfit.get.alias should be("VIPR")
        outfit.get.id should be("37523756405021402")
        outfit.get.leaderCharacterId should be("5428013610391601489")
        outfit.get.name should be("TheVipers")
        outfit.get.creationDate should be(1408310892)
        outfit.get.leaderCharacter.get.name should be("SOLAR15")
        outfit.get.leaderCharacter.get.faction.get.codeTag should be("VS")
      }
    }
    "return outfit info with leader character(with faction) info given id" in {
      val enrichOutfit = EnrichOutfit(Some(EnrichCharacter(withFaction = true)), None)
      val request = FetchOutfitRequest(None, Some("37523756405021402"), Some(enrichOutfit))
      whenReady((fetcherActor ? request).mapTo[Option[Outfit]]) { outfit =>
        outfit.get.alias should be("VIPR")
        outfit.get.id should be("37523756405021402")
        outfit.get.leaderCharacterId should be("5428013610391601489")
        outfit.get.name should be("TheVipers")
        outfit.get.creationDate should be(1408310892)
        outfit.get.leaderCharacter.get.name should be("SOLAR15")
        outfit.get.leaderCharacter.get.faction.get.codeTag should be("VS")
      }
    }
    "return outfit info with leader(with faction) and member characters given alias" in {
      val enrichOutfit = EnrichOutfit(Some(EnrichCharacter(withFaction = true)), Some(EnrichCharacter()))
      val request = FetchOutfitRequest(Some("VIPR"), None, Some(enrichOutfit))
      whenReady((fetcherActor ? request).mapTo[Option[Outfit]]) { outfit =>
        outfit.get.alias should be("VIPR")
        outfit.get.id should be("37523756405021402")
        outfit.get.leaderCharacterId should be("5428013610391601489")
        outfit.get.name should be("TheVipers")
        outfit.get.creationDate should be(1408310892)
        outfit.get.leaderCharacter.get.name should be("SOLAR15")
        outfit.get.leaderCharacter.get.faction.get.codeTag should be("VS")
        outfit.get.members.get.size should be > 0
        outfit.get.members.get(1).battleRank.get.rank.toInt should be > 0
      }
    }
    "return outfit info with leader(with faction) and member characters given id" in {
      val enrichOutfit = EnrichOutfit(Some(EnrichCharacter(withFaction = true)), Some(EnrichCharacter()))
      val request = FetchOutfitRequest(None, Some("37523756405021402"), Some(enrichOutfit))
      whenReady((fetcherActor ? request).mapTo[Option[Outfit]]) { outfit =>
        outfit.get.alias should be("VIPR")
        outfit.get.id should be("37523756405021402")
        outfit.get.leaderCharacterId should be("5428013610391601489")
        outfit.get.name should be("TheVipers")
        outfit.get.creationDate should be(1408310892)
        outfit.get.leaderCharacter.get.name should be("SOLAR15")
        outfit.get.leaderCharacter.get.faction.get.codeTag should be("VS")
        outfit.get.members.get.size should be > 0
        outfit.get.members.get(1).battleRank.get.rank.toInt should be > 0
      }
    }
    "return outfit member characters given id" in {
      // valid id
      var request = FetchOutfitCharactersRequest(None, Some("37523756405021402"), Some(EnrichCharacter()), Page.FirstFive)
      whenReady((fetcherActor ? request).mapTo[Option[FetchOutfitCharactersResponse]]) { response =>
        response.get.total should be > 50
        response.get.characters.size should be(5)
        response.get.characters(1).battleRank.get.rank.toInt should be > 0
      }
      // non-existent id
      request = FetchOutfitCharactersRequest(None, Some("12341"), Some(EnrichCharacter()), Page.FirstFive)
      whenReady((fetcherActor ? request).mapTo[Option[FetchOutfitCharactersResponse]]) { response =>
        response should be(None)
      }
    }
    "return outfit member characters given alias" in {
      // valid id
      var request = FetchOutfitCharactersRequest(Some("VIPR"), None, Some(EnrichCharacter()), Page.FirstTen)
      whenReady((fetcherActor ? request).mapTo[Option[FetchOutfitCharactersResponse]]) { response =>
        response.get.total should be > 50
        response.get.characters.size should be(10)
        response.get.characters(1).battleRank.get.rank.toInt should be > 0
      }

      // non-existent id
      request = FetchOutfitCharactersRequest(Some("VETFEQGEQ"), None, Some(EnrichCharacter()), Page.FirstFive)
      whenReady((fetcherActor ? request).mapTo[Option[FetchOutfitCharactersResponse]]) { response =>
        response should be(None)
      }
    }
  }
}
