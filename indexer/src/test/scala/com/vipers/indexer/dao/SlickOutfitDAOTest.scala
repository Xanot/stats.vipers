package com.vipers.indexer.dao

import java.sql.SQLException

import com.vipers.dao.DAOTest
import com.vipers.model.{Character, Outfit}
import com.vipers.indexer.dao.slick.{SlickOutfitMembershipDAOComponent, SlickCharacterDAOComponent, SlickOutfitDAOComponent}
import org.scalatest.WordSpecLike

class SlickOutfitDAOTest extends WordSpecLike with DAOTest with SlickDBTest
  with SlickOutfitDAOComponent with SlickCharacterDAOComponent with SlickOutfitMembershipDAOComponent {

  import driver.simple._

  override def beforeAll(): Unit = {
    withTransaction { implicit s =>
      (outfitDAO.table.ddl ++ characterDAO.table.ddl).create
    }
  }

  override def afterAll(): Unit = {
    withTransaction { implicit s =>
      (outfitDAO.table.ddl ++ characterDAO.table.ddl).drop
    }
  }

  "Outfit" should {
    "be created" in {
      val char = Character(
        "TestLeader",
        "test",
        "5428013610391601489",
        90,
        20,
        900,
        800,
        80,
        9000,
        1,
        System.currentTimeMillis(),
        System.currentTimeMillis(),
        System.currentTimeMillis(),
        100,
        15000,
        System.currentTimeMillis()
      )

      withTransaction { implicit s =>
        characterDAO.create(char)
        outfitDAO.create(Outfit("TheVipers", "thevipers", "VIPR", "vipr", "5428013610391601489", 126, 1, "37523756405021402", 1408310892, System.currentTimeMillis())) should be(true)
      }

      // duplicate id
      intercept[SQLException] {
        withTransaction { implicit s =>
          outfitDAO.create(Outfit("TheVipers", "thevipers", "VIPR", "vipr", "5428013610391601489", 126, 1, "37523756405021402", 1408310892, System.currentTimeMillis()))
        }
      }
    }

    "s be retrieved" in {
      withTransaction { implicit s =>
        outfitDAO.create(Outfit("test", "test", "test", "test", "5428013610391601489", 126, 1, "test", 1408310892, System.currentTimeMillis())) should be(true)
        outfitDAO.findAll.length should be(2)
      }
    }

    "leader character retrieved" in {
      withSession { implicit s =>
        outfitDAO.findLeader("37523756405021402").get.name should be("TestLeader")
      }
    }

    "be retrieved by id" in {
      withSession { implicit s =>
        outfitDAO.find("37523756405021402").get.name should be("TheVipers")
      }
    }

    "be retrieved by name-lower" in {
      withSession { implicit s =>
        outfitDAO.findByNameLower("thevipers").get.memberCount should be(126)
      }
    }

    "be retrieved by alias-lower" in {
      withSession { implicit s =>
        outfitDAO.findByAliasLower("vipr").get.name should be("TheVipers")
      }
    }

    "be updated" in {
      withTransaction { implicit s =>
        val m = outfitDAO.findByAliasLower("vipr").get
        outfitDAO.update(m.copy(memberCount = 50)) should be(true)
        outfitDAO.findByAliasLower("vipr").get.memberCount should be(50)
      }
    }

    "be deleted" in {
      withTransaction { implicit s =>
        outfitDAO.deleteById(outfitDAO.findByAliasLower("vipr").get.id) should be(true)
        outfitDAO.findByAliasLower("vipr") should be(None)
      }
    }
  }
}
