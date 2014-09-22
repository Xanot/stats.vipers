package com.vipers.indexer

import com.vipers.Logging
import com.vipers.fetcher.FetcherActor.{FetchOutfitRequest, OutfitMember, FetchOutfitResponse}
import com.vipers.model.Outfit
import com.vipers.notifier.NotifierActor.OutfitIndexed
import org.eclipse.jetty.util.ConcurrentHashSet
import com.vipers.model.Character

private[indexer] trait OutfitIndexerComponent extends Logging { this: IndexerActor =>
  val outfitIndexer : OutfitIndexer = new OutfitIndexer

  class OutfitIndexer {
    private val outfitsBeingIndexed = new ConcurrentHashSet[String]

    private def isStale(lastIndexedOn : Long) : Boolean = System.currentTimeMillis() - lastIndexedOn > Configuration.outfitStaleAfter

    def index(response : FetchOutfitResponse) {
      response.contents match {
        case Some((outfit, members)) =>
          try {
            db.withTransaction { implicit s =>
              // Update or create the outfit
              if(db.outfitDAO.exists(outfit.id)) {
                db.outfitDAO.update(outfit)
              } else {
                db.outfitDAO.create(outfit)
              }

              // Remove characters and memberships, seems to be easier and more efficient than diffing
              db.characterDAO.deleteAllByOutfitId(outfit.id)
              db.outfitMembershipDAO.deleteAllByOutfitId(outfit.id)

              // create characters and memberships
              db.characterDAO.createAll(members.map(_._1):_*)
              db.outfitMembershipDAO.createAll(members.map(_._2):_*)

              log.debug(s"Outfit ${outfit.alias} has been indexed")
              notifierActor ! OutfitIndexed(outfit.aliasLower) // Notify client
              outfitsBeingIndexed.remove(response.request)
            }
          } catch {
            case e : Exception => e.printStackTrace()
          }
        case None => outfitsBeingIndexed.remove(response.request)
      }
    }

    def retrieve(outfitAlias : Option[String], outfitId : Option[String]) : Option[(Outfit, Character, List[OutfitMember])] = {
      def outfitResponse(outfit : Outfit)(implicit s : db.Session) : (Outfit, Character, List[OutfitMember]) = {
        (outfit, db.outfitDAO.findLeader(outfit.id).get, db.outfitMembershipDAO.findAllCharactersByOutfitId(outfit.id))
      }

      db.withSession { implicit s =>
        if(outfitAlias.isDefined) {
          db.outfitDAO.findByAliasLower(outfitAlias.get).map { outfit =>
            if(isStale(outfit.lastIndexedOn)) {
              fetcherActor ! FetchOutfitRequest(None, Some(outfit.id))
            }
            outfitResponse(outfit)
          }.orElse {
            if(!outfitsBeingIndexed.contains(outfitAlias.get)) {
              log.debug(s"Outfit ${outfitAlias.get} is being indexed")
              outfitsBeingIndexed.add(outfitAlias.get)
              fetcherActor ! FetchOutfitRequest(outfitAlias, None)
            }
            None
          }
        } else if(outfitId.isDefined) {
          db.outfitDAO.find(outfitId.get).map { outfit =>
            if(isStale(outfit.lastIndexedOn)) {
              fetcherActor ! FetchOutfitRequest(None, Some(outfit.id))
            }
            outfitResponse(outfit)
          }.orElse {
            if(!outfitsBeingIndexed.contains(outfitId.get)) {
              log.debug(s"Outfit ${outfitId.get} is being indexed")
              outfitsBeingIndexed.add(outfitId.get)
              fetcherActor ! FetchOutfitRequest(None, outfitId)
            }
            None
          }
        } else { None }
      }
    }
  }
}