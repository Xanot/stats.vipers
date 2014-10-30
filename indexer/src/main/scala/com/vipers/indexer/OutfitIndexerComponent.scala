package com.vipers.indexer

import com.vipers.Logging
import com.vipers.fetcher.FetcherActor.{OutfitMember, FetchOutfitResponse}
import com.vipers.indexer.EventBusComponent.{OutfitNeedsIndexing, OutfitIndexed}
import com.vipers.indexer.dao.DBComponent
import com.vipers.model.DatabaseModels.{Outfit, Character}
import org.eclipse.jetty.util.ConcurrentHashSet

private[indexer] trait OutfitIndexerComponent extends Logging { this: DBComponent with EventBusComponent =>
  val outfitIndexer : OutfitIndexer = new OutfitIndexer

  class OutfitIndexer extends Indexer {
    private val outfitsBeingIndexed = new ConcurrentHashSet[String]

    def index(response : FetchOutfitResponse) : Unit = {
      response.contents match {
        case Some((outfit, members)) =>
          try {
            withTransaction { implicit s =>
              outfitDAO.createOrUpdate(outfit)

              outfitMembershipDAO.deleteAllByOutfitId(outfit.id)

              members.foreach { case (character, membership) =>
                characterDAO.createOrUpdate(character)
                outfitMembershipDAO.createOrUpdate(membership)
              }

              log.debug(s"Outfit ${outfit.alias} has been indexed")
              outfitsBeingIndexed.remove(response.request)
              eventBus.publish(OutfitIndexed(outfit.aliasLower))
            }
          } catch {
            case e : Exception =>
              e.printStackTrace()
              outfitsBeingIndexed.remove(response.request)
          }
        case None =>
          outfitsBeingIndexed.remove(response.request)
      }
    }

    def retrieve(outfitAliasLower : String) : Option[(Outfit, Character, List[OutfitMember], Long)] = {
      def outfitResponse(outfit : Outfit)(implicit s : Session) : (Outfit, Character, List[OutfitMember], Long) = {
        (outfit, characterDAO.find(outfit.leaderCharacterId).get, outfitMembershipDAO.findAllCharactersByOutfitId(outfit.id), outfit.lastIndexedOn + Configuration.outfitStaleAfter)
      }

      def indexOutfit(aliasLower : String) : Unit = {
        if(!outfitsBeingIndexed.contains(aliasLower)) {
          log.debug(s"Outfit $aliasLower is being indexed")
          eventBus.publish(OutfitNeedsIndexing(outfitAliasLower))
          outfitsBeingIndexed.add(aliasLower)
        }
      }

      withSession { implicit s =>
        outfitDAO.findByAliasLower(outfitAliasLower).map { outfit =>
          if (isStale(outfit.lastIndexedOn, Configuration.outfitStaleAfter)) {
            indexOutfit(outfitAliasLower)
          }

          Some(outfitResponse(outfit))
        }.getOrElse {
          indexOutfit(outfitAliasLower)
          None
        }
      }
    }
  }
}