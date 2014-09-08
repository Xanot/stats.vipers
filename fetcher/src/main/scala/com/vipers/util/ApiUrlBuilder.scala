package com.vipers.util

import com.vipers.Configuration
import com.vipers.model._
import com.vipers.util.CensusQuery.CensusQueryCommand._
import com.vipers.util.CensusQuery.Search
import spray.http.Uri
import Wrapper.{RichEnrichCharacter, RichEnrichOutfit}

object ApiUrlBuilder {
  private val indexUri = Uri(Configuration.apiIndex).withQuery(Lang("en").construct)
  //================================================================================
  // Outfit
  //================================================================================
  private def getOutfit(s : Search, enrichOutfit : Option[EnrichOutfit]) : Uri = {
    val params = enrichOutfit.map { enrichOutfit =>
      CensusQuery(Some(s), Join(enrichOutfit.toJoin:_*)).construct
    }.getOrElse {
      CensusQuery(Some(s)).construct
    }
    construct(Uri.Path("outfit"), params.toMap)
  }

  def getOutfitByAlias(outfitAlias : String, enrichOutfit : Option[EnrichOutfit] = None) : Uri = {
    getOutfit(Search("alias_lower", outfitAlias.toLowerCase), enrichOutfit)
  }

  def getOutfitById(outfitId : String, enrichOutfit : Option[EnrichOutfit] = None) : Uri = {
    getOutfit(Search("outfit_id", outfitId), enrichOutfit)
  }

  private def getOutfitCharacters(s : Search, page : Page, enrich : Option[EnrichCharacter]) : Uri = {
    val params = enrich.map { enrichChar =>
      CensusQuery(Some(s), Join(enrichChar.toJoin())).construct
    }.getOrElse {
      CensusQuery(Some(s), Join(CharacterJoin())).construct
    }
    construct(Uri.Path("outfit_member_extended"), params.toMap ++ withPage(page))
  }

  def getOutfitCharactersByAlias(alias : String, page : Page, enrich : Option[EnrichCharacter] = None) : Uri = {
    getOutfitCharacters(Search("alias_lower", alias.toLowerCase), page, enrich)
  }

  def getOutfitCharactersById(outfitId : String, page : Page, enrich : Option[EnrichCharacter] = None) : Uri = {
    getOutfitCharacters(Search("outfit_id", outfitId), page, enrich)
  }

  //================================================================================
  // Character
  //================================================================================
  private def getCharacters(s : Search, enrich : Option[EnrichCharacter], ids : String*) : Uri = {
    val params = enrich.map { enrichChar =>
      (CensusQuery(Some(s)) ++ enrichChar.toQuery).construct
    }.getOrElse {
      CensusQuery(Some(s)).construct
    }
    construct(Uri.Path("character"), params.toMap)
  }

  def getCharactersById(enrich : Option[EnrichCharacter], ids : String*) : Uri = {
    getCharacters(Search("character_id", ids.mkString(",")), enrich)
  }
  def getCharacterByName(name : String, enrich : Option[EnrichCharacter]) : Uri = {
    getCharacters(Search("name.first_lower", name.toLowerCase), enrich)
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