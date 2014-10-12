package com.vipers.indexer

import java.util.concurrent.atomic.AtomicBoolean
import com.vipers.Logging
import com.vipers.fetcher.FetcherActor.FetchAllWeaponsResponse
import com.vipers.indexer.dao.DBComponent
import com.vipers.model.Weapon

private[indexer] trait WeaponIndexerComponent extends Logging { this: DBComponent =>
  val weaponIndexer = new WeaponIndexer

  class WeaponIndexer {
    private val weaponsBeingIndexed = new AtomicBoolean(false)

    private def isStale(lastIndexedOn : Long) : Boolean = System.currentTimeMillis() - lastIndexedOn > Configuration.weaponsStaleAfter

    def index(response : FetchAllWeaponsResponse) {
      try {
        withTransaction { implicit s =>
          weaponDAO.deleteAll
          weaponDAO.createAll(response.weapons: _*)
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
          val needsIndexing = if(isStale(weapons(0).lastIndexedOn)) {
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
