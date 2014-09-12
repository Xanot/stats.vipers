package com.vipers.routes

import akka.actor.{Actor, Props}
import akka.pattern.ask
import com.vipers.fetcher.FetcherActor
import com.vipers.fetcher.model._
import com.vipers.JsonRoute
import spray.httpx.encoding.Gzip
import scala.util.{Failure, Success}
import spray.http.StatusCodes._

trait OutfitApi extends JsonRoute { this: Actor =>
  import context.dispatcher

  private val fetcherActor = context.actorOf(Props(classOf[FetcherActor]))

  protected lazy val outfitRoute = {
    pathPrefix("outfit") {
      pathEnd {
        get {
          encodeResponse(Gzip) {
            onComplete((fetcherActor ? FetchMultipleOutfitsRequest((Sort.CREATION_DATE, Sort.ASC), Page.FirstTen)).mapTo[List[Outfit]]) {
              case Success(outfits) => complete(outfits)
              case Failure(m) => complete(InternalServerError, m.toString)
            }
          }
        }
      } ~
      path(Segment) { alias =>
        get {
          encodeResponse(Gzip) {
            onComplete(
              (fetcherActor ? FetchOutfitRequest(Some(alias), None,
                Some(EnrichOutfit(withLeaderCharacter = Some(EnrichCharacter(withFaction = true)), Some(EnrichCharacter())))
              )).mapTo[Option[Outfit]]) {
              case Success(outfit) => complete(outfit)
              case Failure(m) => complete(InternalServerError, m.toString)
            }
          }
        }
      }
    }
  }
}
