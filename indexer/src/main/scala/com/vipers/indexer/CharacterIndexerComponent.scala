package com.vipers.indexer

import com.vipers.Logging
import com.vipers.fetcher.FetcherActor.{FetchCharacterRequest, FetchCharacterResponse}
import com.vipers.indexer.IndexerActor.GetCharacterResponseOutfitMembership
import com.vipers.model.{Character, OutfitMembership}
import com.vipers.notifier.NotifierActor.CharacterIndexed
import org.eclipse.jetty.util.ConcurrentHashSet

private[indexer] trait CharacterIndexerComponent extends Logging { this: IndexerActor =>
  val characterIndexer : CharacterIndexer = new CharacterIndexer

  class CharacterIndexer {
    private val charactersBeingIndexed = new ConcurrentHashSet[String]

    private def isStale(lastIndexedOn : Long) : Boolean = System.currentTimeMillis() - lastIndexedOn > Configuration.characterStaleAfter

    def index(response : FetchCharacterResponse) {
      response.character match {
        case Some(character) =>
          try {
            db.withTransaction { implicit s =>
              // Index
              if(db.characterDAO.exists(character.id)) {
                db.characterDAO.update(character)
              } else {
                db.characterDAO.create(character)
              }

              // TODO: Handle outfit membership? leave it blank until the outfit is indexed?

              charactersBeingIndexed.remove(response.request)
              notifierActor ! CharacterIndexed(character.nameLower)// Notify client
              log.debug(s"Character ${character.name} has been indexed")
            }
          } catch {
            case e : Exception => e.printStackTrace()
          }
        case None => charactersBeingIndexed.remove(response.request)
      }
    }

    def retrieve(nameLower : String) : Option[(Character, Option[GetCharacterResponseOutfitMembership], Long)] = {
      def indexChar(nameLower : String) {
        if(!charactersBeingIndexed.contains(nameLower)) {
          log.debug(s"Character $nameLower is being indexed")
          charactersBeingIndexed.add(nameLower)
          fetcherActor ! FetchCharacterRequest(Some(nameLower), None)
        }
      }

      db.withSession { implicit s =>
        db.characterDAO.findByNameLower(nameLower).map { c =>
          if(isStale(c.lastIndexedOn)) {
            indexChar(nameLower)
          }

          val membership = db.outfitMembershipDAO.find(c.id).flatMap { m =>
            db.outfitDAO.find(m.outfitId).map { o =>
              GetCharacterResponseOutfitMembership(m.outfitRank, m.outfitRankOrdinal, m.outfitMemberSinceDate, o.alias, o.name)
            }
          }

          (c, membership, c.lastIndexedOn + Configuration.characterStaleAfter)
        }.orElse {
          indexChar(nameLower)
          None
        }
      }
    }
  }
}
