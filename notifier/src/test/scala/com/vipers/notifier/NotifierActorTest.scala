package com.vipers.notifier

import java.util.concurrent.CountDownLatch
import akka.actor.{Props, ActorRef, ActorSystem}
import akka.testkit.TestKit
import akka.pattern.ask
import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.websocket.{WebSocketListener, WebSocketUpgradeHandler, WebSocket, DefaultWebSocketListener}
import com.vipers.Test
import com.vipers.notifier.NotifierActor._
import org.scalatest.WordSpecLike
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}

class NotifierActorTest (_system : ActorSystem) extends TestKit(_system) with WordSpecLike with Test with ScalaFutures {
  def this() = this(ActorSystem("NotifierActorTest"))

  private var notifierActor : ActorRef = _
  private implicit val timeout = NotifierActor.timeout
  override implicit val patienceConfig : PatienceConfig = PatienceConfig(Span(5000, Millis))

  override def beforeAll(): Unit = {
    notifierActor = system.actorOf(Props[NotifierActor])
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Notifier actor" should {
    "start the websocket server" in {
      notifierActor ! Start
      whenReady((notifierActor ? IsRunning).mapTo[Boolean]) {_ should be(true)}

      val latch = new CountDownLatch(1)
      val listener = new DefaultWebSocketListener {
        override def onOpen(websocket: WebSocket): Unit = {
          latch.countDown()
        }
      }

      test(listener) {
        latch.await()
        latch.getCount should be(0)
      }
    }

    "process event subscription and notify client" in {
      val latch = new CountDownLatch(1)
      val listener = new DefaultWebSocketListener {
        override def onOpen(websocket: WebSocket): Unit = {
          websocket.sendTextMessage("character:12")
        }

        override def onMessage(message: String): Unit = {
          message match {
            case "character:12" => latch.countDown()
          }
        }
      }

      test(listener) {
        Thread.sleep(500)
        notifierActor ! Notify("character:12")
        latch.await()
        latch.getCount should be(0)
      }
    }

    "stop the websocket server" in {
      notifierActor ! Stop
      whenReady((notifierActor ? IsRunning).mapTo[Boolean]) {_ should be(false)}

      intercept[Exception] {
        test(new DefaultWebSocketListener) {}
      }
    }
  }


  private def test(listener : WebSocketListener)(op : => Unit): Unit = {
    val c = new AsyncHttpClient()
    val websocket = c.prepareGet(s"ws://${Configuration.WebSocket.host}:${Configuration.WebSocket.port}").execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(listener).build()).get()
    try { op }
    finally { websocket.close(); c.close() }
  }
}
