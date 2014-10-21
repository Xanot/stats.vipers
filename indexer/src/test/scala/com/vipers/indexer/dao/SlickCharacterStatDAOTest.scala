package com.vipers.indexer.dao

import com.vipers.dao.DAOTest
import com.vipers.indexer.dao.slick.SlickCharacterStatDAOComponent
import org.scalatest.WordSpecLike

class SlickCharacterStatDAOTest extends WordSpecLike with DAOTest with SlickDBTest with SlickCharacterStatDAOComponent with Sample {
  import driver.simple._

  override val ddl = characterStatDAO.table.ddl

  "Character's profile stats" should {
    "be created or updated" in {
      withTransaction { implicit s =>
        characterStatDAO.createAll(SampleCharacterProfileStat.HeavyAssault, SampleCharacterProfileStat.CombatMedic)
        characterStatDAO.createOrUpdate(SampleCharacterProfileStat.HeavyAssault.copy(score = 9000))
      }
    }

    "be retrieved" in {
      withSession { implicit s =>
        characterStatDAO.getCharactersProfileStats(SampleCharacters.Xanot.id) match {
          case m :: ha :: Nil =>
            ha.score should be(9000)
            m.playTime should be(1243963)
          case _ => throw new Error
        }
      }
    }
  }
}
