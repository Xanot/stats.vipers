package com.vipers.indexer.dao

import java.sql.SQLException
import com.vipers.dao.DAOTest
import com.vipers.indexer.dao.slick.SlickOutfitDAOComponent
import org.scalatest.WordSpecLike

class SlickOutfitDAOTest extends WordSpecLike with DAOTest with SlickDBTest with SlickOutfitDAOComponent with Sample {
  import driver.simple._

  override protected val ddl = outfitDAO.table.ddl

  "Outfit" should {
    "be created" in {
      withTransaction { implicit s =>
        outfitDAO.create(SampleOutfits.VIPR) should be(true)
      }

      // duplicate id
      intercept[SQLException] {
        withTransaction { implicit s =>
          outfitDAO.create(SampleOutfits.VIPR)
        }
      }
    }

    "s be retrieved" in {
      withTransaction { implicit s =>
        outfitDAO.create(SampleOutfits.CHI) should be(true)
        outfitDAO.findAll.length should be(2)
      }
    }

    "be retrieved by id" in {
      withSession { implicit s =>
        outfitDAO.find(SampleOutfits.VIPR.id).get.name should be("TheVipers")
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
        outfitDAO.deleteById(SampleOutfits.VIPR.id) should be(true)
        outfitDAO.findByAliasLower("vipr") should be(None)
      }
    }
  }
}
