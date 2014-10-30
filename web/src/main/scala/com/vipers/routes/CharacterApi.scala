package com.vipers.routes

import akka.pattern.ask
import com.vipers.indexer.IndexerActor._
import com.vipers.model.DatabaseModels.WeaponStat
import com.vipers.{ApiActor, JsonRoute}
import spray.httpx.encoding.Gzip
import scala.util.{Success, Failure}
import spray.http.StatusCodes._

trait CharacterApi extends JsonRoute { this: ApiActor =>
  import context.dispatcher

  protected lazy val characterRoute = {
    pathPrefix("player") {
      pathPrefix(Segment) { characterName =>
        pathEnd {
          get {
            encodeResponse(Gzip) {
              onComplete(indexerActor ? GetCharacterRequest(characterName.toLowerCase)) {
                case Success(response) => complete {
                  response match {
                    case m: GetCharacterResponse => m
                    case BeingIndexed => NotFound
                  }
                }
                case Failure(e) => complete(InternalServerError, e.getStackTrace.mkString("\n"))
              }
            }
          }
        } ~
        pathPrefix("stats") {
          path(Segment) { itemId =>
            encodeResponse(Gzip) {
              get {
                onComplete((indexerActor ? GetCharactersWeaponStatHistory(characterName, itemId)).mapTo[List[WeaponStat]]) {
                  case Success(stats) => complete(stats)
                  case Failure(e) => complete(InternalServerError, e.getStackTrace.mkString("\n"))
                }
              }
            }
          }
        }
      }
    }
  }
}
