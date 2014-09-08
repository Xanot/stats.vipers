import akka.actor.{ActorRefFactory, Actor, Props, ActorSystem}
import akka.io.IO
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.vipers.fetcher.FetcherActor
import com.vipers.fetcher.model.{Outfit, EnrichCharacter, EnrichOutfit, FetchOutfitRequest}
import org.json4s.DefaultFormats
import spray.can.Http
import spray.http.HttpHeaders.RawHeader
import spray.httpx.Json4sSupport
import spray.routing.HttpService
import akka.pattern.ask

import scala.util.{Failure, Success}

object Boot extends App {
  implicit val system = ActorSystem()
  val config = ConfigFactory.load()
  val myListener = system.actorOf(Props[Service], name = "service")
  IO(Http) ! Http.Bind(myListener, interface = config.getString("web.hostname"), port = config.getInt("web.port"))
}

trait Route extends HttpService
trait JsonRoute extends Route with Json4sSupport

class Service extends Actor with JsonRoute {
  import context.dispatcher // ExecutionContext for the futures and scheduler

  implicit val timeout = Timeout(5000)
  implicit val json4sFormats = DefaultFormats

  val fetcherActor = context.actorOf(Props(classOf[FetcherActor]))

  val actorRefFactory : ActorRefFactory = context
  def receive = runRoute {
    respondWithHeaders(RawHeader("Access-Control-Allow-Origin", "*")) { ctx => route(ctx) }
  }

  val route = {
    pathPrefix("ui") {
      getFromResourceDirectory("client")
    } ~
    path("ping") {
      get {
        complete("Pong")
      }
    } ~
    pathPrefix("outfit") {
      path(Segment) { outfitAlias =>
        get {
          onComplete(
            (fetcherActor ? FetchOutfitRequest(Some(outfitAlias), None,
              Some(EnrichOutfit(withLeaderCharacter = Some(EnrichCharacter(withFaction = true)), Some(EnrichCharacter())))
            )).mapTo[Option[Outfit]]
          ) {
            case Success(outfit) => complete(outfit)
            case Failure(m) => complete(m.toString)
          }
        }
      }
    }
  }
}