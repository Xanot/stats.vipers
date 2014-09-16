package com.vipers.indexer.dao

import java.sql.SQLException

import com.vipers.dao.DAOTest
import com.vipers.model.Outfit
import com.vipers.indexer.dao.slick.SlickOutfitDAOComponent
import org.scalatest.WordSpecLike

class SlickOutfitDAOTest extends WordSpecLike with DAOTest with SlickDBTest with SlickOutfitDAOComponent {
  import driver.simple._

  override def beforeAll(): Unit = {
    withTransaction { implicit s =>
      outfitDAO.table.ddl.create
    }
  }

  override def afterAll(): Unit = {
    withTransaction { implicit s =>
      outfitDAO.table.ddl.drop
    }
  }

  "Outfit" should {
    "be created" in {
      withTransaction { implicit s =>
        outfitDAO.create(Outfit("TheVipers", "thevipers", "VIPR", "vipr", "5428013610391601489", 126, "37523756405021402", 1408310892)) should be(true)
      }

      // duplicate id
      intercept[SQLException] {
        withTransaction { implicit s =>
          outfitDAO.create(Outfit("TheVipers", "thevipers", "VIPR", "vipr", "5428013610391601489", 126, "37523756405021402", 1408310892))
        }
      }
    }

    "s be retrieved" in {
      withTransaction { implicit s =>
        outfitDAO.create(Outfit("test", "test", "test", "test", "test", 126, "test", 1408310892)) should be(true)
        outfitDAO.findAll.length should be(2)
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
