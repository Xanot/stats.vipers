package com.vipers.routes

import com.vipers.Route

trait UI extends Route {
  protected lazy val uiRoute = {
    pathPrefix("ui") {
      getFromResourceDirectory("client")
    }
  }
}
