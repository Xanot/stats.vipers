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
  def getAllWeapons(page : Page) : Uri = {
    val params = CensusQuery(None, Join(
      ItemToWeaponJoin(nested = Some(Seq(WeaponJoin())), isOuter = Some(false)),
      ItemProfileJoin(show = Some(Seq("profile_id")))
    )).construct
    construct(Uri.Path("item"), params.toMap ++ withPage(page))
  }

  //================================================================================
  // Weapon Attachments
  //================================================================================
  def getAllWeaponAttachments(page : Page) : Uri = {
    val params = CensusQuery(None, Join(
      ItemJoin(isOuter = Some(false), nested = Some(Seq(
        ZoneEffectJoin(on = Some("passive_ability_id"), to = Some("ability_id"), isOuter = Some(false))
      )))
    )).construct
    construct(Uri.Path("weapon_to_attachment"), params.toMap ++ withPage(page))
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