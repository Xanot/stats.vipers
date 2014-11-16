package com.vipers.indexer

import com.vipers.dbms.DB
import com.vipers.fetcher.FetcherActor.FetchAllWeaponsResponse
import com.vipers.indexer.EventBusComponent.WeaponsNeedIndexing
import com.vipers.indexer.dao.DAOs.{GameDataIndexedOnDAOComponent, WeaponPropsDAOComponent, WeaponDAOComponent}

private[indexer] trait WeaponIndexerComponent extends GameDataIndexerComponent {
  this: DB
    with WeaponDAOComponent
    with WeaponPropsDAOComponent
    with EventBusComponent
    with GameDataIndexedOnDAOComponent =>

  val weaponIndexer = new WeaponIndexer

  class WeaponIndexer extends GameDataIndexer[FetchAllWeaponsResponse] {
    override protected val staleAfter: Long = Configuration.weaponsStaleAfter
    override protected val needsIndexingEvent = WeaponsNeedIndexing

    override protected def indexImpl(response: FetchAllWeaponsResponse)(implicit s: Session): Unit = {
      weaponDAO.deleteAll
      weaponDAO.createAll(response.weapons:_*)

      weaponPropsDAO.deleteAll
      weaponPropsDAO.createAll(response.weaponProps:_*)
    }
  }
}
