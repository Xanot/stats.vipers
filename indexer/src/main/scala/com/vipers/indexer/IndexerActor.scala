package com.vipers.indexer

import java.util.concurrent.TimeUnit
import akka.actor.{PoisonPill, ActorRef, Props, Actor}
import akka.util.Timeout
import com.vipers.Logging
import com.vipers.fetcher.FetcherActor
import com.vipers.fetcher.FetcherActor.{FetchCharacterResponse, FetchCharacterRequest, FetchOutfitResponse, FetchOutfitRequest}
import com.vipers.indexer.IndexerActor._
import com.vipers.indexer.dao.slick.SlickDBComponent
import com.vipers.model.{Outfit, OutfitMembership, Character}
import com.vipers.notifier.NotifierActor
import com.vipers.notifier.NotifierActor.{CharacterIndexed, Stop, Start, OutfitIndexed}
import org.eclipse.jetty.util.ConcurrentHashSet
import scala.concurrent.Future
import akka.pattern.pipe

class IndexerActor extends Actor with Logging {
  import context.dispatcher

  private val db : SlickDBComponent = new SlickDBComponent
  private var fetcherActor : ActorRef = _
  private var notifierActor : ActorRef = _

  private val outfitsBeingIndexed = new ConcurrentHashSet[String]
  private val charactersBeingIndexed = new ConcurrentHashSet[String]

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
    case FetchOutfitResponse(contents, request) =>
      Future {
        contents match {
          case Some((outfit, members)) =>
            db.withTransaction { implicit s =>
              // Index
              db.characterDAO.createAll(members.map(_._1):_*)
              db.outfitDAO.create(outfit)
              db.outfitMembershipDAO.createAll(members.map(_._2):_*)

              outfitsBeingIndexed.remove(request)
              notifierActor ! OutfitIndexed(outfit.aliasLower) // Notify client
              log.debug(s"Outfit ${outfit.alias} has been indexed")
            }
          case None => outfitsBeingIndexed.remove(request)
        }
      }

    case FetchCharacterResponse(contents, request) =>
      Future {
        contents match {
          case Some(character) =>
            db.withTransaction { implicit s =>
              // Index
              db.characterDAO.create(character)
              // TODO: Handle outfit membership

              charactersBeingIndexed.remove(request)
              notifierActor ! CharacterIndexed(character.nameLower)// Notify client
              log.debug(s"Character ${character.name} has been indexed")
            }
          case None => charactersBeingIndexed.remove(request)
        }
      }

    //================================================================================
    // Requests
    //================================================================================
    case GetOutfitRequest(alias, id) =>
      Future {
        db.withSession { implicit s =>
          if(alias.isDefined) {
            db.outfitDAO.findByAliasLower(alias.get).map { outfit => getOutfitResponse(outfit) }.getOrElse {
              if(!outfitsBeingIndexed.contains(alias.get)) {
                log.debug(s"Outfit ${alias.get} is being indexed")
                outfitsBeingIndexed.add(alias.get)
                fetcherActor ! FetchOutfitRequest(alias, None)
              }
              BeingIndexed
            }
          } else if(id.isDefined) {
            db.outfitDAO.find(id.get).map { outfit => getOutfitResponse(outfit) }.getOrElse {
              if(!outfitsBeingIndexed.contains(id.get)) {
                log.debug(s"Outfit ${id.get} is being indexed")
                outfitsBeingIndexed.add(id.get)
                fetcherActor ! FetchOutfitRequest(None, id)
              }
              BeingIndexed
            }
          }
        }
      } pipeTo sender

    case GetMultipleOutfits =>
      Future {
        db.withSession { implicit s =>
          db.outfitDAO.findAll
        }
      } pipeTo sender

    case GetCharacterRequest(nameLower) =>
      Future {
        db.withSession { implicit s =>
          db.characterDAO.findByNameLower(nameLower).map { c =>
            val membership = db.outfitMembershipDAO.find(c.id)
            CharacterWithMembership(c.name, c.nameLower, c.id, c.battleRank, c.battleRankPercent, c.availableCerts, c.earnedCerts, c.certPercent,
              c.spentCerts, c.factionId, c.creationDate, c.lastLoginDate, c.lastSaveDate, c.loginCount, c.minutesPlayed, membership)
          }.getOrElse {
            if(!charactersBeingIndexed.contains(nameLower)) {
              log.debug(s"Character $nameLower is being indexed")
              charactersBeingIndexed.add(nameLower)
              fetcherActor ! FetchCharacterRequest(Some(nameLower), None)
            }
            BeingIndexed
          }
        }
      } pipeTo sender

    case e : AnyRef => log.error(e.toString)
  }

  private def getOutfitResponse(outfit : Outfit)(implicit s : db.Session) = {
    new GetOutfitResponse(outfit.name, outfit.nameLower, outfit.alias, outfit.aliasLower, outfit.leaderCharacterId,
      outfit.memberCount, outfit.factionId, outfit.id, outfit.creationDate, db.outfitDAO.findLeader(outfit.id).get,
      db.outfitMembershipDAO.findAllCharactersByOutfitId(outfit.id).map { c =>
        CharacterWithMembership(c._1.name, c._1.nameLower, c._1.id, c._1.battleRank, c._1.battleRankPercent, c._1.availableCerts, c._1.earnedCerts, c._1.certPercent,
          c._1.spentCerts, c._1.factionId, c._1.creationDate, c._1.lastLoginDate, c._1.lastSaveDate, c._1.loginCount, c._1.minutesPlayed, Some(c._2))
      })
  }
}

object IndexerActor {
  val timeout = Timeout(15000, TimeUnit.MILLISECONDS)

  sealed trait IndexerMessage

  // Received
  case class GetOutfitRequest(aliasLower : Option[String], id : Option[String]) extends IndexerMessage
  case class GetCharacterRequest(nameLower : String) extends IndexerMessage
  case object GetMultipleOutfits extends IndexerMessage

  // Sent
  case object BeingIndexed extends IndexerMessage
  case class GetOutfitResponse(name : String, nameLower : String, alias : String, aliasLower : String,
                          leaderCharacterId : String, memberCount : Int, factionId : Byte, id : String, creationDate : Long,
                          leader : Character, members : List[CharacterWithMembership]) extends IndexerMessage

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
                                     membership : Option[OutfitMembership])
}
