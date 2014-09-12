package com.vipers.routes

import akka.pattern.ask
import com.vipers.fetcher.model._
import com.vipers.{ApiActor, JsonRoute}
import spray.httpx.encoding.Gzip
import scala.util.{Failure, Success}
import spray.http.StatusCodes._

trait OutfitApi extends JsonRoute { this: ApiActor =>
  import context.dispatcher

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
