package com.vipers.fetcher.util

import com.vipers.fetcher.Configuration
import com.vipers.model.Page
import com.vipers.fetcher.util.CensusQuery.CensusQueryCommand._
import com.vipers.fetcher.util.CensusQuery.Search
import spray.http.Uri

private[fetcher] object ApiUrlBuilder {
  private val indexUri = Uri(Configuration.apiIndex).withQuery(Lang("en").construct)
  //================================================================================
  // Outfit
  //================================================================================
  def getOutfitByAlias(outfitAlias : String) : Uri = {
    val s = Search("alias_lower", outfitAlias.toLowerCase)
    val params =
      CensusQuery(Some(s),
        Join(
          CharacterJoin(injectAt = "leader", on = Some("leader_character_id"), to = Some("character_id")),
          OutfitMemberJoin(nested = Some(Seq(
            CharacterJoin(isOuter = Some(false), nested = Some(Seq(
              CharacterStatHistoryJoin(
                terms = Some(Seq(("stat_name", "kills"), ("stat_name", "deaths"), ("stat_name", "score"))),
                show = Some(Seq("stat_name", "all_time"))
              )
            )))
          )))
        )
      ).construct

    construct(Uri.Path("outfit"), params.toMap)
  }

  //================================================================================
  // Character
  //================================================================================
  def getCharacterByName(name : String, withStats : Boolean, statsLastSavedOn : Option[Long]) : Uri = {
    val s = Search("name.first_lower", name.toLowerCase)
    val params = {
      if(withStats) {
        CensusQuery(Some(s), Join(
          OutfitMemberJoin(injectAt = "membership", isList = Some(false)),
          CharacterStatHistoryJoin(
            terms = Some(Seq(("stat_name", "kills"), ("stat_name", "deaths"), ("stat_name", "score"))),
            show = Some(Seq("stat_name", "all_time"))
          ),
          CharacterWeaponStatsJoin(terms = Some(Seq(("last_save", ">" + statsLastSavedOn.getOrElse(0))))),
          CharacterWeaponStatsByFactionJoin(terms = Some(Seq(("last_save", ">" + statsLastSavedOn.getOrElse(0))))),
          CharacterProfileStatsJoin(terms = Some(Seq(("last_save", ">" + statsLastSavedOn.getOrElse(0))))),
          CharacterProfileStatsByFactionJoin(terms = Some(Seq(("last_save", ">" + statsLastSavedOn.getOrElse(0)))))
        )).construct
      } else {
        CensusQuery(Some(s), Join(
          OutfitMemberJoin(injectAt = "membership", isList = Some(false)),
          CharacterStatHistoryJoin(
            terms = Some(Seq(("stat_name", "kills"), ("stat_name", "deaths"), ("stat_name", "score"))),
            show = Some(Seq("stat_name", "all_time"))
          )
        )).construct
      }
    }
    construct(Uri.Path("character"), params.toMap)
  }

  //================================================================================
  // Weapon
  //================================================================================
  def getAllWeapons : Uri = {
    val params = CensusQuery(None, Limit(10000), Join(ItemToWeaponJoin(nested = Some(Seq(ItemJoin(), ItemProfileJoin()))))).construct
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