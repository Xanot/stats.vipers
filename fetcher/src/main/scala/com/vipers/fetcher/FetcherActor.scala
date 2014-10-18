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

  implicit val jsonFormats = org.json4s.DefaultFormats

  def receive = {
    case FetchCharacterRequest(name, withStats, statsLastIndexedOn) =>
      Future {
        sendRequest(ApiUrlBuilder.getCharacterByName(name, withStats, statsLastIndexedOn)) \ "character_list" match {
          case JArray(parent) if parent.nonEmpty =>
            FetchCharacterResponse(Some((parent(0).toCharacter.get, (parent(0) \ "membership").toOutfitMembership,
              parent(0).toWeaponStats((parent(0) \ "character_id").extract[String]), None)), (name, withStats))
          case _ => FetchCharacterResponse(None, (name, withStats))
        }
      } pipeTo sender
    case m @ FetchOutfitRequest(alias) =>
      Future {
        sendRequest(ApiUrlBuilder.getOutfitByAlias(alias, isSimple = false)) \ "outfit_list" match {
          case JArray(parent) if parent.nonEmpty =>
            FetchOutfitResponse(Some(parent(0).toOutfit.get, {
              val list = mutable.ListBuffer.empty[OutfitMember]
              val JArray(parents) = parent(0) \ "members"

              parents.foreach { parent =>
                list += (((parent \ "character").toCharacter.get, parent.toOutfitMembership.get))
              }
              list
            }), alias)
          case _ => FetchOutfitResponse(None, alias)
        }
      } pipeTo sender
    case FetchAllWeaponsRequest =>
      Future {
        val JArray(weaponList) = sendRequest(ApiUrlBuilder.getAllWeapons) \ "weapon_list"
        val list = mutable.ListBuffer.empty[Weapon]
        weaponList.foreach { json =>
          list += json.toWeapon.get
        }
        FetchAllWeaponsResponse(list)
      } pipeTo sender
  }

  private def sendRequest(r : Uri) : JValue = {
    val request = (IO(Http)(context.system) ? Get(r))(timeout).mapTo[HttpResponse]
    parse(Await.result(request, timeout.duration).entity.asString)
  }
}

object FetcherActor {
  val timeout = Timeout(30000, TimeUnit.MILLISECONDS)

  type OutfitMember = (Character, OutfitMembership)

  //================================================================================
  // Character request/response
  //================================================================================
  case class FetchCharacterRequest(characterName : String, withStats : Boolean, statsLastIndexedOn : Option[Long])
  case class FetchCharacterResponse(contents : Option[(Character, Option[OutfitMembership], Option[List[WeaponStat]], Option[List[ProfileStat]])], request : (String, Boolean))

  //================================================================================
  // Outfit request/response
  //================================================================================
  case class FetchOutfitRequest(alias : String)
  case class FetchOutfitResponse(contents : Option[(Outfit, Seq[OutfitMember])], request : String)

  //================================================================================
  // Weapon request/response
  //================================================================================
  case object FetchAllWeaponsRequest
  case class FetchAllWeaponsResponse(weapons : Seq[Weapon])
}