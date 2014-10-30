package com.vipers.indexer.dao

import com.vipers.dao.DAOTest
import com.vipers.indexer.dao.slick.{SlickWeaponDAOComponent, SlickWeaponStatDAOComponent}
import com.vipers.model.DatabaseModels.WeaponStat
import org.scalatest.WordSpecLike

class SlickWeaponStatDAOTest extends WordSpecLike with DAOTest with SlickDBTest with Sample
  with SlickWeaponStatDAOComponent with SlickWeaponDAOComponent{
  import driver.simple._

  override protected val ddl = weaponDAO.table.ddl ++
    weaponStatDAO.weaponStatsTable.ddl ++
    weaponStatDAO.weaponStatsTimeSeriesTable.ddl ++
    weaponStatDAO.weaponStatsIndexed.ddl

  "Weapon stats" should {
    "be created or updated" in {
      withTransaction { implicit s =>
        weaponStatDAO.createAll(SampleCharacterWeaponStat.Corvus, SampleCharacterWeaponStat.NS15)
        weaponStatDAO.createOrUpdate(SampleCharacterWeaponStat.Corvus.copy(headshotCount = 9000))
      }
    }

    "be retrieved" in {
      withSession { implicit s =>
        weaponDAO.create(SampleWeapons.Corvus) should be(true)
        weaponDAO.create(SampleWeapons.NS15) should be(true)

        weaponStatDAO.getCharactersMostRecentWeaponStats(SampleCharacters.Xanot.id) match {
          case corvus :: ns :: Nil =>
            ns._2.name should be("NS-15M")
            corvus._1.headshotCount should be(9000)
          case _ => throw new Error
        }
      }
    }

    "max last saved on" in {
      withTransaction { implicit s =>
        weaponStatDAO.getCharactersWeaponStatsLastSavedOn(SampleCharacters.Xanot.id).get should be(1409415911L)
        weaponStatDAO.createOrUpdate(SampleCharacterWeaponStat.Corvus.copy(lastSaveDate = 1409415912L))
        weaponStatDAO.getCharactersWeaponStatsLastSavedOn(SampleCharacters.Xanot.id).get should be(1409415912L)
      }
    }
  }

  "Most recent weapon stat" should {
    "be retrieved" in {
      withSession { implicit s =>
        weaponStatDAO.getCharactersMostRecentWeaponStat(SampleCharacters.Xanot.id, SampleWeapons.Corvus.id).get.lastSaveDate should be(1409415912L)
      }
    }
  }

  "Weapon stat time series" should {
    "be created" in {
      withTransaction { implicit s =>
        weaponStatDAO.insertTimeSeries(SampleCharacterWeaponStat.Corvus, SampleCharacterWeaponStat.NS15)
        weaponStatDAO.insertTimeSeries(SampleCharacterWeaponStat.NS15.copy(lastSaveDate = 1409415912L))
      }
    }

    "be retrieved" in {
      withSession { implicit s =>
        weaponStatDAO.getCharactersWeaponStatHistory(SampleCharacters.Xanot.id, SampleWeapons.Corvus.id) match {
          case t1 :: Nil =>
            t1 should be(SampleCharacterWeaponStat.Corvus)
          case _ => throw new Error
        }

        weaponStatDAO.getCharactersWeaponStatHistory(SampleCharacters.Xanot.id, SampleWeapons.NS15.id) match {
          case t1 :: t2 :: Nil =>
            t1 should be(SampleCharacterWeaponStat.NS15)
            t2 should be(SampleCharacterWeaponStat.NS15.copy(lastSaveDate = 1409415912L))
          case _ => throw new Error
        }
      }
    }
  }

  "Weapon stats last indexed on" should {
    val stamp = System.currentTimeMillis()

    "be created or updated" in {
      withTransaction { implicit s =>
        weaponStatDAO.createOrUpdateLastIndexedOn(SampleCharacters.Xanot.id, stamp)
      }
    }

    "be retrieved" in {
      withSession { implicit s =>
        weaponStatDAO.getCharactersWeaponStatsLastIndexedOn(SampleCharacters.Xanot.id).get should be(stamp)
      }
    }
  }
}
