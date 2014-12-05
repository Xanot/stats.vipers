package com.vipers.routes

import com.vipers.Route
import spray.http.StatusCodes
import spray.httpx.encoding.Gzip

trait StaticFiles extends Route {
  protected lazy val staticFilesRoute = {
    get {
      encodeResponse(Gzip) {
        path("") {
          redirect("/index.html", StatusCodes.PermanentRedirect)
        } ~
          path("favicon.ico") {
            complete(StatusCodes.NotFound)
          } ~
          path(Rest) { path =>
            getFromDirectory("client-production/%s" format path)
          }
      }
    }
  }
}
