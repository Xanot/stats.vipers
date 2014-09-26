package com.vipers.indexer

import java.util.concurrent.TimeUnit
import akka.actor.{PoisonPill, ActorRef, Props, Actor}
import akka.util.Timeout
import com.vipers.Logging
import com.vipers.fetcher.FetcherActor
import com.vipers.fetcher.FetcherActor.{FetchCharacterResponse, FetchOutfitResponse}
import com.vipers.indexer.IndexerActor._
import com.vipers.indexer.dao.slick.SlickDBComponent
import com.vipers.model.{OutfitMembership, Character}
import com.vipers.notifier.NotifierActor
import com.vipers.notifier.NotifierActor.{Stop, Start}
import scala.concurrent.Future
import akka.pattern.pipe

class IndexerActor extends Actor with Logging with OutfitIndexerComponent with CharacterIndexerComponent {
  import context.dispatcher

  protected val db : SlickDBComponent = new SlickDBComponent
  protected var fetcherActor : ActorRef = _
  protected var notifierActor : ActorRef = _

  override def preStart() : Unit = {
    fetcherActor = context.actorOf(Props(classOf[FetcherActor]))
    notifierActor = context.actorOf(Props(classOf[NotifierActor]))
    notifierActor ! Start
  }

  override def postStop() : Unit = {
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
        outfitIndexer.index(r)
      }

    case r : FetchCharacterResponse =>
      Future {
        characterIndexer.index(r)
      }

    //================================================================================
    // Requests
    //================================================================================
    case GetOutfitRequest(alias, id) =>
      Future {
        outfitIndexer.retrieve(alias, id).map { case (outfit, leader, members, updateTime) =>
          new GetOutfitResponse(outfit.name, outfit.nameLower, outfit.alias, outfit.aliasLower, outfit.leaderCharacterId,
            outfit.memberCount, outfit.factionId, outfit.id, outfit.creationDate, leader, outfit.lastIndexedOn, updateTime,
            members.map { c =>
              CharacterWithMembership(c._1.name, c._1.nameLower, c._1.id, c._1.battleRank, c._1.battleRankPercent, c._1.availableCerts, c._1.earnedCerts, c._1.certPercent,
                c._1.spentCerts, c._1.factionId, c._1.creationDate, c._1.lastLoginDate, c._1.lastSaveDate, c._1.loginCount, c._1.minutesPlayed, c._1.lastIndexedOn, None, Some(c._2))
            }
          )
        }.getOrElse(BeingIndexed)
      } pipeTo sender

    case GetAllIndexedOutfits =>
      Future {
        db.withSession { implicit s =>
          db.outfitDAO.findAll
        }
      } pipeTo sender

    case GetCharacterRequest(nameLower) =>
      Future {
        characterIndexer.retrieve(nameLower).map { case (c, membership, updateTime) =>
          CharacterWithMembership(c.name, c.nameLower, c.id, c.battleRank, c.battleRankPercent, c.availableCerts, c.earnedCerts, c.certPercent,
            c.spentCerts, c.factionId, c.creationDate, c.lastLoginDate, c.lastSaveDate, c.loginCount, c.minutesPlayed, c.lastIndexedOn, Some(updateTime), membership)
        }.getOrElse(BeingIndexed)
      } pipeTo sender

    case GetAllIndexedCharacters =>
      Future {
        db.withSession { implicit s =>
          db.characterDAO.findAll
        }
      } pipeTo sender

    case e : AnyRef => log.error(e.toString)
  }
}

object IndexerActor {
  val timeout = Timeout(15000, TimeUnit.MILLISECONDS)

  sealed trait IndexerMessage

  // Received
  case class GetOutfitRequest(aliasLower : Option[String], id : Option[String]) extends IndexerMessage
  case class GetCharacterRequest(nameLower : String) extends IndexerMessage
  case object GetAllIndexedOutfits extends IndexerMessage
  case object GetAllIndexedCharacters extends IndexerMessage

  // Sent
  case object BeingIndexed extends IndexerMessage
  case class GetOutfitResponse(name : String, nameLower : String, alias : String, aliasLower : String,
                          leaderCharacterId : String, memberCount : Int, factionId : Byte, id : String, creationDate : Long,
                          leader : Character, lastIndexedOn : Long, updateTime : Long, members : List[CharacterWithMembership]) extends IndexerMessage

  case class CharacterWithMembership(name : String,
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
                                     lastSaveDate : Long,
                                     loginCount : Int,
                                     minutesPlayed : Int,
                                     lastIndexedOn : Long,
                                     updateTime : Option[Long],
                                     membership : Option[OutfitMembership])
}
