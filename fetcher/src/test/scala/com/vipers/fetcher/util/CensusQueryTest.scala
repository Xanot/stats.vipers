package com.vipers.fetcher.util

import com.vipers.Test
import com.vipers.model.{Sort => SortModel}
import SortModel.{ASC, DESC}
import com.vipers.fetcher.util.CensusQuery.CensusQueryCommand._
import com.vipers.fetcher.util.CensusQuery.Search
import org.scalatest.WordSpecLike

class CensusQueryTest extends WordSpecLike with Test {
  "SimpleCensusQueryCommand" should {
    "construct Case command" in {
      Case(true).construct should be(("c:case", "true"))
      Case(false).construct should be(("c:case", "false"))
    }
    "construct Limit command" in {
      Limit(20).construct should be(("c:limit", "20"))
    }
    "construct LimitPerDB command" in {
      LimitPerDB(20).construct should be(("c:limitPerDB", "20"))
    }
    "construct Start command" in {
      Start(5).construct should be(("c:start", "5"))
    }
    "construct IncludeNull command" in {
      IncludeNull(true).construct should be(("c:includeNull", "true"))
      IncludeNull(false).construct should be(("c:includeNull", "false"))
    }
    "construct Lang command" in {
      Lang("en").construct should be(("c:lang", "en"))
    }
    "construct Timing command" in {
      Timing(true).construct should be(("c:timing", "true"))
      Timing(false).construct should be(("c:timing", "false"))
    }
  }
  "CommaSeparatedCensusQueryCommand" should {
    "construct Resolve command" in {
      Resolve("coll1", "coll2").construct should be(("c:resolve", "coll1,coll2"))
    }
    "construct Hide command" in {
      Hide("field1", "field2").construct should be(("c:hide", "field1,field2"))
    }
    "construct Show command" in {
      Show("field3", "field4").construct should be(("c:show", "field3,field4"))
    }
    "construct Has command" in {
      Has("field5", "field6").construct should be(("c:has", "field5,field6"))
    }
    "construct Sort command" in {
      Sort(("field", DESC), ("field", ASC)).construct should be(("c:sort", "field:-1,field:1"))
    }
  }
  "CommaSeparatedCensusQueryCommand" should {
    "construct Join command" in {
      Join(
        new JoinQuery(
          "weapon",
          "my_weapon",
          isList = Some(false),
          on = Some("weapon_id"),
          to = Some("some_other_weapon_id"),
          terms = Some(Seq(("termKey", "termValue"), ("termKey2", "termValue2"))),
          hide = Some(Seq("hideField", "hideField2")),
          show = Some(Seq("showField", "showField2")),
          nested = Some(Seq(CharacterJoin(isOuter = Some(true)))),
          isOuter = Some(false)
      ) {}, FactionJoin()).construct match {
        case (command, v) =>
          command should be("c:join")
          v should be ("weapon^inject_at:my_weapon^list:0^on:weapon_id^to:some_other_weapon_id^terms:termKey=termValue'termKey2=termValue2^hide:hideField'hideField2^show:showField'showField2^outer:0(character^inject_at:character^outer:1),faction^inject_at:faction")
      }
    }
  }
  "CensusQuery" should {
    "be constructed with Search and without commands" in {
      CensusQuery(Some(Search("name.first_lower", "xanot"))).construct match {
        case search :: Nil => search should be(("name.first_lower", "xanot"))
      }
    }
    "be constructed without Search and without commands" in {
      CensusQuery(None).construct.length should be(0)
    }
    "be constructed with Search and several commands" in {
      CensusQuery(Some(Search("name.first_lower", "xanot")), commands =
        Show("field1", "field2"),
        Timing(true)
      ).construct match {
        case search :: show :: timing :: Nil =>
          search should be(("name.first_lower", "xanot"))
          show should be(("c:show", "field1,field2"))
          timing should be(("c:timing", "true"))
      }
    }
    "be constructed without Search and several commands" in {
      CensusQuery(None, commands =
        Show("field1", "field2"),
        Timing(true),
        Join(CharacterJoin(nested = Some(Seq(FactionJoin()))), FactionJoin())
      ).construct match {
        case show :: timing :: join :: Nil =>
          show should be(("c:show", "field1,field2"))
          timing should be(("c:timing", "true"))
          join should be(("c:join",
            "character^inject_at:character(faction^inject_at:faction),faction^inject_at:faction"
          ))
      }
    }
    "be added with another CensusQuery" in {
      val p = CensusQuery(Some(Search("alias_lower", "vipr")), commands =
        Show("field1", "field2"),
        Timing(true),
        Join(CharacterJoin(nested = Some(Seq(FactionJoin()))), FactionJoin())
      ) ++ CensusQuery(None, commands = Case(true), Limit(10))

      p.construct match {
        case search:: show :: timing :: join :: cas :: limit :: Nil =>
          search should be(("alias_lower", "vipr"))
          show should be(("c:show", "field1,field2"))
          timing should be(("c:timing", "true"))
          join should be(("c:join",
            "character^inject_at:character(faction^inject_at:faction),faction^inject_at:faction"
          ))
          cas should be(("c:case", "true"))
          limit should be(("c:limit", "10"))
      }

      (CensusQuery(None, Start(5)) ++ None ++ Some(CensusQuery(None, Limit(10)))).construct match {
        case start :: limit :: Nil =>
          start should be(("c:start", "5"))
          limit should be(("c:limit", "10"))
      }
    }
    "be added with CensusQueryCommand" in {
      (CensusQuery(None, commands = Case(true)) + None + Limit(10)).construct match {
        case cas :: limit :: Nil =>
          cas should be(("c:case", "true"))
          limit should be(("c:limit", "10"))
      }
    }
  }
}