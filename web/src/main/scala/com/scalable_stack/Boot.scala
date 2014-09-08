import akka.actor.{ActorRefFactory, Actor, Props, ActorSystem}
import akka.io.IO
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import spray.can.Http
import spray.http.HttpHeaders.RawHeader
import spray.routing.HttpService

object Boot extends App {
  implicit val system = ActorSystem()
  val config = ConfigFactory.load()
  val myListener = system.actorOf(Props[Service], name = "service")
  IO(Http) ! Http.Bind(myListener, interface = config.getString("web.hostname"), port = config.getInt("web.port"))
}

class Service extends Actor with HttpService {
  implicit val timeout = Timeout(1000)

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
    }
  }
}