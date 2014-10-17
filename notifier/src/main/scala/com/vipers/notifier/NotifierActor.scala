package com.vipers.notifier

import java.util.concurrent.{TimeUnit, ConcurrentLinkedQueue}
import akka.util.Timeout
import com.vipers.Logging
import com.vipers.notifier.NotifierActor._
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}
import org.eclipse.jetty.util.ConcurrentHashSet
import org.eclipse.jetty.websocket.servlet.{WebSocketServletFactory, WebSocketServlet}
import akka.actor.Actor
import org.eclipse.jetty.server.{ServerConnector, Server}
import org.json4s.NoTypeHints
import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import akka.pattern.pipe
import scala.concurrent.duration.FiniteDuration
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._
import org.json4s.native.Serialization
import org.json4s.Extraction

class NotifierActor extends Actor with Logging {
  import context.dispatcher
  implicit val formats = Serialization.formats(NoTypeHints)

  override def preStart() : Unit = {
    // Periodically send a keep-alive ping
    val timeout = FiniteDuration(Configuration.WebSocket.pingInterval, TimeUnit.MILLISECONDS)
    context.system.scheduler.schedule(timeout, timeout) {
      val iterator = connections.iterator()
      while(iterator.hasNext) {
        val socket = iterator.next()
        if(socket.isNotConnected) {
          iterator.remove()
        } else {
          socket.getRemote.sendPing(null)
        }
      }
    }
  }

  def receive = {
    case Start =>
      listeners = new TrieMap[String, ConcurrentHashSet[NotifierSocket]]
      connections = new ConcurrentLinkedQueue[NotifierSocket]()
      server.start()
    case Stop =>
      server.stop()
      listeners = null
      connections = null
    case IsRunning =>
      Future {
        server.isRunning
      } pipeTo sender
    case Publish(event, data) =>
      Future {
        log.debug("Notifying: " + event)
        val iterator = listeners(event).iterator()
        while(iterator.hasNext) {
          val socket = iterator.next()
          if(socket.isNotConnected) {
            iterator.remove()
          } else {
            socket.getRemote.sendString(compact(render(("event" -> event) ~ ("data" -> Extraction.decompose(data)))))
          }
        }
      }
  }
}

object NotifierActor {
  val timeout = Timeout(5000, TimeUnit.MILLISECONDS)

  private[notifier] var listeners : TrieMap[String, ConcurrentHashSet[NotifierSocket]] = _
  private[notifier] var connections : ConcurrentLinkedQueue[NotifierSocket] = _

  private val server : Server = {
    val server = new Server()
    val connector = new ServerConnector(server)
    connector.setHost(Configuration.WebSocket.host)
    connector.setPort(Configuration.WebSocket.port)
    server.addConnector(connector)
    val context = new ServletContextHandler
    context.setContextPath("/")
    server.setHandler(context)

    val holderEvents = new ServletHolder("WebSocketServlet", new WebSocketServlet {
      override def configure(factory : WebSocketServletFactory) {
        factory.register(classOf[NotifierSocket])
      }
    }.getClass)

    context.addServlet(holderEvents, "/")
    server
  }

  sealed trait NotifierMessage

  // Received
  case object Start extends NotifierMessage
  case object Stop extends NotifierMessage
  case object IsRunning extends NotifierMessage

  // Event types
  case class Publish(event : String, data : AnyRef) extends NotifierMessage
}
