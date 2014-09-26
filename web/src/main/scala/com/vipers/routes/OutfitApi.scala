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
          encodeResponse(Gzip) {
            onComplete((indexerActor ? GetAllIndexedOutfits).mapTo[List[Outfit]]) {
              case Success(outfits) => complete(outfits)
              case Failure(m) => complete(InternalServerError, m.toString)
            }
          }
        }
      } ~
      path(Segment) { aliasOrId =>
        get {
          encodeResponse(Gzip) {
            val request = if(aliasOrId.length <= 4) {
              indexerActor ? GetOutfitRequest(Some(aliasOrId), None)
            } else {
              indexerActor ? GetOutfitRequest(None, Some(aliasOrId))
            }
            onComplete(request) {
              case Success(response) =>
                complete {
                  response match {
                    case m: GetOutfitResponse => m
                    case BeingIndexed => NotFound
                  }
                }
              case Failure(m) => complete(InternalServerError, m.toString)
            }
          }
        }
      }
    }
  }
}
