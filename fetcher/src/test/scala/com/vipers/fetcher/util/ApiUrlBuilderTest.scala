package com.vipers.fetcher.util

import com.vipers.Test
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

  //================================================================================
  // Character
  //================================================================================
  "getCharacterByName uri" should "be constructed" in {
    // Without stats
    var uri = ApiUrlBuilder.getCharacterByName("xanot", false)
    uri.path.tail.toString().endsWith("character") should be(right = true)
    uri.query.length should be(3)
    uri.query.get("c:lang").get should be("en")
    uri.query.get("c:join").get should be("outfit_member^inject_at:membership^list:0")
    uri.query.get("name.first_lower").get should be("xanot")

    // With stats
    uri = ApiUrlBuilder.getCharacterByName("xanot", true)
    uri.path.tail.toString().endsWith("character") should be(right = true)
    uri.query.length should be(4)
    uri.query.get("c:lang").get should be("en")
    uri.query.get("c:join").get should be("outfit_member^inject_at:membership^list:0")
    uri.query.get("c:resolve").get should be("weapon_stat,weapon_stat_by_faction")
    uri.query.get("name.first_lower").get should be("xanot")
  }

  //================================================================================
  // Weapon
  //================================================================================
  "getAllWeapons uri" should "be constructed" in {
    val uri = ApiUrlBuilder.getAllWeapons
    uri.path.tail.toString().endsWith("weapon") should be(right = true)
    uri.query.length should be(3)
    uri.query.get("c:lang").get should be("en")
    uri.query.get("c:limit").get should be("10000")
    uri.query.get("c:join").get should be("item_to_weapon^inject_at:item_to_weapon(item^inject_at:item)")
  }
}
