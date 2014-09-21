package com.vipers.routes

import akka.pattern.ask
import com.vipers.indexer.IndexerActor.{BeingIndexed, GetCharacterRequest}
import com.vipers.{ApiActor, JsonRoute}
import scala.util.{Success, Failure}
import spray.http.StatusCodes._
import com.vipers.indexer.IndexerActor.CharacterWithMembership

trait CharacterApi extends JsonRoute { this: ApiActor =>
  import context.dispatcher

  protected lazy val characterRoute = {
    pathPrefix("player") {
      pathEnd {
        get {
          complete {
            "ok"
          }
        }
      } ~
      path(Segment) { characterName =>
        get {
          onComplete(indexerActor ? GetCharacterRequest(characterName.toLowerCase)) {
            case Success(response) => complete {
              response match {
                case m : CharacterWithMembership => m
                case BeingIndexed => NotFound
              }
            }
            case Failure(e) => complete(InternalServerError, e.toString)
          }
        }
      }
    }
  }
}
