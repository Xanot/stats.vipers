import akka.actor.{Props, ActorSystem}
import akka.io.IO
import com.typesafe.config.ConfigFactory
import com.vipers.ApiActor
import spray.can.Http

object Boot extends App {
  implicit val system = ActorSystem()
  val config = ConfigFactory.load()
  val myListener = system.actorOf(Props[ApiActor], name = "service")
  IO(Http) ! Http.Bind(myListener, interface = config.getString("web.hostname"), port = config.getInt("web.port"))
}