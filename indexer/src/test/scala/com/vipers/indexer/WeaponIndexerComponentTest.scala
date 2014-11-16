package com.vipers.indexer

import com.vipers.Test
import com.vipers.fetcher.FetcherActor.FetchAllWeaponsResponse
import com.vipers.indexer.EventBusComponent.WeaponsNeedIndexing
import com.vipers.indexer.WeaponIndexerComponentTest.{MockGameDataIndexedOnDAOComponent, MockWeaponPropsDAO, MockWeaponDAO}
import com.vipers.indexer.dao.DAOs.{GameDataIndexedOnDAOComponent, WeaponPropsDAOComponent, WeaponDAOComponent}
import com.vipers.indexer.dao.{MockDB, Sample}
import com.vipers.model.DatabaseModels.GameDataIndexedOn
import org.scalamock.scalatest.proxy.MockFactory
import org.scalatest.{Suite, WordSpecLike}

class WeaponIndexerComponentTest extends WordSpecLike with Test
  with WeaponIndexerComponent with EventBusComponent with MockWeaponDAO with MockWeaponPropsDAO
  with MockGameDataIndexedOnDAOComponent with Sample {

  private val weps = List(SampleWeapons.Corvus, SampleWeapons.NS15)
  private val wepProps = List(SampleWeaponProps.Corvus, SampleWeaponProps.NS15)

  "Weapon indexer" should {
    "index weapons" in {
      inSequence {
        weaponDAO.expects('deleteAll)(None).returning(true)
        weaponDAO.expects('createAll)(weps, None).returning(true)

        weaponPropsDAO.expects('deleteAll)(None).returning(true)
        weaponPropsDAO.expects('createAll)(wepProps, None).returning(true)

        gameDataIndexedOnDAO.expects('createOrUpdate)(*, None).returning(true)
      }

      weaponIndexer.index(FetchAllWeaponsResponse(weps, wepProps))
      weaponIndexer.beingIndexed.get() should be(false)
    }

    "index if weapons are not indexed" in {
      inSequence {
        gameDataIndexedOnDAO.expects('find)(WeaponsNeedIndexing.getClass.getSimpleName, None).returning(None)

        weaponDAO.expects('deleteAll)(None).returning(true)
        weaponDAO.expects('createAll)(weps, None).returning(true)

        weaponPropsDAO.expects('deleteAll)(None).returning(true)
        weaponPropsDAO.expects('createAll)(wepProps, None).returning(true)

        gameDataIndexedOnDAO.expects('createOrUpdate)(*, None).returning(true)
      }

      weaponIndexer.needsIndexing should be(true)
      weaponIndexer.beingIndexed.get() should be(true)
      weaponIndexer.index(FetchAllWeaponsResponse(weps, wepProps))
      weaponIndexer.beingIndexed.get() should be(false)
    }

    "return weapons if weapons are indexed and are not stale" in {
      inSequence {
        gameDataIndexedOnDAO.expects('find)(WeaponsNeedIndexing.getClass.getSimpleName, None).returning {
          Some(GameDataIndexedOn(WeaponsNeedIndexing.getClass.toString, System.currentTimeMillis()))
        }
      }

      weaponIndexer.needsIndexing should be(false)
      weaponIndexer.beingIndexed.get() should be(false)
    }
  }
}

object WeaponIndexerComponentTest extends {
  trait MockWeaponDAO extends WeaponDAOComponent with MockDB with MockFactory { this: Suite =>
    override val weaponDAO = mock[WeaponDAO]
  }

  trait MockWeaponPropsDAO extends WeaponPropsDAOComponent with MockDB with MockFactory { this: Suite =>
    override val weaponPropsDAO = mock[WeaponPropsDAO]
  }

  trait MockGameDataIndexedOnDAOComponent extends GameDataIndexedOnDAOComponent with MockDB with MockFactory { this: Suite =>
    override val gameDataIndexedOnDAO = mock[GameDataIndexedOnDAO]
  }
}
