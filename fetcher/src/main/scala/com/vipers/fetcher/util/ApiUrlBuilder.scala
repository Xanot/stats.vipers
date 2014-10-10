package com.vipers.fetcher.util

import com.vipers.fetcher.Configuration
import com.vipers.model.Page
import com.vipers.model.Sort.{Sort => SortModel, MEMBER_COUNT, CREATION_DATE}
import com.vipers.fetcher.util.CensusQuery.CensusQueryCommand._
import com.vipers.fetcher.util.CensusQuery.CensusQueryCommand.{Sort => SortQ}
import com.vipers.fetcher.util.CensusQuery.Search
import spray.http.Uri

private[fetcher] object ApiUrlBuilder {
  private val indexUri = Uri(Configuration.apiIndex).withQuery(Lang("en").construct)
  //================================================================================
  // Outfit
  //================================================================================
  private def getOutfit(s : Search, isSimple : Boolean) : Uri = {
    val params = isSimple match {
      case true =>
        CensusQuery(Some(s),
          Join(
            CharacterJoin(injectAt = "leader", on = Some("leader_character_id"), to = Some("character_id"))
          )
        ).construct
      case false =>
        CensusQuery(Some(s),
          Join(
            CharacterJoin(injectAt = "leader", on = Some("leader_character_id"), to = Some("character_id")),
            OutfitMemberJoin(nested = Some(CharacterJoin(isOuter = Some(false))))
          )
        ).construct
    }
    construct(Uri.Path("outfit"), params.toMap)
  }

  def getSimpleOutfits(s : SortModel, p : Page) : Uri = {
    val sort = SortQ(
      (s._1 match {
        case CREATION_DATE => "time_created"
        case MEMBER_COUNT => "member_count"
      }, s._2)
    )
    val join = Join(CharacterJoin(injectAt = "leader", on = Some("leader_character_id"), to = Some("character_id")))
    val params = CensusQuery(None, sort, join).construct
    construct(Uri.Path("outfit"), params.toMap ++ withPage(p))
  }

  def getOutfitByAlias(outfitAlias : String, isSimple : Boolean) : Uri = {
    getOutfit(Search("alias_lower", outfitAlias.toLowerCase), isSimple)
  }

  def getOutfitById(outfitId : String, isSimple : Boolean) : Uri = {
    getOutfit(Search("outfit_id", outfitId), isSimple)
  }

  private def getOutfitCharacters(s : Search, page : Page) : Uri = {
    val params = CensusQuery(Some(s), Join(CharacterJoin(isOuter = Some(false)))).construct
    construct(Uri.Path("outfit_member_extended"), params.toMap ++ withPage(page))
  }

  def getOutfitCharactersByAlias(alias : String, page : Page) : Uri = {
    getOutfitCharacters(Search("alias_lower", alias.toLowerCase), page)
  }

  def getOutfitCharactersById(outfitId : String, page : Page) : Uri = {
    getOutfitCharacters(Search("outfit_id", outfitId), page)
  }

  //================================================================================
  // Character
  //================================================================================
  private def getCharacters(s : Search, withStats : Boolean) : Uri = {
    val params = {
      if(withStats) {
        CensusQuery(Some(s), Join(OutfitMemberJoin(injectAt = "membership", isList = Some(false))), Resolve("weapon_stat", "weapon_stat_by_faction")).construct
      } else {
        CensusQuery(Some(s), Join(OutfitMemberJoin(injectAt = "membership", isList = Some(false)))).construct
      }
    }
    construct(Uri.Path("character"), params.toMap)
  }

  def getCharactersById(ids : String*) : Uri = {
    getCharacters(Search("character_id", ids.mkString(",")), false)
  }
  def getCharacterByName(name : String, withStats : Boolean) : Uri = {
    getCharacters(Search("name.first_lower", name.toLowerCase), withStats)
  }

  //================================================================================
  // Weapon
  //================================================================================
  def getAllWeapons : Uri = {
    val params = CensusQuery(None, Limit(10000), Join(ItemToWeaponJoin(nested = Some(ItemJoin())))).construct
    construct(Uri.Path("weapon"), params.toMap)
  }

  //================================================================================
  // Utility
  //================================================================================
  private def construct(path : Uri.Path, params : Map[String, String]) : Uri = {
    indexUri.copy(path = indexUri.path ++ path, query = Uri.Query(indexUri.query.toMap ++ params))
  }

  private def withPage(page : Page) : Seq[(String, String)] = {
    val records = page.records.map { records => Seq(Limit(records).construct) }.getOrElse(Nil)
    val start = page.start.map { start => Seq(Start(start).construct) }.getOrElse(Nil)

    records ++ start
  }
}