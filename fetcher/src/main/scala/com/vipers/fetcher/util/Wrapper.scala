package com.vipers.fetcher.util

import com.vipers.model.DatabaseModels._
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

        val JArray(stats) = json \ "characters_stat_history"
        val (kills, deaths, score) = {
          var kills = 0L
          var deaths = 0L
          var score = 0L

          stats.foreach { stat =>
            val JString(statName) = stat \ "stat_name"
            val JString(value) = stat \ "all_time"

            statName match {
              case "kills" => kills = value.toLong
              case "deaths" => deaths = value.toLong
              case "score" => score = value.toLong
            }
          }
          (kills, deaths, score)
        }

        Character(
          name,
          nameLower,
          id,
          kills,
          deaths,
          score,
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

    def toWeapon : Option[(Weapon, WeaponProps)] = {
      def toItemProfile(itemProfile : JValue) : Short = {
        val JString(profileId) = itemProfile \ "profile_id"
        profileId.toShort
      }

      implicit val jsonFormats = org.json4s.DefaultFormats
      check {
        val itemName = json \ "name" \ "en"
        val itemDesc = json \ "description" \ "en"
        val itemToWeapon = json \ "item_to_weapon"

        if(itemName == JNothing || itemDesc == JNothing || itemToWeapon == JNothing) {
          return None
        }

        val weapon = itemToWeapon \ "weapon"

        val (JString(itemId), JString(name), description, factionId, JString(imagePath), JString(isVehicleWeapon),
          JString(moveModifier), JString(turnModifier)) = {
          (
            json \ "item_id",
            itemName,
            itemDesc,
            json \ "faction_id",
            json \ "image_path",
            json \ "is_vehicle_weapon",
            weapon \ "move_modifier",
            weapon \ "turn_modifier"
          )
        }

        val (equipMs, fromIronSightsMs, toIronSightsMs, unEquipMs, sprintRecoveryMs) = {(
          weapon \ "equip_ms",
          weapon \ "from_iron_sights_ms",
          weapon \ "to_iron_sights_ms",
          weapon \ "unequip_ms",
          weapon \ "sprint_recovery_ms"
        )}

        val (heatBleedOffRate, heatCapacity, heatOverheatPenaltyMs) = {(
          weapon \ "heat_bleed_off_rate",
          weapon \ "heat_capacity",
          weapon \ "heat_overheat_penalty_ms"
        )}

        (Weapon(itemId, name, description.toOption.map(_.extract[String]), factionId.toOption.map(_.extract[String].toByte), imagePath, isVehicleWeapon match { case "0" => false; case "1" => true},
          System.currentTimeMillis(), {
            if(json \ "item_profile" != JNothing) {
              val JArray(profiles) = json \ "item_profile"
              Some(profiles.map( s => toItemProfile(s)).mkString(","))
            } else {
              None
            }
          }), WeaponProps(itemId, equipMs.toOption.map(_.extract[String].toInt),
            fromIronSightsMs.toOption.map(_.extract[String].toInt),
            toIronSightsMs.toOption.map(_.extract[String].toInt),
            unEquipMs.toOption.map(_.extract[String].toInt),
            sprintRecoveryMs.toOption.map(_.extract[String].toInt),
            moveModifier.toFloat,
            turnModifier.toFloat,
            heatBleedOffRate.toOption.map(_.extract[String].toFloat),
            heatCapacity.toOption.map(_.extract[String].toInt),
            heatOverheatPenaltyMs.toOption.map(_.extract[String].toInt)
          )
        )
      }
    }

    def toWeaponStats(characterId : String) : Option[List[WeaponStat]] = {
      check {
        if(json \ "characters_weapon_stat" == JNothing || json \ "characters_weapon_stat_by_faction" == JNothing) {
          return None
        }

        val map = mutable.Map[String, (Long, mutable.ListBuffer[(String, Long)])]().empty

        val JArray(weaponStat) = json \ "characters_weapon_stat"
        weaponStat.foreach { stat =>
          val JString(itemId) = stat \ "item_id"
          val JString(statName) = stat \ "stat_name"
          val JString(value) = stat \ "value"

          if(map.contains(itemId)) {
            map(itemId)._2 += ((statName, value.toLong))
          } else {
            val JString(lastSave) = stat \ "last_save"
            map += (itemId -> (lastSave.toLong, mutable.ListBuffer((statName, value.toLong))))
          }
        }

        val JArray(weaponStatByFaction) = json \ "characters_weapon_stat_by_faction"
        weaponStatByFaction.foreach { stat =>
          val JString(itemId) = stat \ "item_id"
          val JString(statName) = stat \ "stat_name"
          val JString(valueNc) = stat \ "value_nc"
          val JString(valueTr) = stat \ "value_tr"
          val JString(valueVs) = stat \ "value_vs"

          if(map.contains(itemId)) {
            map(itemId)._2 += ((statName, valueNc.toLong + valueTr.toLong + valueVs.toLong))
          } else {
            val JString(vehicleId) = stat \ "vehicle_id"
            if(vehicleId == "0") {
              val JString(lastSave) = stat \ "last_save"
              map += (itemId -> (lastSave.toLong, mutable.ListBuffer((statName, valueNc.toLong + valueTr.toLong + valueVs.toLong))))
            }
          }
        }

        val list = new mutable.ListBuffer[WeaponStat]

        for((itemId, (lastSave, stats)) <- map) {
          var fireCount = 0L
          var hitCount = 0L
          var hsCount = 0L
          var killCount = 0L
          var deathCount = 0L
          var score = 0L
          var secondsPlayed = 0L

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
            } else if(statName == "weapon_score") {
              score = value
            } else if(statName == "weapon_play_time") {
              secondsPlayed = value
            }
          }

          if(fireCount > 0 && killCount > 0) {
            list += WeaponStat(characterId, itemId, fireCount, hitCount, hsCount, killCount, deathCount, secondsPlayed, score, lastSave)
          }
        }
        list.toList
      }
    }

    def toProfileStats(characterId : String) : Option[List[ProfileStat]] = {
      check {
        if(json \ "characters_stat" == JNothing || json \ "characters_stat_by_faction" == JNothing) {
          return None
        }

        val map = mutable.Map[Short, (Long, mutable.ListBuffer[(String, Long)])]().empty

        val JArray(weaponStat) = json \ "characters_stat"
        weaponStat.foreach { stat =>
          val JString(pid) = stat \ "profile_id"
          val profileId = pid.toShort

          if(profileId != 0) {
            val JString(statName) = stat \ "stat_name"
            val JString(value) = stat \ "value_forever"

            if(map.contains(profileId)) {
              map(profileId)._2 += ((statName, value.toLong))
            } else {
              val JString(lastSave) = stat \ "last_save"
              map += (profileId -> (lastSave.toLong, mutable.ListBuffer((statName, value.toLong))))
            }
          }
        }

        val JArray(weaponStatByFaction) = json \ "characters_stat_by_faction"
        weaponStatByFaction.foreach { stat =>
          val JString(pid) = stat \ "profile_id"
          val profileId = pid.toShort

          if(profileId != 0) {
            val JString(statName) = stat \ "stat_name"
            val JString(valueNc) = stat \ "value_forever_nc"
            val JString(valueTr) = stat \ "value_forever_tr"
            val JString(valueVs) = stat \ "value_forever_vs"

            if(map.contains(profileId)) {
              map(profileId)._2 += ((statName, valueNc.toLong + valueTr.toLong + valueVs.toLong))
            } else {
              val JString(lastSave) = stat \ "last_save"
              map += (profileId -> (lastSave.toLong, mutable.ListBuffer((statName, valueNc.toLong + valueTr.toLong + valueVs.toLong))))
            }
          }
        }

        val list = new mutable.ListBuffer[ProfileStat]

        for((profileId, (lastSave, stats)) <- map) {
          var killedByCount = 0L
          var score = 0L
          var secondsPlayed = 0L

          for((statName, value) <- stats) {
            if(statName == "score") {
              score = value
            } else if(statName == "play_time") {
              secondsPlayed = value
            } else if(statName == "killed_by") {
              killedByCount = value
            }
          }

          list += ProfileStat(characterId, profileId, killedByCount, secondsPlayed, score)
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
