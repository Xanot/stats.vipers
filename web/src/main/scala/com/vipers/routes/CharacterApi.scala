package com.vipers.routes

import akka.pattern.ask
import com.vipers.indexer.IndexerActor._
import com.vipers.model.{WeaponStat, Character}
import com.vipers.{ApiActor, JsonRoute}
import scala.util.{Success, Failure}
import spray.http.StatusCodes._

trait CharacterApi extends JsonRoute { this: ApiActor =>
  import context.dispatcher

  protected lazy val characterRoute = {
    pathPrefix("player") {
      pathEnd {
        get {
          onComplete((indexerActor ? GetAllIndexedCharacters).mapTo[List[Character]]) {
            case Success(characters) => complete{ characters }
            case Failure(e) => complete(InternalServerError, e.toString)
          }
        }
      } ~
      pathPrefix(Segment) { characterName =>
        pathEnd {
          get {
            onComplete(indexerActor ? GetCharacterRequest(characterName.toLowerCase)) {
              case Success(response) => complete {
                response match {
                  case m : GetCharacterResponse => m
                  case BeingIndexed => NotFound
                }
              }
              case Failure(e) => complete(InternalServerError, e.toString)
            }
          }
        } ~
        pathPrefix("stats") {
          path(Segment) { itemId =>
            get {
              onComplete((indexerActor ? GetCharactersWeaponStatHistory(characterName, itemId)).mapTo[List[WeaponStat]]) {
                case Success(stats) => complete(stats)
                case Failure(e) => complete(InternalServerError, e.toString)
              }
            }
          }
        }
      }
    }
  }
}
