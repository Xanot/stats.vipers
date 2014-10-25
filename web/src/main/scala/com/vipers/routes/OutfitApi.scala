package com.vipers.routes

import akka.pattern.ask
import com.vipers.indexer.IndexerActor._
import com.vipers.{ApiActor, JsonRoute}
import spray.httpx.encoding.Gzip
import scala.util.{Failure, Success}
import spray.http.StatusCodes._

trait OutfitApi extends JsonRoute { this: ApiActor =>
  import context.dispatcher

  protected lazy val outfitRoute = {
    pathPrefix("outfit") {
      path(Segment) { alias =>
        get {
          encodeResponse(Gzip) {
            authorize(alias.length >= 1 && alias.length <= 4) {
              onComplete(indexerActor ? GetOutfitRequest(alias)) {
                case Success(response) =>
                  complete {
                    response match {
                      case m: GetOutfitResponse => m
                      case BeingIndexed => NotFound
                    }
                  }
                case Failure(m) => complete(InternalServerError, m.getStackTrace.mkString("\n"))
              }
            }
          }
        }
      }
    }
  }
}
