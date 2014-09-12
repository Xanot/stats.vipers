package com.vipers.fetcher

import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.io.IO
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.vipers.fetcher.model._
import com.vipers.fetcher.util.ApiUrlBuilder
import com.vipers.fetcher.util.Wrapper.ApiDeserializer
import org.json4s.JsonAST._
import org.json4s.native.JsonMethods._
import spray.can.Http
import spray.http._
import spray.httpx.RequestBuilding._
import scala.collection.mutable
import scala.concurrent.{Await, Future}
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters._

class FetcherActor extends Actor {
  import context.dispatcher
  import FetcherActor._

  private val fetchOutfitRequestCache = new ConcurrentHashMap[FetchOutfitRequest, Option[Outfit]]().asScala
  private val fetchMultipleOutfitsRequestCache = new ConcurrentHashMap[FetchMultipleOutfitsRequest, List[Outfit]]().asScala

  def receive = {
    case FetchCharacterRequest(name, id, enrich) =>
      Future {
        val json = {
          if(id.isDefined) {
            sendRequest(ApiUrlBuilder.getCharactersById(enrich, id.get))
          } else {
            sendRequest(ApiUrlBuilder.getCharacterByName(name.get, enrich))
          }
        }
        json \ "character_list" match {
          case JArray(parent) if parent.nonEmpty =>  Some(parent(0).toCharacter().get)
          case _ => None
        }
      } pipeTo sender
    case FetchMultipleCharactersByIdRequest(enrich, ids @_*) =>
      Future {
        val json = sendRequest(ApiUrlBuilder.getCharactersById(enrich, ids:_*))
        val JArray(parent) = json \ "character_list"
        val list = mutable.ListBuffer.empty[Character]
        parent.foreach { charJson =>
          list += charJson.toCharacter().get
        }
        list
      } pipeTo sender
    case m @ FetchOutfitRequest(alias, id, enrich) =>
      Future {
        if(fetchOutfitRequestCache.contains(m)) {
          fetchOutfitRequestCache(m)
        } else {
          val json = {
            if (alias.isDefined) {
              sendRequest(ApiUrlBuilder.getOutfitByAlias(alias.get, enrich))
            } else {
              sendRequest(ApiUrlBuilder.getOutfitById(id.get, enrich))
            }
          }
          try {
            json \ "outfit_list" match {
              case JArray(parent) if parent.nonEmpty =>
                val o = Some(parent(0).toOutfit.get)
                fetchOutfitRequestCache += m -> o
                o
              case _ =>
                fetchOutfitRequestCache += m -> None
                None
            }
          } catch {
            case e : Exception => e.printStackTrace()
          }

        }
      } pipeTo sender

    case m @ FetchMultipleOutfitsRequest(sort, page) =>
      Future {
        if(fetchMultipleOutfitsRequestCache.contains(m)) {
          fetchMultipleOutfitsRequestCache(m)
        } else {
          val JArray(outfits) = sendRequest(ApiUrlBuilder.getOutfits(sort, page)) \ "outfit_list"
          val list = mutable.ListBuffer.empty[Outfit]
          outfits.foreach { outfitJson =>
            list += outfitJson.toOutfit.get
          }
          fetchMultipleOutfitsRequestCache += m -> list.toList
          list.toList
        }
      } pipeTo sender
    case FetchOutfitCharactersRequest(alias, id, enrich, page) =>
      Future {
        val json = {
          if(alias.isDefined) {
            sendRequest(ApiUrlBuilder.getOutfitCharactersByAlias(alias.get, page, enrich))
          } else {
            sendRequest(ApiUrlBuilder.getOutfitCharactersById(id.get, page, enrich))
          }
        }
        json \ "outfit_member_extended_list" match {
          case JArray(parents) if parents.nonEmpty =>
            val JString(total) = parents(0) \ "member_count"
            val list = mutable.ListBuffer.empty[Character]
            parents.foreach { parent =>
              list += (parent \ "character").toCharacter(parent.toOutfitMember).get
            }
            Some(FetchOutfitCharactersResponse(total.toInt, list))
          case _ => None
        }
      } pipeTo sender
  }

  private def sendRequest(r : Uri) : JValue = {
    val request = (IO(Http)(context.system) ? Get(r))(timeout).mapTo[HttpResponse]
    parse(Await.result(request, timeout.duration).entity.asString)
  }
}

object FetcherActor {
  val timeout = Timeout(5000, TimeUnit.MILLISECONDS)
}