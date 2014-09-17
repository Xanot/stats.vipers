package com.vipers.indexer.dao

import java.sql.SQLException
import com.vipers.dao.DAOTest
import com.vipers.indexer.dao.slick.{SlickOutfitDAOComponent, SlickCharacterDAOComponent, SlickOutfitMembershipDAOComponent}
import com.vipers.model.{OutfitMembership, Outfit, Character}
import org.scalatest.WordSpecLike

class SlickOutfitMembershipDAOTest extends WordSpecLike with DAOTest with SlickDBTest
  with SlickOutfitMembershipDAOComponent with SlickCharacterDAOComponent with SlickOutfitDAOComponent {
  import driver.simple._

  override def beforeAll(): Unit = {
    withTransaction { implicit s =>
      (characterDAO.table.ddl ++ outfitDAO.table.ddl ++ outfitMembershipDAO.table.ddl).create
    }
  }

  override def afterAll(): Unit = {
    withTransaction { implicit s =>
      (characterDAO.table.ddl ++ outfitDAO.table.ddl ++ outfitMembershipDAO.table.ddl).drop
    }
  }

  "Outfit members" should {
    "be created" in {
      withTransaction { implicit s =>
        val char = Character(
          "Test",
          "test",
          "id",
          90,
          20,
          900,
          800,
          80,
          9000,
          "VS",
          System.currentTimeMillis(),
          System.currentTimeMillis(),
          System.currentTimeMillis(),
          100,
          15000
        )

        characterDAO.create(char) should be(true)
        outfitDAO.create(Outfit("TheVipers", "thevipers", "VIPR", "vipr", "id", 126, "VS", "37523756405021402", 1408310892)) should be(true)
        outfitMembershipDAO.create(OutfitMembership("37523756405021402", "id", "outfit-rank", 2, System.currentTimeMillis())) should be(true)

        // Same character cannot be in another outfit
        intercept[SQLException] {
          outfitMembershipDAO.create(OutfitMembership("37523756405021402", "id", "outfit-rank", 2, System.currentTimeMillis()))
        }
      }
    }

    "be retrieved given outfit id" in {
      withSession { implicit s =>
        outfitMembershipDAO.findAllCharactersByOutfitId("37523756405021402") match {
          case member :: Nil => member._1.name should be("Test")
        }
      }
    }

    "be updated in" in {
      withTransaction { implicit s =>
        outfitDAO.create(Outfit("test", "test", "tt", "tt", "id", 0, "VS", "outfitId", System.currentTimeMillis())) should be(true)

        val m = OutfitMembership("37523756405021402", "id", "outfit-rank", 2, System.currentTimeMillis())
        outfitMembershipDAO.update(m.copy(outfitId = "outfitId")) should be(true)
        outfitMembershipDAO.find("id").get.outfitId should be("outfitId")

        outfitMembershipDAO.findAllCharactersByOutfitId("outfitId") match {
          case member :: Nil => member._1.name should be("Test")
        }

        // invalid outfit
        intercept[SQLException] {
          outfitMembershipDAO.update(m.copy(outfitId = "newOutfit"))
        }
      }
    }

    "be removed" in {
      withTransaction { implicit s =>
        outfitMembershipDAO.deleteById("id") should be(true)
        outfitMembershipDAO.findAll.length should be(0)
      }
    }
  }
}
