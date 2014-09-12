package com.vipers.routes

import com.vipers.{Api, JsonRoute}

trait CharacterApi extends JsonRoute {
  protected lazy val characterRoute = {
    path("character") {
      get {
        complete {
          "ok"
        }
      }
    }
  }
}
