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
        case Some((character, membership, weaponStats, profileStats)) =>
          try {
            withTransaction { implicit s =>
              characterDAO.createOrUpdate(character)
              membership.map { m => outfitMembershipDAO.createOrUpdate(m) } // TODO: Handle outfit membership? leave it blank until the outfit is indexed?
              weaponStats.map { ws =>
                weaponStatDAO.deleteCharactersStats(character.id)
                weaponStatDAO.createAll(ws:_*)
              }
              profileStats.map { ps =>
                characterStatDAO.deleteCharactersStats(character.id)
                characterStatDAO.createAll(ps:_*)
              }

              log.debug(s"Character ${character.name} has been indexed")
              charactersBeingIndexed.remove(response.request)
              eventBus.publish(CharacterIndexed(character.nameLower))
            }
          } catch {
            case e : Exception =>
              e.printStackTrace()
              charactersBeingIndexed.remove(response.request)
          }
        case None =>
          charactersBeingIndexed.remove(response.request)
      }
    }

    def retrieve(nameLower : String) : Option[(Character, Option[GetCharacterResponseOutfitMembership], Long, List[(WeaponStat, Weapon)])] = {
      def indexChar(nameLower : String) : Unit = {
        if(!charactersBeingIndexed.contains(nameLower)) {
          log.debug(s"Character $nameLower is being indexed")
          eventBus.publish(CharacterNeedsIndexing(nameLower))
          charactersBeingIndexed.add(nameLower)
        }
      }

      withSession { implicit s =>
        characterDAO.findByNameLower(nameLower).map { c =>
          if(isStale(c.lastIndexedOn, Configuration.characterStaleAfter)) {
            indexChar(nameLower)
          } else {
            val statsLastIndexedOn = weaponStatDAO.getCharactersWeaponStatsLastIndexedOn(c.id)
            if(statsLastIndexedOn.isEmpty) { // e.g. Character previously indexed without stats
              indexChar(nameLower)
            } else if(statsLastIndexedOn.isDefined && isStale(statsLastIndexedOn.get, Configuration.characterStaleAfter)) { // e.g. Character previously indexed but has stale stats
              indexChar(nameLower)
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
          indexChar(nameLower)
          None
        }
      }
    }
  }
}
