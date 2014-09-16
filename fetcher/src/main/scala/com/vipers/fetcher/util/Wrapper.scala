package com.vipers.fetcher.util

import com.vipers.model._
import org.json4s.JsonAST._
import scala.collection.mutable

private[fetcher] object Wrapper {
  implicit class ApiDeserializer(val json : JValue) extends AnyVal {
    def toOutfit : Option[Outfit] = {
      check {
        val (JString(name), JString(nameLower), JString(alias), JString(aliasLower), JString(id), JString(memberCount), JString(leaderCharacterId), JString(timeCreatedDate)) = {
          (
            json \ "name",
            json \ "name_lower",
            json \ "alias",
            json \ "alias_lower",
            json \ "outfit_id",
            json \ "member_count",
            json \ "leader_character_id",
            json \ "time_created"
            )
        }

        Outfit(name, nameLower, alias, aliasLower, leaderCharacterId, memberCount.toInt, id, timeCreatedDate.toLong)
      }
    }

    def toCharacter : Option[Character] = {
      check {
        val JString(name) = json \ "name" \ "first"
        val JString(id) = json \ "character_id"

        val br = json \ "battle_rank"
        val (JString(battleRankPercentToNext), JString(battleRank)) = (br \ "percent_to_next", br \ "value")

        val certs = json \ "certs"
        val (JString(availablePoints), JString(earnedPoints), JString(giftedPoints), JString(percentToNext), JString(spentPoints)) = {
          (
            certs \ "available_points",
            certs \ "earned_points",
            certs \ "gifted_points",
            certs \ "percent_to_next",
            certs \ "spent_points"
          )
        }

        val JString(factionCodeTag) = json \ "faction" \ "code_tag"

        val times = json \ "times"
        val (JString(creationDate), JString(lastLoginDate), JString(lastSaveDate), JString(loginCount), JString(minutesPlayed)) = {
          (
            times \ "creation",
            times \ "last_login",
            times \ "last_save",
            times \ "login_count",
            times \ "minutes_played"
          )
        }

        Character(
          name,
          id,
          battleRank.toShort,
          battleRankPercentToNext.toShort,
          availablePoints.toInt,
          earnedPoints.toInt,
          percentToNext.toDouble.toShort,
          spentPoints.toInt,
          factionCodeTag,
          creationDate.toLong,
          lastLoginDate.toLong,
          lastSaveDate.toLong,
          loginCount.toInt,
          minutesPlayed.toInt)
      }
    }

    def toOutfitMembership : Option[OutfitMembership] = {
      check {
        val (JString(outfitId), JString(rank), JString(rankOrdinal), JString(memberSinceDate)) = {
          (
            json \ "outfit_id",
            json.findField{case (k, v) => k.endsWith("rank")}.get._2, // match rank or member_rank
            json.findField{case (k, v) => k.endsWith("ordinal")}.get._2,
            json \ "member_since"
            )
        }
        OutfitMembership(outfitId, rank, rankOrdinal.toByte, memberSinceDate.toLong)
      }
    }

    private def check[T](f : => T) : Option[T] = {
      if(json != JNothing) {
        Some(f)
      } else {
        None
      }
    }
  }
}
