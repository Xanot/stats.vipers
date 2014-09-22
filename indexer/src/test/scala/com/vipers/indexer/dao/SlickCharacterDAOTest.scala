package com.vipers.indexer.dao

import com.vipers.dao.DAOTest
import com.vipers.indexer.dao.slick.{SlickOutfitDAOComponent, SlickOutfitMembershipDAOComponent, SlickCharacterDAOComponent}
import com.vipers.model.Character
import org.scalatest.WordSpecLike

class SlickCharacterDAOTest extends WordSpecLike with DAOTest with SlickDBTest
  with SlickCharacterDAOComponent with SlickOutfitMembershipDAOComponent with SlickOutfitDAOComponent {
  import driver.simple._

  override def beforeAll(): Unit = {
    withTransaction { implicit s =>
      characterDAO.table.ddl.create
    }
  }

  override def afterAll(): Unit = {
    withTransaction { implicit s =>
      characterDAO.table.ddl.drop
    }
  }

  "Character" should {
    "be created" in {
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
        1,
        System.currentTimeMillis(),
        System.currentTimeMillis(),
        System.currentTimeMillis(),
        100,
        15000,
        System.currentTimeMillis()
      )

      withTransaction { implicit s =>
        characterDAO.create(char) should be(true)
      }
    }

    "be retrieved given id" in {
      withSession { implicit s =>
        characterDAO.find("id").get.name should be("Test")
      }
    }

    "be retrieved given name-lower" in {
      withSession { implicit s =>
        characterDAO.findByNameLower("test").get.name should be("Test")
      }
    }

    "be updated" in {
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
          1,
          System.currentTimeMillis(),
          System.currentTimeMillis(),
          System.currentTimeMillis(),
          100,
          15000,
          System.currentTimeMillis()
        )
        characterDAO.update(char.copy(name = "NewName")) should be(true)
        characterDAO.find("id").get.name should be("NewName")
      }
    }

    "be deleted" in {
      withTransaction { implicit s =>
        characterDAO.deleteById("id") should be(true)
        characterDAO.find("id") should be(None)
      }
    }
  }
}
