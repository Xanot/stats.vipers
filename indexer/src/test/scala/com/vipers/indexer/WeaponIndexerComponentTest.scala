package com.vipers.indexer

import com.vipers.Test
import com.vipers.dbms.DB
import com.vipers.fetcher.FetcherActor.FetchAllWeaponsResponse
import com.vipers.indexer.WeaponIndexerComponentTest.{MockWeaponPropsDAO, MockWeaponDAO}
import com.vipers.indexer.dao.DAOs.{WeaponPropsDAOComponent, WeaponDAOComponent}
import com.vipers.indexer.dao.Sample
import org.scalamock.scalatest.proxy.MockFactory
import org.scalatest.{Suite, WordSpecLike}

class WeaponIndexerComponentTest extends WordSpecLike with Test
  with WeaponIndexerComponent with MockWeaponDAO with MockWeaponPropsDAO with Sample {

  private val weps = List(
    SampleWeapons.Corvus.copy(lastIndexedOn = System.currentTimeMillis()),
    SampleWeapons.NS15.copy(lastIndexedOn = System.currentTimeMillis())
  )

  private val wepProps = List(SampleWeaponProps.Corvus, SampleWeaponProps.NS15)

  "Weapon indexer" should {
    "index weapons" in {
      inSequence {
        weaponDAO.expects('deleteAll)(None).returning(true)
        weaponDAO.expects('createAll)(weps, None).returning(true)

        weaponPropsDAO.expects('deleteAll)(None).returning(true)
        weaponPropsDAO.expects('createAll)(wepProps, None).returning(true)
      }

      weaponIndexer.index(FetchAllWeaponsResponse(weps, wepProps))
      weaponIndexer.weaponsBeingIndexed.get() should be(false)
    }

    "index if weapons are not indexed" in {
      inSequence {
        weaponDAO.expects('findAll)(None).returning(List.empty)
        weaponDAO.expects('deleteAll)(None).returning(true)
        weaponDAO.expects('createAll)(weps, None).returning(true)

        weaponPropsDAO.expects('deleteAll)(None).returning(true)
        weaponPropsDAO.expects('createAll)(wepProps, None).returning(true)
      }

      weaponIndexer.retrieve match {
        case (needsIndexing, weapons) =>
          needsIndexing should be(true)
          weapons should be(None)
      }

      weaponIndexer.weaponsBeingIndexed.get() should be(true)
      weaponIndexer.index(FetchAllWeaponsResponse(weps, wepProps))
      weaponIndexer.weaponsBeingIndexed.get() should be(false)
    }

    "return weapons if weapons are indexed and are not stale" in {
      inSequence {
        weaponDAO.expects('findAll)(None).returning(weps)
      }

      weaponIndexer.retrieve match {
        case (needsIndexing, weapons) =>
          needsIndexing should be(false)
          weapons should be(Some(weps))
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

  trait MockWeaponPropsDAO extends WeaponPropsDAOComponent with MockDB with MockFactory { this: Suite =>
    override val weaponPropsDAO = mock[WeaponPropsDAO]
  }
}
