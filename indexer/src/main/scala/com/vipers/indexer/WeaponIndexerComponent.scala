package com.vipers.indexer

import java.util.concurrent.atomic.AtomicBoolean
import com.vipers.Logging
import com.vipers.dbms.DB
import com.vipers.fetcher.FetcherActor.FetchAllWeaponsResponse
import com.vipers.indexer.dao.DAOs.{WeaponPropsDAOComponent, WeaponDAOComponent}
import com.vipers.model.DatabaseModels.Weapon

private[indexer] trait WeaponIndexerComponent extends Logging { this: DB
  with WeaponDAOComponent with WeaponPropsDAOComponent =>
  val weaponIndexer = new WeaponIndexer

  class WeaponIndexer extends Indexer {
    val weaponsBeingIndexed = new AtomicBoolean(false)

    def index(response : FetchAllWeaponsResponse) {
      try {
        withTransaction { implicit s =>
          weaponDAO.deleteAll
          weaponDAO.createAll(response.weapons:_*)

          weaponPropsDAO.deleteAll
          weaponPropsDAO.createAll(response.weaponProps:_*)

          log.debug("Weapons have been indexed")
          weaponsBeingIndexed.compareAndSet(true, false)
        }
      } catch {
        case e : Exception =>
          e.printStackTrace()
          weaponsBeingIndexed.compareAndSet(true, false)
      }
    }

    def retrieve : (Boolean, Option[List[Weapon]]) = {
      def isAlreadyBeingIndexed : Boolean = {
        if(weaponsBeingIndexed.compareAndSet(false, true)) {
          log.debug("Weapons are being indexed")
          true
        } else {
          false
        }
      }

      withSession { implicit s =>
        val weapons = weaponDAO.findAll
        if(weapons.nonEmpty) {
          val needsIndexing = if(isStale(weapons(0).lastIndexedOn, Configuration.weaponsStaleAfter)) {
            isAlreadyBeingIndexed
          } else {
            false
          }

          (needsIndexing, Some(weapons))
        } else {
          (isAlreadyBeingIndexed, None)
        }
      }
    }
  }
}
