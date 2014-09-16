package com.vipers.indexer

import java.util.concurrent.TimeUnit
import akka.actor.{Props, Actor}
import akka.util.Timeout
import com.vipers.Logging
import com.vipers.fetcher.FetcherActor
import com.vipers.fetcher.FetcherActor.{FetchOutfitResponse, FetchOutfitRequest}
import com.vipers.model._
import com.vipers.indexer.IndexerActor.{GetMultipleOutfits, GetOutfit}
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
    case m : FetchOutfitResponse =>
      Future {
        m.contents.map { case (outfit, leader, members) =>
          // TODO: Index
          db.withTransaction { implicit s =>
            db.outfitDAO.create(outfit)

            // TODO: Notify client here
          }
        }
      }

    case GetOutfit(alias, id) =>
      Future {
        db.withSession { implicit s =>
          if(alias.isDefined) {
            db.outfitDAO.findByAliasLower(alias.get).orElse {
              // TODO: Schedule indexing if not found
              fetcherActor ! FetchOutfitRequest(alias, None)
              None
            }
          } else if(id.isDefined) {
            db.outfitDAO.findByAliasLower(id.get).orElse {
              // TODO: Schedule indexing if not found
              fetcherActor ! FetchOutfitRequest(id, None)
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

    case e : AnyRef => log.error(e.toString)
  }
}

object IndexerActor {
  sealed trait IndexerMessage
  case class GetOutfit(aliasLower : Option[String], id : Option[String]) extends IndexerMessage
  case object GetMultipleOutfits extends IndexerMessage

  val timeout = Timeout(5000, TimeUnit.MILLISECONDS)
}
