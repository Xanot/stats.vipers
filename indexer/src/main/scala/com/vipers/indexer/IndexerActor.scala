package com.vipers.indexer

import java.util.concurrent.TimeUnit
import akka.actor.{Props, Actor}
import akka.util.Timeout
import com.vipers.Logging
import com.vipers.fetcher.FetcherActor
import com.vipers.fetcher.model.{FetchOutfitRequest, Outfit => FetcherOutfit}
import com.vipers.indexer.IndexerActor.{GetMultipleOutfits, GetOutfit}
import com.vipers.indexer.dao.Model.Outfit
import com.vipers.indexer.dao.slick.SlickDBComponent
import scala.concurrent.Future
import akka.pattern.pipe

class IndexerActor extends Actor with Logging {
  import context.dispatcher

  private val db : SlickDBComponent = new SlickDBComponent
  private val fetcherActor = context.actorOf(Props(classOf[FetcherActor]))

  override def preStart(): Unit = {
    // TODO: Schedule here
  }

  def receive = {
    case m : Some[FetcherOutfit] =>
      // TODO: Index
      val outfit = m.get
      db.withTransaction { implicit s =>
        db.outfitDAO.create(Outfit(outfit.name, outfit.name.toLowerCase, outfit.alias, outfit.aliasLower, outfit.leaderCharacterId, outfit.memberCount, outfit.id, outfit.creationDate))

        // TODO: Notify client here
        log.info(db.outfitDAO.find(outfit.id).get.toString)
      }

    case GetOutfit(alias, id) =>
      Future {
        db.withSession { implicit s =>
          if(alias.isDefined) {
            db.outfitDAO.findByAliasLower(alias.get).orElse {
              // TODO: Schedule indexing if not found
              fetcherActor ! FetchOutfitRequest(alias, None, None)
              None
            }
          } else if(id.isDefined) {
            db.outfitDAO.findByAliasLower(id.get).orElse {
              // TODO: Schedule indexing if not found
              fetcherActor ! FetchOutfitRequest(id, None, None)
              None
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
  }
}

object IndexerActor {
  sealed trait IndexerMessage
  case class GetOutfit(aliasLower : Option[String], id : Option[String]) extends IndexerMessage
  case object GetMultipleOutfits extends IndexerMessage

  val timeout = Timeout(5000, TimeUnit.MILLISECONDS)
}
