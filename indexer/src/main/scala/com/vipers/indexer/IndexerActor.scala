package com.vipers.indexer

import java.util.concurrent.TimeUnit
import akka.actor.{Props, Actor}
import akka.util.Timeout
import com.vipers.Logging
import com.vipers.fetcher.FetcherActor
import com.vipers.fetcher.FetcherActor.{FetchOutfitResponse, FetchOutfitRequest}
import com.vipers.indexer.IndexerActor._
import com.vipers.indexer.dao.slick.SlickDBComponent
import com.vipers.model.{Outfit, OutfitMembership, Character}
import scala.concurrent.Future
import akka.pattern.pipe

class IndexerActor extends Actor with Logging {
  import context.dispatcher

  private val db : SlickDBComponent = new SlickDBComponent
  private val fetcherActor = context.actorOf(Props(classOf[FetcherActor]))

  def receive = {
    case m : FetchOutfitResponse =>
      Future {
        m.contents.map { case (outfit, members) =>
          // Index
          db.withTransaction { implicit s =>
            db.characterDAO.createAll(members.map(_._1):_*)
            db.outfitDAO.create(outfit)
            db.outfitMembershipDAO.createAll(members.map(_._2):_*)
            // TODO: Notify client here
          }
        }
      }

    case GetOutfitRequest(alias, id) =>
      Future {
        db.withSession { implicit s =>
          if(alias.isDefined) {
            db.outfitDAO.findByAliasLower(alias.get).map { outfit => getOutfitResponse(outfit) }.getOrElse {
              fetcherActor ! FetchOutfitRequest(alias, None)
              BeingIndexed
            }
          } else if(id.isDefined) {
            db.outfitDAO.find(id.get).map { outfit => getOutfitResponse(outfit) }.getOrElse {
              fetcherActor ! FetchOutfitRequest(None, id)
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

    case e : AnyRef => log.error(e.toString)
  }

  private def getOutfitResponse(outfit : Outfit)(implicit s : db.Session) = {
    new GetOutfitResponse(outfit.name, outfit.nameLower, outfit.alias, outfit.aliasLower, outfit.leaderCharacterId,
      outfit.memberCount, outfit.factionCodeTag, outfit.id, outfit.creationDate, db.outfitDAO.findLeader(outfit.id).get,
      db.outfitMembershipDAO.findAllCharactersByOutfitId(outfit.id).map { s =>
        CharacterWithMembership(s._1.name : String, s._1.nameLower : String, s._1.id : String, s._1.battleRank : Short,
          s._1.battleRankPercent : Short, s._1.availableCerts : Int, s._1.earnedCerts : Int, s._1.certPercent : Short,
          s._1.spentCerts : Int, s._1.factionCodeTag : String, s._1.creationDate : Long, s._1.lastLoginDate : Long,
          s._1.lastSaveDate : Long, s._1.loginCount : Int, s._1.minutesPlayed : Int, s._2)
      })
  }
}

object IndexerActor {
  val timeout = Timeout(5000, TimeUnit.MILLISECONDS)

  sealed trait IndexerMessage

  // Received
  case class GetOutfitRequest(aliasLower : Option[String], id : Option[String]) extends IndexerMessage
  case object GetMultipleOutfits extends IndexerMessage

  // Sent
  case object BeingIndexed extends IndexerMessage
  case class GetOutfitResponse(name : String, nameLower : String, alias : String, aliasLower : String,
                          leaderCharacterId : String, memberCount : Int, factionCodeTag : String, id : String, creationDate : Long,
                          leader : Character, members : List[CharacterWithMembership]) extends IndexerMessage

  private[indexer] case class CharacterWithMembership(name : String,
                                                      nameLower : String,
                                                      id : String,
                                                      battleRank : Short,
                                                      battleRankPercent : Short,
                                                      availableCerts : Int,
                                                      earnedCerts : Int,
                                                      certPercent : Short,
                                                      spentCerts : Int,
                                                      factionCodeTag : String,
                                                      creationDate : Long,
                                                      lastLoginDate : Long,
                                                      lastSaveDate : Long,
                                                      loginCount : Int,
                                                      minutesPlayed : Int,
                                                      membership : OutfitMembership)
}
