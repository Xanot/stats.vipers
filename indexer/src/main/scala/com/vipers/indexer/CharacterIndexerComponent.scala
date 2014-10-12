package com.vipers.indexer

import com.vipers.Logging
import com.vipers.fetcher.FetcherActor.FetchCharacterResponse
import com.vipers.indexer.IndexerActor.GetCharacterResponseOutfitMembership
import com.vipers.indexer.dao.DBComponent
import com.vipers.model.{Weapon, WeaponStat, Character}
import org.eclipse.jetty.util.ConcurrentHashSet

private[indexer] trait CharacterIndexerComponent extends Logging { this: DBComponent =>
  val characterIndexer : CharacterIndexer = new CharacterIndexer

  class CharacterIndexer {
    private val charactersBeingIndexed = new ConcurrentHashSet[String]

    private def isStale(lastIndexedOn : Long) : Boolean = System.currentTimeMillis() - lastIndexedOn > Configuration.characterStaleAfter

    def index(response : FetchCharacterResponse) : Option[Character] = {
      response.contents match {
        case Some((character, membership, weaponStats)) =>
          try {
            withTransaction { implicit s =>
              characterDAO.createOrUpdate(character)
              membership.map { m => outfitMembershipDAO.createOrUpdate(m) } // TODO: Handle outfit membership? leave it blank until the outfit is indexed?
              weaponStats.map { ws =>
                weaponStatDAO.deleteCharactersStats(character.id)
                weaponStatDAO.createAll(ws:_*)
              }

              log.debug(s"Character ${character.name} has been indexed")
              charactersBeingIndexed.remove(response.request)
              Some(character)
            }
          } catch {
            case e : Exception =>
              e.printStackTrace()
              charactersBeingIndexed.remove(response.request)
              None
          }
        case None =>
          charactersBeingIndexed.remove(response.request)
          None
      }
    }

    def retrieve(nameLower : String) : (Boolean, Option[(Character, Option[GetCharacterResponseOutfitMembership], Long, List[(WeaponStat, Weapon)])]) = {
      def indexChar(nameLower : String) : Boolean = {
        if(!charactersBeingIndexed.contains(nameLower)) {
          log.debug(s"Character $nameLower is being indexed")
          charactersBeingIndexed.add(nameLower)
        } else {
          false
        }
      }

      withSession { implicit s =>
        characterDAO.findByNameLower(nameLower).map { c =>
          val needsIndexing = if(isStale(c.lastIndexedOn)) {
            indexChar(nameLower)
          } else {
            val statsLastIndexedOn = weaponStatDAO.getCharactersWeaponStatsLastIndexedOn(c.id)
            if(statsLastIndexedOn.isEmpty) { // e.g. Character previously indexed without stats
              indexChar(nameLower)
            } else if(statsLastIndexedOn.isDefined && isStale(statsLastIndexedOn.get)) { // e.g. Character previously indexed but has stale stats
              indexChar(nameLower)
            } else {
              false
            }
          }

          val membership = outfitMembershipDAO.find(c.id).flatMap { m =>
            outfitDAO.find(m.outfitId).map { o =>
              GetCharacterResponseOutfitMembership(m.outfitRank, m.outfitRankOrdinal, m.outfitMemberSinceDate, o.alias, o.name)
            }
          }

          val weaponStats = weaponStatDAO.getCharactersMostRecentWeaponStats(c.id)

          (needsIndexing, Some(c, membership, c.lastIndexedOn + Configuration.characterStaleAfter, weaponStats))
        }.getOrElse {
          (indexChar(nameLower), None)
        }
      }
    }
  }
}
