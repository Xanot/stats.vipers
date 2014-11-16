package com.vipers.indexer

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import akka.actor.ActorSystem
import com.vipers.dbms.DB
import com.vipers.indexer.EventBusComponent.NeedsIndexing
import com.vipers.indexer.dao.DAOs.GameDataIndexedOnDAOComponent
import com.vipers.model.DatabaseModels.GameDataIndexedOn
import scala.concurrent.duration.FiniteDuration

trait GameDataIndexerComponent { this: DB with GameDataIndexedOnDAOComponent with EventBusComponent =>
  trait GameDataIndexer[FetcherResponse] extends Indexer {
    val beingIndexed = new AtomicBoolean(false)
    protected val staleAfter : Long
    protected val needsIndexingEvent : NeedsIndexing

    def schedule(system : ActorSystem) = {
      import system.dispatcher
      system.scheduler.schedule(FiniteDuration(0, TimeUnit.MILLISECONDS),
        FiniteDuration(staleAfter + 10000, TimeUnit.MILLISECONDS)) {
          if(needsIndexing) {
            eventBus.publish(needsIndexingEvent)
          }
      }
    }

    protected def indexImpl(response : FetcherResponse)(implicit s : Session)
    def index(response : FetcherResponse) = {
      try {
        withTransaction { implicit s =>
          indexImpl(response)
          gameDataIndexedOnDAO.createOrUpdate(
            GameDataIndexedOn(needsIndexingEvent.getClass.getSimpleName, System.currentTimeMillis())
          )
          beingIndexed.compareAndSet(true, false)
        }
      } catch {
        case e : Exception =>
          e.printStackTrace()
          beingIndexed.compareAndSet(true, false)
      }
    }

    def needsIndexing : Boolean = {
      def isAlreadyBeingIndexed : Boolean = {
        if(beingIndexed.compareAndSet(false, true)) {
          true
        } else {
          false
        }
      }

      withSession { implicit s =>
        val gameData = gameDataIndexedOnDAO.find(needsIndexingEvent.getClass.getSimpleName)
        if(gameData.nonEmpty) {
          val needsIndexing = if(isStale(gameData.get.lastIndexedOn, staleAfter)) {
            isAlreadyBeingIndexed
          } else {
            false
          }

          needsIndexing
        } else {
          isAlreadyBeingIndexed
        }
      }
    }
  }
}
