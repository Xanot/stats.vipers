package com.vipers.indexer

import com.vipers.Test
import com.vipers.dbms.DB
import com.vipers.fetcher.FetcherActor.FetchAllWeaponsResponse
import com.vipers.indexer.WeaponIndexerComponentTest.MockWeaponDAO
import com.vipers.indexer.dao.DAOs.WeaponDAOComponent
import com.vipers.indexer.dao.Sample
import org.scalamock.scalatest.proxy.MockFactory
import org.scalatest.{Suite, WordSpecLike}

class WeaponIndexerComponentTest extends WordSpecLike with Test
  with WeaponIndexerComponent with MockWeaponDAO with Sample {

  private val weps = List(
    (SampleWeapons.Corvus.copy(lastIndexedOn = System.currentTimeMillis()), Some(SampleItemProfiles.Corvus)),
    (SampleWeapons.NS15.copy(lastIndexedOn = System.currentTimeMillis()), Some(SampleItemProfiles.NS15))
  )

  "Weapon indexer" should {
    "index weapons" in {
      inSequence {
        weaponDAO.expects('deleteAll)(None).returning(true)
        weaponDAO.expects('deleteItemProfiles)(None).returning(true)
        weaponDAO.expects('createAll)(weps.map(_._1), None).returning(true)
        weaponDAO.expects('createItemProfiles)(weps.flatMap(_._2), None).returning(true)
      }

      weaponIndexer.index(FetchAllWeaponsResponse(weps))
      weaponIndexer.weaponsBeingIndexed.get() should be(false)
    }

    "index if weapons are not indexed" in {
      inSequence {
        weaponDAO.expects('findAll)(None).returning(List.empty)
        weaponDAO.expects('deleteAll)(None).returning(true)
        weaponDAO.expects('deleteItemProfiles)(None).returning(true)
        weaponDAO.expects('createAll)(weps.map(_._1), None).returning(true)
        weaponDAO.expects('createItemProfiles)(weps.flatMap(_._2), None).returning(true)
      }

      weaponIndexer.retrieve match {
        case (needsIndexing, weapons) =>
          needsIndexing should be(true)
          weapons should be(None)
      }

      weaponIndexer.weaponsBeingIndexed.get() should be(true)
      weaponIndexer.index(FetchAllWeaponsResponse(weps))
      weaponIndexer.weaponsBeingIndexed.get() should be(false)
    }

    "return weapons if weapons are indexed and are not stale" in {
      inSequence {
        weaponDAO.expects('findAll)(None).returning(weps.map(_._1))
      }

      weaponIndexer.retrieve match {
        case (needsIndexing, weapons) =>
          needsIndexing should be(false)
          weapons should be(Some(weps.map(_._1)))
      }

      weaponIndexer.weaponsBeingIndexed.get() should be(false)
    }
  }
}

object WeaponIndexerComponentTest extends {
  trait MockDB extends DB {
    override type Session = Option[AnyRef]
    override def withSession[T](f: (Session) => T): T = f(None)
    override def withTransaction[T](f: (Session) => T): T = f(None)
  }

  trait MockWeaponDAO extends WeaponDAOComponent with MockDB with MockFactory { this: Suite =>
    override val weaponDAO = mock[WeaponDAO]
  }
}
