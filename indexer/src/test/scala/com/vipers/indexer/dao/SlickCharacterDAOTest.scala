package com.vipers.indexer.dao

import java.sql.SQLException
import com.vipers.dao.DAOTest
import com.vipers.indexer.dao.slick.SlickCharacterDAOComponent
import org.scalatest.WordSpecLike

class SlickCharacterDAOTest extends WordSpecLike with DAOTest with SlickDBTest with SlickCharacterDAOComponent with Sample {
  import driver.simple._

  override protected val ddl = characterDAO.table.ddl

  "Character" should {
    "be created" in {
      withTransaction { implicit s =>
        characterDAO.create(SampleCharacters.Xanot) should be(true)

        intercept[SQLException] {
          characterDAO.create(SampleCharacters.Xanot)
        }
      }
    }

    "be retrieved given id" in {
      withSession { implicit s =>
        characterDAO.find(SampleCharacters.Xanot.id).get.name should be("Xanot")
      }
    }

    "be retrieved given name-lower" in {
      withSession { implicit s =>
        characterDAO.findByNameLower("xanot").get.name should be("Xanot")
      }
    }

    "be updated" in {
      withTransaction { implicit s =>
        characterDAO.update(SampleCharacters.Xanot.copy(name = "NewName")) should be(true)
        characterDAO.find(SampleCharacters.Xanot.id).get.name should be("NewName")
      }
    }

    "be deleted" in {
      withTransaction { implicit s =>
        characterDAO.deleteById(SampleCharacters.Xanot.id) should be(true)
        characterDAO.find(SampleCharacters.Xanot.id) should be(None)
      }
    }
  }
}
