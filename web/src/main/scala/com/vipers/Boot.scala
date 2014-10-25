import akka.actor.{Props, ActorSystem}
import akka.io.IO
import com.vipers.{Configuration, ApiActor}
import spray.can.Http

object Boot extends App {
  implicit val system = ActorSystem()
  val myListener = system.actorOf(Props[ApiActor], name = "service")
  IO(Http) ! Http.Bind(myListener, interface = Configuration.Web.host, port = Configuration.Web.port)
}