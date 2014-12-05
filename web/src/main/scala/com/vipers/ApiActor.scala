package com.vipers

import java.util.concurrent.TimeUnit
import akka.actor.{Props, Actor}
import akka.util.Timeout
import com.vipers.indexer.IndexerActor
import com.vipers.routes.{StaticFiles, WeaponApi, OutfitApi, CharacterApi}
import org.json4s.DefaultFormats
import spray.http.HttpHeaders.RawHeader
import spray.httpx.Json4sSupport
import spray.routing.HttpService

sealed class ApiActor extends Actor with Api {
  override val actorRefFactory = context
  protected val indexerActor = context.actorOf(Props(classOf[IndexerActor]))
  private val cors = RawHeader("Access-Control-Allow-Origin", Configuration.Web.allowOrigin)

  def receive = runRoute {
    respondWithHeaders(cors) { ctx => route(ctx) } // Enable CORS
  }
}

trait Route extends HttpService
trait JsonRoute extends Route with Json4sSupport {
  implicit val timeout = Timeout(30000, TimeUnit.MILLISECONDS)
  implicit val json4sFormats = DefaultFormats
}

trait Api extends CharacterApi with OutfitApi with WeaponApi with StaticFiles { this: ApiActor =>
  protected lazy val route = {
    pathPrefix("api") {
      outfitRoute ~
        characterRoute ~
        weaponRoute
    } ~ staticFilesRoute
  }
}
