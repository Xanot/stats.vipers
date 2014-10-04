package com.vipers.routes

import com.vipers.indexer.IndexerActor.{BeingIndexed, GetAllWeapons}
import com.vipers.{ApiActor, Route}
import akka.pattern.ask
import spray.http.StatusCodes.{NotFound, InternalServerError}
import scala.util.{Failure, Success}

trait WeaponApi extends Route { this: ApiActor =>
  import context.dispatcher

  protected lazy val weaponRoute = {
    path("weapon") {
      get {
        onComplete(indexerActor ? GetAllWeapons) {
          case Success(response) => complete {
            response match {
              case m : List[_] => m
              case BeingIndexed => NotFound
            }
          }
          case Failure(e) => complete(InternalServerError, e.toString)
        }
      }
    }
  }
}
