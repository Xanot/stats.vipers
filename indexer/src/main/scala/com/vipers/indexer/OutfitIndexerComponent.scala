package com.vipers.indexer

import com.vipers.Logging
import com.vipers.fetcher.FetcherActor.{OutfitMember, FetchOutfitResponse}
import com.vipers.indexer.dao.DBComponent
import com.vipers.model.Outfit
import org.eclipse.jetty.util.ConcurrentHashSet
import com.vipers.model.Character

private[indexer] trait OutfitIndexerComponent extends Logging { this: DBComponent =>
  val outfitIndexer : OutfitIndexer = new OutfitIndexer

  class OutfitIndexer {
    private val outfitsBeingIndexed = new ConcurrentHashSet[String]

    private def isStale(lastIndexedOn : Long) : Boolean = System.currentTimeMillis() - lastIndexedOn > Configuration.outfitStaleAfter

    def index(response : FetchOutfitResponse) : Option[Outfit] = {
      response.contents match {
        case Some((outfit, members)) =>
          withTransaction { implicit s =>
            outfitDAO.createOrUpdate(outfit)

            // Remove characters and memberships, seems to be easier and more efficient than diffing
            characterDAO.deleteAllByOutfitId(outfit.id)
            outfitMembershipDAO.deleteAllByOutfitId(outfit.id)

            // create characters and memberships
            characterDAO.createAll(members.map(_._1):_*)
            outfitMembershipDAO.createAll(members.map(_._2):_*)

            log.debug(s"Outfit ${outfit.alias} has been indexed")
            outfitsBeingIndexed.remove(response.request)
            Some(outfit)
          }
        case None =>
          outfitsBeingIndexed.remove(response.request)
          None
      }
    }

    def retrieve(outfitAlias : Option[String], outfitId : Option[String]) : (Boolean, Option[(Outfit, Character, List[OutfitMember], Long)]) = {
      def outfitResponse(outfit : Outfit)(implicit s : Session) : (Outfit, Character, List[OutfitMember], Long) = {
        (outfit, characterDAO.find(outfit.leaderCharacterId).get, outfitMembershipDAO.findAllCharactersByOutfitId(outfit.id), outfit.lastIndexedOn + Configuration.outfitStaleAfter)
      }

      def indexOutfit(aliasOrId : String) : Boolean = {
        if(!outfitsBeingIndexed.contains(aliasOrId)) {
          log.debug(s"Outfit $aliasOrId is being indexed")
          outfitsBeingIndexed.add(aliasOrId)
        } else {
          false
        }
      }

      withSession { implicit s =>
        if(outfitAlias.isDefined) {
          outfitDAO.findByAliasLower(outfitAlias.get).map { outfit =>
            val needsIndexing = if (isStale(outfit.lastIndexedOn)) {
              indexOutfit(outfitAlias.get)
            } else {
              false
            }

            (needsIndexing, Some(outfitResponse(outfit)))
          }.getOrElse {
            (indexOutfit(outfitAlias.get), None)
          }
        } else if(outfitId.isDefined) {
          outfitDAO.find(outfitId.get).map { outfit =>
            val needsIndexing = if (isStale(outfit.lastIndexedOn)) {
              indexOutfit(outfitId.get)
            } else {
              false
            }

            (needsIndexing, Some(outfitResponse(outfit)))
          }.getOrElse {
            (indexOutfit(outfitId.get), None)
          }
        } else { (false, None) }
      }
    }
  }
}