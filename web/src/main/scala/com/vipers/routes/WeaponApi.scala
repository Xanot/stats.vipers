package com.vipers.routes

import com.vipers.indexer.IndexerActor.GetWeaponRequest
import com.vipers.{ApiActor, Route}
import akka.pattern.ask
import spray.http.StatusCodes.InternalServerError
import scala.util.{Failure, Success}
import spray.http.StatusCodes._

trait WeaponApi extends Route { this: ApiActor =>
  import context.dispatcher

  protected lazy val weaponRoute = {
    path("weapon" / Segment) { itemId =>
      get {
        onComplete(indexerActor ? GetWeaponRequest(itemId)) {
          case Success(r : Option[AnyRef]) => r match {
            case Some(_) => complete(r)
            case None => complete(NotFound)
          }
          case Failure(f) => complete(InternalServerError, f.getStackTrace.mkString("\n"))
        }
      }
    }
  }
}
