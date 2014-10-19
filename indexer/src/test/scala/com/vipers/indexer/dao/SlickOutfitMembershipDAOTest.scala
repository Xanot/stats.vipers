package com.vipers.indexer.dao

import java.sql.SQLException
import com.vipers.dao.DAOTest
import com.vipers.indexer.dao.slick.{SlickCharacterDAOComponent, SlickOutfitMembershipDAOComponent}
import org.scalatest.WordSpecLike

class SlickOutfitMembershipDAOTest extends WordSpecLike with DAOTest with SlickDBTest with Sample
  with SlickOutfitMembershipDAOComponent with SlickCharacterDAOComponent {
  import driver.simple._

  override protected val ddl = characterDAO.table.ddl ++ outfitMembershipDAO.table.ddl

  "Outfit members" should {
    "be created" in {
      withTransaction { implicit s =>
        characterDAO.create(SampleCharacters.Xanot) should be(true)
        outfitMembershipDAO.create(SampleOutfitMemberships.Xanot) should be(true)

        // Same character cannot be in another outfit
        intercept[SQLException] {
          outfitMembershipDAO.create(SampleOutfitMemberships.Xanot)
        }
      }
    }

    "be retrieved given outfit id" in {
      withSession { implicit s =>
        outfitMembershipDAO.findAllCharactersByOutfitId(SampleOutfits.VIPR.id) match {
          case member :: Nil => member._1.name should be("Xanot")
        }
      }
    }

    "be updated in" in {
      withTransaction { implicit s =>
        outfitMembershipDAO.update(SampleOutfitMemberships.Xanot.copy(outfitId = "outfitId")) should be(true)
        outfitMembershipDAO.find(SampleCharacters.Xanot.id).get.outfitId should be("outfitId")

        outfitMembershipDAO.findAllCharactersByOutfitId("outfitId") match {
          case member :: Nil => member._1.name should be("Xanot")
        }
      }
    }

    "be removed" in {
      withTransaction { implicit s =>
        outfitMembershipDAO.deleteAllByOutfitId("outfitId") should be(true)
        outfitMembershipDAO.findAll.length should be(0)
      }
    }
  }
}
