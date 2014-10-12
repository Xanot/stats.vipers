package com.vipers.routes

import com.vipers.{ApiActor, Route}
import akka.pattern.ask
import spray.http.StatusCodes.{NotFound, InternalServerError}
import scala.util.{Failure, Success}

trait WeaponApi extends Route { this: ApiActor =>
  import context.dispatcher

  protected lazy val weaponRoute = {
    path("weapon") {
      get {
        complete {
          "Ok"
        }
      }
    }
  }
}
