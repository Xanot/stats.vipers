package com.vipers.fetcher.util

import com.vipers.fetcher.model._
import com.vipers.fetcher.util.CensusQuery.CensusQueryCommand._
import org.json4s.JsonAST._
import scala.collection.mutable

private[fetcher] object Wrapper {
  implicit class ApiDeserializer(val json : JValue) extends AnyVal {
    def toOutfit : Option[Outfit] = {
      check { () =>
        val (JString(name), JString(alias), JString(id), JString(memberCount), JString(leaderCharacterId), JString(timeCreatedDate)) = {
          (
            json \ "name",
            json \ "alias",
            json \ "outfit_id",
            json \ "member_count",
            json \ "leader_character_id",
            json \ "time_created"
            )
        }

        Outfit(name, alias, leaderCharacterId, memberCount.toInt, id, timeCreatedDate.toLong, (json \ "leader_character").toCharacter(), {
          val list = mutable.ListBuffer.empty[Character]
          json \ "members" match {
            case JArray(parents) =>
              parents.foreach { parent =>
                list += (parent \ "character").toCharacter(parent.toOutfitMember).get
              }
              Some(list)
            case _ => None
          }
        })
      }
    }

    def toCharacter(memberShip : Option[OutfitMember] = None) : Option[Character] = {
      check { () =>
        val JString(name) = json \ "name" \ "first"
        val JString(id) = json \ "character_id"
        Character(name, id, (json \ "battle_rank").toBattleRank, (json \ "certs").toCerts, (json \ "faction").toFaction, (json \ "times").toTimes, memberShip)
      }
    }

    def toBattleRank : Option[BattleRank] = {
      check { () =>
        val (JString(battleRankPercentToNext), JString(battleRank)) = {
          (
            json \ "percent_to_next",
            json \ "value"
            )
        }
        BattleRank(battleRank.toShort, battleRankPercentToNext.toShort)
      }
    }

    def toCerts : Option[Certs] = {
      check { () =>
        val (JString(availablePoints), JString(earnedPoints), JString(giftedPoints), JString(percentToNext), JString(spentPoints)) = {
          (
            json \ "available_points",
            json \ "earned_points",
            json \ "gifted_points",
            json \ "percent_to_next",
            json \ "spent_points"
            )
        }
        Certs(availablePoints.toInt, earnedPoints.toInt, giftedPoints.toInt, percentToNext.toDouble, spentPoints.toInt)
      }
    }

    def toOutfitMember : Option[OutfitMember] = {
      check { () =>
        val (JString(rank), JString(rankOrdinal), JString(memberSinceDate)) = {
          (
            json.findField{case (k, v) => k.endsWith("rank")}.get._2, // match rank or member_rank
            json.findField{case (k, v) => k.endsWith("ordinal")}.get._2,
            json \ "member_since"
            )
        }
        OutfitMember(rank, rankOrdinal.toByte, memberSinceDate.toLong)
      }
    }

    def toFaction : Option[Faction] = {
      check { () =>
        val (JString(name), JString(codeTag), JString(id), JString(imageId), JString(imagePath), JString(imageSetId)) = {
          (
            json \ "name" \ "en",
            json \ "code_tag",
            json \ "faction_id",
            json \ "image_id",
            json \ "image_path",
            json \ "image_set_id"
            )
        }
        Faction(name, codeTag, id, imageId, imagePath, imageSetId)
      }
    }

    def toTimes : Option[Times] = {
      check { () =>
        val (JString(creationDate), JString(lastLoginDate), JString(lastSaveDate), JString(loginCount), JString(minutesPlayed)) = {
          (
            json \ "creation",
            json \ "last_login",
            json \ "last_save",
            json \ "login_count",
            json \ "minutes_played"
            )
        }
        Times(creationDate.toLong, lastLoginDate.toLong, lastSaveDate.toLong, loginCount.toInt, minutesPlayed.toInt)
      }
    }

    private def check[T](f : () => T) : Option[T] = {
      if(json != JNothing) {
        Some(f())
      } else {
        None
      }
    }
  }

  implicit class RichEnrichCharacter(val enrichCharacter : EnrichCharacter) extends AnyVal {
    def toJoin(injectAt : String = "character", on : Option[String] = None, to : Option[String] = None) : CharacterJoin = {
      val showAndHide = showHide()

      if(enrichCharacter.withFaction) {
        CharacterJoin(show = showAndHide._1, hide = showAndHide._2, nested = Some(FactionJoin()), on = on, to = to, injectAt = injectAt)
      } else {
        CharacterJoin(show = showAndHide._1, hide = showAndHide._2, on = on, to = to, injectAt = injectAt)
      }
    }

    def toQuery : CensusQuery = {
      val showAndHide = showHide()

      if(enrichCharacter.withFaction) {
        CensusQuery(None, Join(FactionJoin())) + showAndHide._1.map(s => Show(s:_*)) + showAndHide._2.map(h => Hide(h:_*))
      } else {
        CensusQuery(None) + showAndHide._1.map(s => Show(s:_*)) + showAndHide._2.map(h => Hide(h:_*))
      }
    }

    private def showHide() : (Option[Seq[String]], Option[Seq[String]]) = {
      val show = mutable.ListBuffer.empty[String]
      val hide = mutable.ListBuffer.empty[String]

      enrichCharacter.withBattleRank.map { withBattleRank =>
        if(withBattleRank) {
          show += "battle_rank"
        } else {
          hide += "battle_rank"
        }
      }

      enrichCharacter.withCerts.map { withCerts =>
        if(withCerts) {
          show += "certs"
        } else {
          hide += "certs"
        }
      }
      enrichCharacter.withTimes.map { withTimes =>
        if(withTimes) {
          show += "times"
        } else {
          hide += "times"
        }
      }

      (if(show.nonEmpty) Some(show) else None, if(hide.nonEmpty) Some(hide) else None)
    }
  }

  implicit class RichEnrichOutfit(val enrichOutfit : EnrichOutfit) extends AnyVal {
    def toJoin : List[JoinQuery] = {
      val joinList = mutable.ListBuffer.empty[JoinQuery]

      enrichOutfit.withLeaderCharacter.map { enrich =>
        joinList += enrich.toJoin(injectAt = "leader_character", on = Some("leader_character_id"), to = Some("character_id"))
      }
      enrichOutfit.withMemberCharacters.map { withMembers =>
        joinList += OutfitMemberJoin(nested = Some(withMembers.toJoin()))
      }

      joinList.toList
    }
  }
}
