package com.vipers.fetcher.util

import com.vipers.Test
import com.vipers.fetcher.model._
import org.scalatest.FlatSpecLike

class ApiUrlBuilderTest extends FlatSpecLike with Test {
  //================================================================================
  // Outfit
  //================================================================================
  "getOutfitByAlias uri" should "be constructed" in {
    val uri = ApiUrlBuilder.getOutfitByAlias("vipr")
    uri.path.tail.toString().endsWith("outfit") should be(right = true)
    uri.query.length should be(2)
    uri.query.get("c:lang").get should be("en")
    uri.query.get("alias_lower").get should be("vipr")
  }

  "enriched getOutfitByAlias uri" should "be constructed" in {
    // With leader character (with faction)
    {
      val enrichOutfit = EnrichOutfit(Some(EnrichCharacter(withFaction = true)), None)
      val uri = ApiUrlBuilder.getOutfitByAlias("vipr", Some(enrichOutfit))
      uri.path.tail.toString().endsWith("outfit") should be(right = true)
      uri.query.length should be(3)
      uri.query.get("c:lang").get should be("en")
      uri.query.get("c:join").get should be("character^inject_at:leader_character^on:leader_character_id^to:character_id(faction^inject_at:faction)")
      uri.query.get("alias_lower").get should be("vipr")
    }
    // With member characters
    {
      val enrichOutfit = EnrichOutfit(None, Some(EnrichCharacter()))
      val uri = ApiUrlBuilder.getOutfitByAlias("vipr", Some(enrichOutfit))
      uri.path.tail.toString().endsWith("outfit") should be(right = true)
      uri.query.length should be(3)
      uri.query.get("c:lang").get should be("en")
      uri.query.get("c:join").get should be("outfit_member^inject_at:members^list:1(character^inject_at:character)")
      uri.query.get("alias_lower").get should be("vipr")
    }
    // With leader (with faction) and member characters
    {
      val enrichOutfit = EnrichOutfit(withLeaderCharacter = Some(EnrichCharacter()), withMemberCharacters = Some(EnrichCharacter()))
      val uri = ApiUrlBuilder.getOutfitByAlias("vipr", Some(enrichOutfit))
      uri.path.tail.toString().endsWith("outfit") should be(right = true)
      uri.query.length should be(3)
      uri.query.get("c:lang").get should be("en")
      uri.query.get("c:join").get should be("character^inject_at:leader_character^on:leader_character_id^to:character_id,outfit_member^inject_at:members^list:1(character^inject_at:character)")
      uri.query.get("alias_lower").get should be("vipr")
    }
  }

  "getOutfitById uri" should "be constructed" in {
    val uri = ApiUrlBuilder.getOutfitById("1234")
    uri.path.tail.toString().endsWith("outfit") should be(right = true)
    uri.query.length should be(2)
    uri.query.get("c:lang").get should be("en")
    uri.query.get("outfit_id").get should be("1234")
  }

  "enriched getOutfitById uri" should "be constructed" in {
    // With leader character (with faction)
    {
      val enrichOutfit = EnrichOutfit(Some(EnrichCharacter(withFaction = true)), None)
      val uri = ApiUrlBuilder.getOutfitById("1234", Some(enrichOutfit))
      uri.path.tail.toString().endsWith("outfit") should be(right = true)
      uri.query.length should be(3)
      uri.query.get("c:lang").get should be("en")
      uri.query.get("c:join").get should be("character^inject_at:leader_character^on:leader_character_id^to:character_id(faction^inject_at:faction)")
      uri.query.get("outfit_id").get should be("1234")
    }
    // With member characters
    {
      val enrichOutfit = EnrichOutfit(None, Some(EnrichCharacter()))
      val uri = ApiUrlBuilder.getOutfitById("1234", Some(enrichOutfit))
      uri.path.tail.toString().endsWith("outfit") should be(right = true)
      uri.query.length should be(3)
      uri.query.get("c:lang").get should be("en")
      uri.query.get("c:join").get should be("outfit_member^inject_at:members^list:1(character^inject_at:character)")
      uri.query.get("outfit_id").get should be("1234")
    }
    // With leader (with faction) and member characters
    {
      val enrichOutfit = EnrichOutfit(Some(EnrichCharacter(withFaction = true)), Some(EnrichCharacter()))
      val uri = ApiUrlBuilder.getOutfitById("1234", Some(enrichOutfit))
      uri.path.tail.toString().endsWith("outfit") should be(right = true)
      uri.query.length should be(3)
      uri.query.get("c:lang").get should be("en")
      uri.query.get("c:join").get should be("character^inject_at:leader_character^on:leader_character_id^to:character_id(faction^inject_at:faction),outfit_member^inject_at:members^list:1(character^inject_at:character)")
      uri.query.get("outfit_id").get should be("1234")
    }
  }

  "getOutfitCharactersByAlias uri" should "be constructed" in {
    {
      val enrichOutfit = EnrichOutfit(Some(EnrichCharacter(withFaction = true)), None)
      val uri = ApiUrlBuilder.getOutfitCharactersByAlias("VIPR", Page(Some(10), Some(5)))
      uri.path.tail.toString().endsWith("outfit_member_extended") should be(right = true)
      uri.query.length should be(5)
      uri.query.get("c:lang").get should be("en")
      uri.query.get("c:limit").get should be("10")
      uri.query.get("c:start").get should be("5")
      uri.query.get("c:join").get should be("character^inject_at:character")
      uri.query.get("alias_lower").get should be("vipr")
    }
  }

  "getOutfitCharactersById uri" should "be constructed" in {
    {
      val enrichOutfit = EnrichOutfit(Some(EnrichCharacter(withFaction = true)), None)
      val uri = ApiUrlBuilder.getOutfitCharactersById("1234", Page(Some(2), Some(7)))
      uri.path.tail.toString().endsWith("outfit_member_extended") should be(right = true)
      uri.query.length should be(5)
      uri.query.get("c:lang").get should be("en")
      uri.query.get("c:limit").get should be("2")
      uri.query.get("c:start").get should be("7")
      uri.query.get("c:join").get should be("character^inject_at:character")
      uri.query.get("outfit_id").get should be("1234")
    }
  }

  //================================================================================
  // Character
  //================================================================================
  "getCharactersById uri" should "be constructed" in {
    val uri = ApiUrlBuilder.getCharactersById(None, "1", "2", "3", "4")
    uri.path.tail.toString().endsWith("character") should be(right = true)
    uri.query.length should be(2)
    uri.query.get("c:lang").get should be("en")
    uri.query.get("character_id").get should be("1,2,3,4")
  }

  "enrich getCharactersById uri" should "be constructed" in {
    val enrich = EnrichCharacter(withBattleRank = Some(true), withCerts = Some(true), withFaction = true)
    val uri = ApiUrlBuilder.getCharactersById(Some(enrich), "1", "2", "3", "4")
    uri.path.tail.toString().endsWith("character") should be(right = true)
    uri.query.length should be(4)
    uri.query.get("c:lang").get should be("en")
    uri.query.get("c:show").get should be("battle_rank,certs")
    uri.query.get("c:join").get should be("faction^inject_at:faction")
    uri.query.get("character_id").get should be("1,2,3,4")
  }

  "getCharacterByName uri" should "be constructed" in {
    val uri = ApiUrlBuilder.getCharacterByName("xanot", None)
    uri.path.tail.toString().endsWith("character") should be(right = true)
    uri.query.length should be(2)
    uri.query.get("c:lang").get should be("en")
    uri.query.get("name.first_lower").get should be("xanot")
  }

  "enrich getCharacterByName uri" should "be constructed" in {
    val enrich = EnrichCharacter(withBattleRank = Some(true), withCerts = Some(true))
    val uri = ApiUrlBuilder.getCharacterByName("xanot", Some(enrich))
    uri.path.tail.toString().endsWith("character") should be(right = true)
    uri.query.length should be(3)
    uri.query.get("c:lang").get should be("en")
    uri.query.get("c:show").get should be("battle_rank,certs")
    uri.query.get("name.first_lower").get should be("xanot")
  }
}
