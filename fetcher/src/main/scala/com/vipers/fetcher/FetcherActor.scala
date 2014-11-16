package com.vipers.fetcher

import java.util.concurrent.TimeUnit
import akka.actor.Actor
import akka.io.IO
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.vipers.fetcher.util.ApiUrlBuilder
import com.vipers.fetcher.util.Wrapper.ApiDeserializer
import com.vipers.model.DatabaseModels._
import com.vipers.model.Page
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
    case FetchCharacterRequest(name, withStats, statsLastSavedOn) =>
      Future {
        try {
          sendRequest(ApiUrlBuilder.getCharacterByName(name, withStats, statsLastSavedOn)) \ "character_list" match {
            case JArray(parent) if parent.nonEmpty =>
              val JString(characterId) = parent(0) \ "character_id"
              FetchCharacterResponse(Some((parent(0).toCharacter.get, (parent(0) \ "membership").toOutfitMembership,
                parent(0).toWeaponStats(characterId), parent(0).toProfileStats(characterId), statsLastSavedOn.isEmpty)), (name, withStats))
            case _ => FetchCharacterResponse(None, (name, withStats))
          }
        } catch {
          case _ : Exception => FetchCharacterResponse(None, (name, withStats))
        }
      } pipeTo sender
    case m @ FetchOutfitRequest(alias) =>
      Future {
        try {
          sendRequest(ApiUrlBuilder.getOutfitByAlias(alias)) \ "outfit_list" match {
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
        } catch {
          case _ : Exception => FetchOutfitResponse(None, alias)
        }
      } pipeTo sender
    case FetchAllWeaponsRequest =>
      Future {
        val JArray(weaponList) = {
          val aggregation = for {
            res1 <- sendAsyncRequest(ApiUrlBuilder.getAllWeapons(Page(Some(5000), Some(0))))
            res2 <- sendAsyncRequest(ApiUrlBuilder.getAllWeapons(Page(Some(5000), Some(5000))))
            res3 <- sendAsyncRequest(ApiUrlBuilder.getAllWeapons(Page(Some(5000), Some(10000))))
            res4 <- sendAsyncRequest(ApiUrlBuilder.getAllWeapons(Page(Some(5000), Some(15000))))
          } yield (res1, res2, res3, res4)

          val result = Await.result(aggregation, timeout.duration)
          parse(result._1.entity.asString) \ "item_list" ++
            parse(result._2.entity.asString) \ "item_list" ++
              parse(result._3.entity.asString) \ "item_list" ++
                parse(result._4.entity.asString) \ "item_list"
        }

        val weapons = mutable.ListBuffer.empty[Weapon]
        val weaponProps = mutable.ListBuffer.empty[WeaponProps]
        weaponList.foreach { json =>
          json.toWeapon.map { wep =>
            weapons += wep._1
            weaponProps += wep._2
          }
        }
        FetchAllWeaponsResponse(weapons, weaponProps)
      } pipeTo sender
    case FetchAllWeaponAttachmentsRequest =>
      Future {
        val JArray(weaponToAttachmentList) = {
          val aggregation = for {
            res1 <- sendAsyncRequest(ApiUrlBuilder.getAllWeaponAttachments(Page(Some(5000), Some(0))))
            res2 <- sendAsyncRequest(ApiUrlBuilder.getAllWeaponAttachments(Page(Some(5000), Some(5000))))
            res3 <- sendAsyncRequest(ApiUrlBuilder.getAllWeaponAttachments(Page(Some(5000), Some(10000))))
            res4 <- sendAsyncRequest(ApiUrlBuilder.getAllWeaponAttachments(Page(Some(5000), Some(15000))))
          } yield (res1, res2, res3, res4)

          val result = Await.result(aggregation, timeout.duration)
          parse(result._1.entity.asString) \ "weapon_to_attachment_list" ++
            parse(result._2.entity.asString) \ "weapon_to_attachment_list" ++
              parse(result._3.entity.asString) \ "weapon_to_attachment_list" ++
                parse(result._4.entity.asString) \ "weapon_to_attachment_list"
        }

        val attachments = mutable.ListBuffer.empty[WeaponAttachment]
        val effects = mutable.ListBuffer.empty[WeaponAttachmentEffect]
        weaponToAttachmentList.foreach { json =>
          val JString(weaponGroupId) = json \ "weapon_group_id"
          (json \ "item").toWeaponAttachment(weaponGroupId).map { wep =>
            attachments += wep._1
            effects ++= wep._2
          }
        }
        FetchAllWeaponAttachmentsResponse(attachments, effects.distinct)
      } pipeTo sender
  }

  private def sendAsyncRequest(r : Uri) : Future[HttpResponse] = {
    (IO(Http)(context.system) ? Get(r))(timeout).mapTo[HttpResponse]
  }

  private def sendRequest(r : Uri) : JValue = {
    parse(Await.result(sendAsyncRequest(r), timeout.duration).entity.asString)
  }
}

object FetcherActor {
  val timeout = Timeout(30000, TimeUnit.MILLISECONDS)

  type OutfitMember = (Character, OutfitMembership)

  //================================================================================
  // Character request/response
  //================================================================================
  case class FetchCharacterRequest(characterName : String, withStats : Boolean, statsLastSavedOn : Option[Long])
  case class FetchCharacterResponse(contents : Option[(Character, Option[OutfitMembership], Option[List[WeaponStat]], Option[List[ProfileStat]], Boolean)],
                                    request : (String, Boolean))

  //================================================================================
  // Outfit request/response
  //================================================================================
  case class FetchOutfitRequest(alias : String)
  case class FetchOutfitResponse(contents : Option[(Outfit, Seq[OutfitMember])], request : String)

  //================================================================================
  // Weapon request/response
  //================================================================================
  case object FetchAllWeaponsRequest
  case class FetchAllWeaponsResponse(weapons : Seq[Weapon], weaponProps : Seq[WeaponProps])

  //================================================================================
  // Weapon Attachment request/response
  //================================================================================
  case object FetchAllWeaponAttachmentsRequest
  case class FetchAllWeaponAttachmentsResponse(attachments : Seq[WeaponAttachment], effects : Seq[WeaponAttachmentEffect])
}