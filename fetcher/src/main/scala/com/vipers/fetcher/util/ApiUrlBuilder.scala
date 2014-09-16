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
      case true => CensusQuery(Some(s)).construct
      case false => CensusQuery(Some(s),
        Join(
          CharacterJoin(injectAt = "leader", on = Some("leader_character_id"), to = Some("character_id"), nested = Some(FactionJoin())),
          OutfitMemberJoin(nested = Some(CharacterJoin(isOuter = Some(false), nested = Some(FactionJoin()))))
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
    val params = CensusQuery(None, sort).construct
    construct(Uri.Path("outfit"), params.toMap ++ withPage(p))
  }

  def getOutfitByAlias(outfitAlias : String, isSimple : Boolean) : Uri = {
    getOutfit(Search("alias_lower", outfitAlias.toLowerCase), isSimple)
  }

  def getOutfitById(outfitId : String, isSimple : Boolean) : Uri = {
    getOutfit(Search("outfit_id", outfitId), isSimple)
  }

  private def getOutfitCharacters(s : Search, page : Page) : Uri = {
    val params = CensusQuery(Some(s), Join(CharacterJoin(isOuter = Some(false), nested = Some(FactionJoin())))).construct
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
  private def getCharacters(s : Search, ids : String*) : Uri = {
    val params = CensusQuery(Some(s), Join(FactionJoin())).construct
    construct(Uri.Path("character"), params.toMap)
  }

  def getCharactersById(ids : String*) : Uri = {
    getCharacters(Search("character_id", ids.mkString(",")))
  }
  def getCharacterByName(name : String) : Uri = {
    getCharacters(Search("name.first_lower", name.toLowerCase))
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