package com.vipers.notifier

import java.util.concurrent.ConcurrentLinkedQueue
import com.vipers.Logging
import org.eclipse.jetty.websocket.api.{WebSocketAdapter, Session}

private[notifier] class NotifierSocket extends WebSocketAdapter with Logging {
  override protected def onWebSocketConnect(sess : Session) {
    super.onWebSocketConnect(sess)
    NotifierActor.connections.add(this)
  }

  override protected def onWebSocketText(event : String) {
    subscribe(event)
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
      if(!listeners(event).contains(this)) {
        listeners(event).add(this)
      }
    } else {
      listeners += (event -> new ConcurrentLinkedQueue[NotifierSocket])
      listeners(event).add(this)
    }
  }
}
