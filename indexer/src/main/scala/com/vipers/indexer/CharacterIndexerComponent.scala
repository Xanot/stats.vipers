package com.vipers.indexer

import com.vipers.Logging
import com.vipers.fetcher.FetcherActor.FetchCharacterResponse
import com.vipers.indexer.EventBusComponent.{CharacterIndexed, CharacterNeedsIndexing}
import com.vipers.indexer.IndexerActor.GetCharacterResponseOutfitMembership
import com.vipers.indexer.dao.DBComponent
import com.vipers.model.{Weapon, WeaponStat, Character}
import org.eclipse.jetty.util.ConcurrentHashSet

private[indexer] trait CharacterIndexerComponent extends Logging { this: DBComponent with EventBusComponent =>
  val characterIndexer = new CharacterIndexer

  class CharacterIndexer extends Indexer {
    private val charactersBeingIndexed = new ConcurrentHashSet[String]

    def index(response : FetchCharacterResponse) : Unit = {
      response.contents match {
        case Some((character, membership, weaponStats, profileStats, firstStatIndex)) =>
          try {
            withTransaction { implicit s =>
              characterDAO.createOrUpdate(character)
              membership.map { m => outfitMembershipDAO.createOrUpdate(m) } // TODO: Process outfit? leave it blank until the outfit is indexed?

              if(response.request._2) { // If stats were requested
                weaponStatDAO.createOrUpdateLastIndexedOn(character.id, System.currentTimeMillis())

                weaponStats.map { ws =>
                  if(firstStatIndex) {
                    weaponStatDAO.createAll(ws:_*)
                    weaponStatDAO.insertTimeSeries(ws:_*)
                  } else {
                    weaponStatDAO.insertTimeSeries(correctStats(ws):_*)
                  }
                }
                profileStats.map { ps =>
                  if(firstStatIndex) {
                    characterStatDAO.createAll(ps:_*)
                  } else {
                    ps.foreach { stat =>
                      characterStatDAO.createOrUpdate(stat)
                    }
                  }
                }
              }

              log.debug(s"Character ${character.name} has been indexed")
              charactersBeingIndexed.remove(response.request._1)
              eventBus.publish(CharacterIndexed(character.nameLower))
            }
          } catch {
            case e : Exception =>
              e.printStackTrace()
              charactersBeingIndexed.remove(response.request._1)
          }
        case None =>
          charactersBeingIndexed.remove(response.request._1)
      }
    }

    def retrieve(nameLower : String) : Option[(Character, Option[GetCharacterResponseOutfitMembership], Long, List[(WeaponStat, Weapon)])] = {
      def indexChar(nameLower : String, statsLastSavedOn : Option[Long]) : Unit = {
        if(!charactersBeingIndexed.contains(nameLower)) {
          log.debug(s"Character $nameLower is being indexed")
          eventBus.publish(CharacterNeedsIndexing(nameLower, statsLastSavedOn))
          charactersBeingIndexed.add(nameLower)
        }
      }

      withSession { implicit s =>
        characterDAO.findByNameLower(nameLower).map { c =>
          val weaponStatsLastSavedOn = weaponStatDAO.getCharactersWeaponStatsLastSavedOn(c.id)

          if(isStale(c.lastIndexedOn, Configuration.characterStaleAfter)) {
            indexChar(nameLower, weaponStatsLastSavedOn)
          } else {
            val statsLastIndexedOn = weaponStatDAO.getCharactersWeaponStatsLastIndexedOn(c.id)
            if(statsLastIndexedOn.isEmpty) { // e.g. Character previously indexed without stats
              indexChar(nameLower, None)
            } else if(statsLastIndexedOn.isDefined && isStale(statsLastIndexedOn.get, Configuration.characterStaleAfter)) { // e.g. Character previously indexed but has stale stats
              indexChar(nameLower, weaponStatsLastSavedOn)
            }
          }

          val membership = outfitMembershipDAO.find(c.id).flatMap { m =>
            outfitDAO.find(m.outfitId).map { o =>
              GetCharacterResponseOutfitMembership(m.outfitRank, m.outfitRankOrdinal, m.outfitMemberSinceDate, o.alias, o.name)
            }
          }

          val weaponStats = weaponStatDAO.getCharactersMostRecentWeaponStats(c.id)

          Some(c, membership, c.lastIndexedOn + Configuration.characterStaleAfter, weaponStats)
        }.getOrElse {
          indexChar(nameLower, None)
          None
        }
      }
    }

    private def correctStats(stats : Seq[WeaponStat])(implicit s : Session) : Seq[WeaponStat] = {
      stats.map { stat =>
        val corrected : WeaponStat = if(stat.deathCount == 0 || stat.fireCount == 0 || stat.headshotCount == 0 || stat.hitCount == 0 || stat.killCount == 0 || stat.score == 0) {
          weaponStatDAO.getCharactersMostRecentWeaponStat(stat.characterId, stat.itemId).map { p =>
            var deathCount = stat.deathCount
            if(deathCount == 0) {
              deathCount = p.deathCount
            }

            var fireCount = stat.fireCount
            if(fireCount == 0) {
              fireCount = p.fireCount
            }

            var headshotCount = stat.headshotCount
            if(headshotCount == 0) {
              headshotCount = p.headshotCount
            }

            var hitCount = stat.hitCount
            if(hitCount == 0) {
              hitCount = p.hitCount
            }

            var killCount = stat.killCount
            if(killCount == 0) {
              killCount = p.killCount
            }

            var score = stat.score
            if(score == 0) {
              score = p.score
            }
            stat.copy(deathCount = deathCount, fireCount = fireCount, headshotCount = headshotCount, hitCount = hitCount, killCount = killCount, score = score)
          }.getOrElse(stat)
        } else {
          stat
        }

        weaponStatDAO.createOrUpdate(corrected)
        corrected
      }
    }
  }
}
