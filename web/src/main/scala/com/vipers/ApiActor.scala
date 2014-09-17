package com.vipers

import java.util.concurrent.TimeUnit
import akka.actor.{Props, Actor}
import akka.util.Timeout
import com.vipers.indexer.IndexerActor
import com.vipers.routes.{UI, OutfitApi, CharacterApi}
import org.json4s.DefaultFormats
import spray.http.HttpHeaders.RawHeader
import spray.httpx.Json4sSupport
import spray.routing.HttpService

sealed class ApiActor extends Actor with Api {
  override val actorRefFactory = context

  protected val indexerActor = context.actorOf(Props(classOf[IndexerActor]))

  def receive = runRoute {
    respondWithHeaders(RawHeader("Access-Control-Allow-Origin", "*")) { ctx => route(ctx) } // Enable CORS
  }
}

trait Route extends HttpService
trait JsonRoute extends Route with Json4sSupport {
  implicit val timeout = Timeout(15000, TimeUnit.MILLISECONDS)
  implicit val json4sFormats = DefaultFormats
}

trait Api extends CharacterApi with OutfitApi with UI { this: ApiActor =>
  protected lazy val route = {
    uiRoute ~
    outfitRoute ~
    characterRoute
  }
}
