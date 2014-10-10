package com.vipers.fetcher.util

import com.vipers.model._
import org.json4s.JsonAST._
import scala.collection.mutable

private[fetcher] object Wrapper {
  implicit class ApiDeserializer(val json : JValue) extends AnyVal {
    def toOutfit : Option[Outfit] = {
      check {
        val (JString(name), JString(nameLower), JString(alias), JString(aliasLower), JString(id), JString(memberCount), JString(leaderCharacterId), JString(factionId), JString(timeCreatedDate)) = {
          (
            json \ "name",
            json \ "name_lower",
            json \ "alias",
            json \ "alias_lower",
            json \ "outfit_id",
            json \ "member_count",
            json \ "leader_character_id",
            json \ "leader" \ "faction_id",
            json \ "time_created"
          )
        }

        Outfit(name, nameLower, alias, aliasLower, leaderCharacterId, memberCount.toInt, factionId.toByte, id, timeCreatedDate.toLong, System.currentTimeMillis())
      }
    }

    def toCharacter : Option[Character] = {
      check {
        val JString(name) = json \ "name" \ "first"
        val JString(nameLower) = json \ "name" \ "first_lower"
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

        val JString(factionId) = json \ "faction_id"

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
          nameLower,
          id,
          battleRank.toShort,
          battleRankPercentToNext.toShort,
          availablePoints.toInt,
          earnedPoints.toInt,
          percentToNext.toDouble.toShort,
          spentPoints.toInt,
          factionId.toByte,
          creationDate.toLong,
          lastLoginDate.toLong,
          lastSaveDate.toLong,
          loginCount.toInt,
          minutesPlayed.toInt,
          System.currentTimeMillis())
      }
    }

    def toOutfitMembership : Option[OutfitMembership] = {
      check {
        val (JString(outfitId), JString(characterId), JString(rank), JString(rankOrdinal), JString(memberSinceDate)) = {
          (
            json \ "outfit_id",
            json \ "character_id",
            json.findField{case (k, v) => k.endsWith("rank")}.get._2, // match rank or member_rank
            json.findField{case (k, v) => k.endsWith("ordinal")}.get._2,
            json \ "member_since"
            )
        }
        OutfitMembership(outfitId, characterId, rank, rankOrdinal.toByte, memberSinceDate.toLong)
      }
    }

    def toWeapon : Option[Weapon] = {
      implicit val jsonFormats = org.json4s.DefaultFormats
      check {
        val item = json \ "item_to_weapon" \ "item"
        val itemName = item \ "name" \ "en"
        val itemDescription = item \ "description" \ "en"
        val (JString(weaponId), JString(name), description, factionId, JString(imagePath), JString(isVehicleWeapon),
          JString(moveModifier), JString(turnModifier)) = {
          (
            item \ "item_id",
            itemName,
            itemDescription,
            item \ "faction_id",
            item \ "image_path",
            item \ "is_vehicle_weapon",
            json \ "move_modifier",
            json \ "turn_modifier"
          )
        }

        val (equipMs, fromIronSightsMs, toIronSightsMs, unEquipMs, sprintRecoveryMs) = {(
          json \ "equip_ms",
          json \ "from_iron_sights_ms",
          json \ "to_iron_sights_ms",
          json \ "unequip_ms",
          json \ "sprint_recovery_ms"
        )}

        val (heatBleedOffRate, heatCapacity, heatOverheatPenaltyMs) = {(
          json \ "heat_bleed_off_rate",
          json \ "heat_capacity",
          json \ "heat_overheat_penalty_ms"
        )}

        Weapon(weaponId, name, description.toOption.map(_.extract[String]), factionId.toOption.map(_.extract[String].toByte), imagePath, isVehicleWeapon match { case "0" => false; case "1" => true},
          equipMs.toOption.map(_.extract[String].toInt),
          fromIronSightsMs.toOption.map(_.extract[String].toInt),
          toIronSightsMs.toOption.map(_.extract[String].toInt),
          unEquipMs.toOption.map(_.extract[String].toInt),
          sprintRecoveryMs.toOption.map(_.extract[String].toInt),
          moveModifier.toFloat,
          turnModifier.toFloat,
          heatBleedOffRate.toOption.map(_.extract[String].toFloat),
          heatCapacity.toOption.map(_.extract[String].toInt),
          heatOverheatPenaltyMs.toOption.map(_.extract[String].toInt),
          System.currentTimeMillis())
      }
    }

    def toWeaponStats(characterId : String) : Option[List[WeaponStat]] = {
      check {
        val map = mutable.Map[String, mutable.ListBuffer[(String, Long)]]().empty

        val JArray(weaponStat) = json \ "weapon_stat"
        weaponStat.foreach { stat =>
          val JString(itemId) = stat \ "item_id"
          val JString(statName) = stat \ "stat_name"
          val JString(value) = stat \ "value"
          val JString(vehicleId) = stat \ "vehicle_id"

          if(vehicleId == "0") {
            if(map.contains(itemId)) {
              map(itemId) += ((statName, value.toLong))
            } else {
              map += (itemId -> mutable.ListBuffer((statName, value.toLong)))
            }
          }
        }

        val JArray(weaponStatByFaction) = json \ "weapon_stat_by_faction"
        weaponStatByFaction.foreach { stat =>
          val JString(itemId) = stat \ "item_id"
          val JString(statName) = stat \ "stat_name"
          val JString(valueNc) = stat \ "value_nc"
          val JString(valueTr) = stat \ "value_tr"
          val JString(valueVs) = stat \ "value_vs"
          val JString(vehicleId) = stat \ "vehicle_id"

          // TODO: Don't count teamkills (or count as different column)
          if(vehicleId == "0") {
            if(map.contains(itemId)) {
              map(itemId) += ((statName, valueNc.toLong + valueTr.toLong + valueVs.toLong))
            } else {
              map += (itemId -> mutable.ListBuffer((statName, valueNc.toLong + valueTr.toLong + valueVs.toLong)))
            }
          }
        }

        val list = new mutable.ListBuffer[WeaponStat]

        for((itemId, stats) <- map) {
          var fireCount = 0L
          var hitCount = 0L
          var hsCount = 0L
          var killCount = 0L
          var deathCount = 0L

          for((statName, value) <- stats) {
            if (statName == "weapon_hit_count") {
              hitCount = value
            } else if(statName == "weapon_headshots") {
              hsCount = value
            } else if(statName == "weapon_fire_count") {
              fireCount = value
            } else if(statName == "weapon_kills") {
              killCount = value
            } else if(statName == "weapon_deaths") {
              deathCount = value
            }
          }

          if(fireCount > 0 && killCount > 0) {
            list += WeaponStat(characterId, itemId, fireCount, hitCount, hsCount, killCount, deathCount, System.currentTimeMillis())
          }
        }
        list.toList
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
