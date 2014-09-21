package com.vipers.fetcher

import java.util.concurrent.TimeUnit
import akka.actor.Actor
import akka.io.IO
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.vipers.fetcher.util.ApiUrlBuilder
import com.vipers.fetcher.util.Wrapper.ApiDeserializer
import com.vipers.model._
import org.json4s.JsonAST._
import org.json4s.native.JsonMethods._
import spray.can.Http
import spray.http._
import spray.httpx.RequestBuilding._
import scala.collection.mutable
import scala.concurrent.{Await, Future}

class FetcherActor extends Actor {
  import context.dispatcher
  import FetcherActor._

  def receive = {
    case FetchCharacterRequest(name, id) =>
      Future {
        val json = {
          if(id.isDefined) {
            sendRequest(ApiUrlBuilder.getCharactersById(id.get))
          } else {
            sendRequest(ApiUrlBuilder.getCharacterByName(name.get))
          }
        }
        json \ "character_list" match {
          case JArray(parent) if parent.nonEmpty => FetchCharacterResponse(Some(parent(0).toCharacter.get), name.getOrElse(id.get))
          case _ => FetchCharacterResponse(None, name.getOrElse(id.get))
        }
      } pipeTo sender
    case FetchMultipleCharactersByIdRequest(ids @_*) =>
      Future {
        val json = sendRequest(ApiUrlBuilder.getCharactersById(ids:_*))
        val JArray(parent) = json \ "character_list"
        val list = mutable.ListBuffer.empty[Character]
        parent.foreach { charJson =>
          list += charJson.toCharacter.get
        }
        list
      } pipeTo sender
    case m @ FetchSimpleMultipleOutfitsRequest(sort, page) =>
      Future {
        val JArray(outfits) = sendRequest(ApiUrlBuilder.getSimpleOutfits(sort, page)) \ "outfit_list"
        val list = mutable.ListBuffer.empty[Outfit]
        outfits.foreach { outfitJson =>
          list += outfitJson.toOutfit.get
        }
        list.toList
      } pipeTo sender
    case m @ FetchSimpleOutfitRequest(alias, id) =>
      Future {
        val json = {
          if (alias.isDefined) {
            sendRequest(ApiUrlBuilder.getOutfitByAlias(alias.get, isSimple = true))
          } else {
            sendRequest(ApiUrlBuilder.getOutfitById(id.get, isSimple = true))
          }
        }
        json \ "outfit_list" match {
          case JArray(parent) if parent.nonEmpty => Some(parent(0).toOutfit.get)
          case _ => None
        }
      } pipeTo sender
    case m @ FetchOutfitRequest(alias, id) =>
      Future {
        val json = {
          if (alias.isDefined) {
            sendRequest(ApiUrlBuilder.getOutfitByAlias(alias.get, isSimple = false))
          } else {
            sendRequest(ApiUrlBuilder.getOutfitById(id.get, isSimple = false))
          }
        }
        json \ "outfit_list" match {
          case JArray(parent) if parent.nonEmpty =>
            FetchOutfitResponse(Some(parent(0).toOutfit.get, {
              val list = mutable.ListBuffer.empty[OutfitMember]
              val JArray(parents) = parent(0) \ "members"

              parents.foreach { parent =>
                list += (((parent \ "character").toCharacter.get, parent.toOutfitMembership.get))
              }
              list
            }), alias.getOrElse(id.get))
          case _ => FetchOutfitResponse(None, alias.getOrElse(id.get))
        }
      } pipeTo sender
    case FetchOutfitCharactersRequest(alias, id, page) =>
      Future {
        val json = {
          if(alias.isDefined) {
            sendRequest(ApiUrlBuilder.getOutfitCharactersByAlias(alias.get, page))
          } else {
            sendRequest(ApiUrlBuilder.getOutfitCharactersById(id.get, page))
          }
        }
        json \ "outfit_member_extended_list" match {
          case JArray(parents) if parents.nonEmpty =>
            val JString(total) = parents(0) \ "member_count"
            val list = mutable.ListBuffer.empty[OutfitMember]
            parents.foreach { parent =>
              list += (((parent \ "character").toCharacter.get, parent.toOutfitMembership.get))
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
  val timeout = Timeout(15000, TimeUnit.MILLISECONDS)
  import com.vipers.model.Sort.Sort

  type OutfitMember = (Character, OutfitMembership)

  //================================================================================
  // Character request/response
  //================================================================================
  case class FetchCharacterRequest(characterName : Option[String], characterId : Option[String])
  case class FetchCharacterResponse(character : Option[Character], request : String)

  case class FetchMultipleCharactersByIdRequest(characterIds : String*)

  //================================================================================
  // Outfit request/response
  //================================================================================
  case class FetchSimpleOutfitRequest(alias : Option[String], id : Option[String])

  case class FetchSimpleMultipleOutfitsRequest(sort : Sort, page : Page)

  case class FetchOutfitRequest(alias : Option[String], id : Option[String])
  case class FetchOutfitResponse(contents : Option[(Outfit, Seq[OutfitMember])], request : String)

  case class FetchOutfitCharactersRequest(alias : Option[String], id : Option[String], page : Page)
  case class FetchOutfitCharactersResponse(total : Int, characters : Seq[OutfitMember])
}