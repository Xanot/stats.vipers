package com.vipers.indexer

import java.util.concurrent.TimeUnit
import akka.actor.{PoisonPill, ActorRef, Props, Actor}
import akka.util.Timeout
import com.vipers.Logging
import com.vipers.fetcher.FetcherActor
import com.vipers.fetcher.FetcherActor._
import com.vipers.indexer.IndexerActor._
import com.vipers.indexer.dao.slick.SlickDBComponent
import com.vipers.model.{Weapon, WeaponStat, OutfitMembership, Character}
import com.vipers.notifier.NotifierActor
import com.vipers.notifier.NotifierActor.{OutfitIndexed, CharacterIndexed, Stop, Start}
import scala.concurrent.Future
import akka.pattern.pipe
import scala.concurrent.duration.FiniteDuration

class IndexerActor extends Actor with Logging with SlickDBComponent with OutfitIndexerComponent with CharacterIndexerComponent with WeaponIndexerComponent {
  import context.dispatcher

  protected var fetcherActor : ActorRef = _
  protected var notifierActor : ActorRef = _

  val weaponSchedule = context.system.scheduler.schedule(FiniteDuration(0, TimeUnit.MILLISECONDS),
    FiniteDuration(Configuration.weaponsStaleAfter + 10000, TimeUnit.MILLISECONDS)) {
    weaponIndexer.retrieve match {
      case (needsIndexing, _) =>
        if(needsIndexing) {
          fetcherActor ! FetchAllWeaponsRequest
        }
    }
  }

  override def preStart() : Unit = {
    fetcherActor = context.actorOf(Props(classOf[FetcherActor]))
    notifierActor = context.actorOf(Props(classOf[NotifierActor]))
    notifierActor ! Start
  }

  override def postStop() : Unit = {
    weaponSchedule.cancel()
    notifierActor ! Stop
    fetcherActor ! PoisonPill
    notifierActor ! PoisonPill
  }

  def receive = {
    //================================================================================
    // Fetcher response
    //================================================================================
    case r : FetchOutfitResponse =>
      Future {
        outfitIndexer.index(r).map { outfit =>
          notifierActor ! OutfitIndexed(outfit.aliasLower) // Notify client
        }
      }

    case r : FetchCharacterResponse =>
      Future {
        characterIndexer.index(r).map { character =>
          notifierActor ! CharacterIndexed(character.nameLower) // Notify client
        }
      }

    case r : FetchAllWeaponsResponse =>
      Future {
        weaponIndexer.index(r)
      }

    //================================================================================
    // Requests
    //================================================================================
    case GetOutfitRequest(alias, id) =>
      Future {
        outfitIndexer.retrieve(alias, id) match {
          case (needsIndexing, retrievedInfo) =>
            if(needsIndexing) {
              fetcherActor ! FetchOutfitRequest(alias, id)
            }

            retrievedInfo.map { case (outfit, leader, members, updateTime) =>
              GetOutfitResponse(outfit.name, outfit.alias, outfit.aliasLower,
                outfit.memberCount, outfit.factionId, outfit.id, outfit.creationDate, leader, outfit.lastIndexedOn, updateTime,
                members.map { c =>
                  GetOutfitResponseCharacter(c._1.name, c._1.nameLower, c._1.id, c._1.battleRank, c._1.battleRankPercent, c._1.earnedCerts,
                    c._1.creationDate, c._1.lastLoginDate, c._1.minutesPlayed, c._2)
                }
              )
            }.getOrElse(BeingIndexed)
        }
      } pipeTo sender

    case GetAllIndexedOutfits =>
      Future {
        withSession { implicit s =>
          outfitDAO.findAll
        }
      } pipeTo sender

    case GetCharacterRequest(nameLower) =>
      Future {
        characterIndexer.retrieve(nameLower) match {
          case (needsIndexing, retrievedInfo) =>
            if(needsIndexing) {
              fetcherActor ! FetchCharacterRequest(Some(nameLower), None, withStats = true)
            }

            retrievedInfo.map { case (c, membership, updateTime, mostRecentWeaponStats) =>
              GetCharacterResponse(c.name, c.nameLower, c.id, c.battleRank, c.battleRankPercent, c.availableCerts, c.earnedCerts, c.certPercent,
                c.spentCerts, c.factionId, c.creationDate, c.lastLoginDate, c.minutesPlayed, c.lastIndexedOn, updateTime, membership, mostRecentWeaponStats)
            }.getOrElse(BeingIndexed)
        }
      } pipeTo sender

    case GetAllIndexedCharacters =>
      Future {
        withSession { implicit s =>
          characterDAO.findAll
        }
      } pipeTo sender

    case GetCharactersWeaponStatHistory(characterId, itemId) =>
      Future {
        withSession { implicit s =>
          weaponStatDAO.getCharactersWeaponStatHistory(characterId, itemId)
        }
      } pipeTo sender

    case e : AnyRef => log.error(e.toString)
  }
}

object IndexerActor {
  val timeout = Timeout(30000, TimeUnit.MILLISECONDS)

  sealed trait IndexerMessage

  // Received
  case class GetOutfitRequest(aliasLower : Option[String], id : Option[String]) extends IndexerMessage
  case class GetCharacterRequest(nameLower : String) extends IndexerMessage
  case object GetAllIndexedOutfits extends IndexerMessage
  case object GetAllIndexedCharacters extends IndexerMessage
  case class GetCharactersWeaponStatHistory(characterId : String, itemId : String) extends IndexerMessage

  // Sent
  case object BeingIndexed extends IndexerMessage

  case class GetOutfitResponse(name : String,
                               alias : String,
                               aliasLower : String,
                               memberCount : Int,
                               factionId : Byte,
                               id : String,
                               creationDate : Long,
                               leader : Character,
                               lastIndexedOn : Long,
                               updateTime : Long,
                               members : List[GetOutfitResponseCharacter]) extends IndexerMessage

  case class GetOutfitResponseCharacter(name : String,
                                        nameLower : String,
                                        id : String,
                                        battleRank : Short,
                                        battleRankPercent : Short,
                                        earnedCerts : Int,
                                        creationDate : Long,
                                        lastLoginDate : Long,
                                        minutesPlayed : Int,
                                        membership : OutfitMembership)

  case class GetCharacterResponse(name : String,
                                  nameLower : String,
                                  id : String,
                                  battleRank : Short,
                                  battleRankPercent : Short,
                                  availableCerts : Int,
                                  earnedCerts : Int,
                                  certPercent : Short,
                                  spentCerts : Int,
                                  factionId : Byte,
                                  creationDate : Long,
                                  lastLoginDate : Long,
                                  minutesPlayed : Int,
                                  lastIndexedOn : Long,
                                  updateTime : Long,
                                  membership : Option[GetCharacterResponseOutfitMembership],
                                  weaponStats : List[(WeaponStat, Weapon)])

  case class GetCharacterResponseOutfitMembership(rank : String,
                                                  rankOrdinal : Byte,
                                                  memberSinceDate : Long,
                                                  outfitAlias : String,
                                                  outfitName : String)
}
