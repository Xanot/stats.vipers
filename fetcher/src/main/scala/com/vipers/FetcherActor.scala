package com.vipers

import java.util.concurrent.TimeUnit
import akka.actor.Actor
import akka.io.IO
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.vipers.model._
import com.vipers.util.ApiUrlBuilder
import org.json4s.JsonAST._
import spray.can.Http
import spray.httpx.RequestBuilding._
import spray.http._
import scala.concurrent.{Await, Future}
import org.json4s.native.JsonMethods._
import com.vipers.util.Wrapper.ApiDeserializer
import scala.collection.mutable

class FetcherActor extends Actor {
  import scala.concurrent.ExecutionContext.Implicits.global
  import FetcherActor._

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
        val list = mutable.ListBuffer.empty[model.Character]
        parent.foreach { charJson =>
          list += charJson.toCharacter().get
        }
        list
      } pipeTo sender
    case FetchOutfitRequest(alias, id, enrich) =>
      Future {
        val json = {
          if(alias.isDefined) {
            sendRequest(ApiUrlBuilder.getOutfitByAlias(alias.get, enrich))
          } else {
            sendRequest(ApiUrlBuilder.getOutfitById(id.get, enrich))
          }
        }
        json \ "outfit_list" match {
          case JArray(parent) if parent.nonEmpty =>  Some(parent(0).toOutfit.get)
          case _ => None
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
            val list = mutable.ListBuffer.empty[model.Character]
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