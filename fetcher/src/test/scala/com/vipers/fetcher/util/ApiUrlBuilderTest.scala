package com.vipers.fetcher.util

import com.vipers.Test
import com.vipers.model.{Sort, Page}
import org.scalatest.FlatSpecLike

class ApiUrlBuilderTest extends FlatSpecLike with Test {
  //================================================================================
  // Outfit
  //================================================================================
  "getOutfitByAlias uri" should "be constructed" in {
    // Simple (with leader and faction)
    {
      val uri = ApiUrlBuilder.getOutfitByAlias("vipr", isSimple = true)
      uri.path.tail.toString().endsWith("outfit") should be(right = true)
      uri.query.length should be(3)
      uri.query.get("c:lang").get should be("en")
      uri.query.get("c:join").get should be("character^inject_at:leader^on:leader_character_id^to:character_id")
      uri.query.get("alias_lower").get should be("vipr")
    }

    // With leader (with faction) and member characters
    {
      val uri = ApiUrlBuilder.getOutfitByAlias("vipr", isSimple = false)
      uri.path.tail.toString().endsWith("outfit") should be(right = true)
      uri.query.length should be(3)
      uri.query.get("c:lang").get should be("en")
      uri.query.get("c:join").get should be("character^inject_at:leader^on:leader_character_id^to:character_id,outfit_member^inject_at:members^list:1(character^inject_at:character^outer:0)")
      uri.query.get("alias_lower").get should be("vipr")
    }
  }

  "getOutfitById uri" should "be constructed" in {
    // Simple (with leader and faction)
    {
      val uri = ApiUrlBuilder.getOutfitById("1234", isSimple = true)
      uri.path.tail.toString().endsWith("outfit") should be(right = true)
      uri.query.length should be(3)
      uri.query.get("c:lang").get should be("en")
      uri.query.get("c:join").get should be("character^inject_at:leader^on:leader_character_id^to:character_id")
      uri.query.get("outfit_id").get should be("1234")
    }

    // With leader (with faction) and member characters
    {
      val uri = ApiUrlBuilder.getOutfitById("1234", isSimple = false)
      uri.path.tail.toString().endsWith("outfit") should be(right = true)
      uri.query.length should be(3)
      uri.query.get("c:lang").get should be("en")
      uri.query.get("c:join").get should be("character^inject_at:leader^on:leader_character_id^to:character_id,outfit_member^inject_at:members^list:1(character^inject_at:character^outer:0)")
      uri.query.get("outfit_id").get should be("1234")
    }
  }

  "getOutfitCharactersByAlias uri" should "be constructed" in {
    {
      val uri = ApiUrlBuilder.getOutfitCharactersByAlias("VIPR", Page(Some(10), Some(5)))
      uri.path.tail.toString().endsWith("outfit_member_extended") should be(right = true)
      uri.query.length should be(5)
      uri.query.get("c:lang").get should be("en")
      uri.query.get("c:limit").get should be("10")
      uri.query.get("c:start").get should be("5")
      uri.query.get("c:join").get should be("character^inject_at:character^outer:0")
      uri.query.get("alias_lower").get should be("vipr")
    }
  }

  "getOutfitCharactersById uri" should "be constructed" in {
    {
      val uri = ApiUrlBuilder.getOutfitCharactersById("1234", Page(Some(2), Some(7)))
      uri.path.tail.toString().endsWith("outfit_member_extended") should be(right = true)
      uri.query.length should be(5)
      uri.query.get("c:lang").get should be("en")
      uri.query.get("c:limit").get should be("2")
      uri.query.get("c:start").get should be("7")
      uri.query.get("c:join").get should be("character^inject_at:character^outer:0")
      uri.query.get("outfit_id").get should be("1234")
    }
  }

  "getOutfits uri" should "be constructed" in {
    val uri = ApiUrlBuilder.getSimpleOutfits((Sort.CREATION_DATE, Sort.ASC), Page(Some(1), Some(5)))
    uri.path.tail.toString().endsWith("outfit") should be(right = true)
    uri.query.length should be(5)
    uri.query.get("c:lang").get should be("en")
    uri.query.get("c:limit").get should be("1")
    uri.query.get("c:join").get should be("character^inject_at:leader^on:leader_character_id^to:character_id")
    uri.query.get("c:start").get should be("5")
    uri.query.get("c:sort").get should be("time_created:1")
  }

  //================================================================================
  // Character
  //================================================================================
  "getCharactersById uri" should "be constructed" in {
    val uri = ApiUrlBuilder.getCharactersById("1", "2", "3", "4")
    uri.path.tail.toString().endsWith("character") should be(right = true)
    uri.query.length should be(2)
    uri.query.get("c:lang").get should be("en")
    uri.query.get("character_id").get should be("1,2,3,4")
  }

  "getCharacterByName uri" should "be constructed" in {
    val uri = ApiUrlBuilder.getCharacterByName("xanot")
    uri.path.tail.toString().endsWith("character") should be(right = true)
    uri.query.length should be(2)
    uri.query.get("c:lang").get should be("en")
    uri.query.get("name.first_lower").get should be("xanot")
  }
}
