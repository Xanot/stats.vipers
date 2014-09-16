package com.vipers.routes

import akka.pattern.ask
import com.vipers.indexer.IndexerActor._
import com.vipers.model.Outfit
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
          parameter("aliasLower".?) { aliasLower =>
            encodeResponse(Gzip) {
              if(!aliasLower.isDefined) {
                onComplete((indexerActor ? GetMultipleOutfits).mapTo[List[Outfit]]) {
                  case Success(outfits) => complete(outfits)
                  case Failure(m) => complete(InternalServerError, m.toString)
                }
              } else {
                onComplete(
                  (indexerActor ? GetOutfit(aliasLower, None)).mapTo[Option[Outfit]]) {
                  case Success(outfit) => complete {
                    if(outfit.isDefined)
                      List(outfit.get)
                    else
                      NotFound
                  }
                  case Failure(m) => complete(InternalServerError, m.toString)
                }
              }
            }
          }
        }
      } ~
      path(Segment) { id =>
        get {
          encodeResponse(Gzip) {
            onComplete(
              (indexerActor ? GetOutfit(None, Some(id))).mapTo[Option[Outfit]]) {
              case Success(outfit) => complete(outfit)
              case Failure(m) => complete(InternalServerError, m.toString)
            }
          }
        }
      }
    }
  }
}
