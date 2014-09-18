package com.vipers.notifier

import java.util.concurrent.ConcurrentLinkedQueue
import com.vipers.Logging
import org.eclipse.jetty.websocket.api.{WebSocketAdapter, Session}
import org.json4s.JsonAST.{JObject, JString}
import org.json4s.native.JsonMethods._

private[notifier] class NotifierSocket extends WebSocketAdapter with Logging {
  override protected def onWebSocketConnect(sess : Session) {
    super.onWebSocketConnect(sess)
    NotifierActor.connections.add(this)
  }

  override protected def onWebSocketText(message : String) {
    parse(message) match {
      case JObject(msg) =>
        msg match {
          case ("event", JString(event)) :: ("data", JString(data)) :: Nil =>
            event match {
              case "subscribe" => subscribe(data)
              case _ => getSession.close()
            }
          case _ => getSession.close()
        }
      case _ => getSession.close()
    }
  }

  override protected def onWebSocketClose(statusCode : Int , reason : String) {
    super.onWebSocketClose(statusCode, reason)
  }

  override protected def onWebSocketError(cause : Throwable) {
    cause.printStackTrace()
  }

  private def subscribe(event : String) : Unit = {
    import NotifierActor.listeners
    if(listeners.keySet.contains(event)) {
      listeners(event).add(this)
    } else {
      listeners += (event -> new ConcurrentLinkedQueue[NotifierSocket])
      listeners(event).add(this)
    }
  }
}
