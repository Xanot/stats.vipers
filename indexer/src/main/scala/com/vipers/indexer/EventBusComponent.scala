package com.vipers.indexer

import akka.actor.{ActorRef, ActorSystem}
import com.vipers.indexer.EventBusComponent._

private[indexer] trait EventBusComponent {
  val eventBus = new EventBus

  class EventBus {
    private val system = ActorSystem("event-bus")

    def subscribe[T <: IndexerEvent](subscriber : ActorRef, channel : Class[T]) : Boolean = {
      system.eventStream.subscribe(subscriber, channel)
    }
    def unsubscribe(subscriber : ActorRef) = {
      system.eventStream.unsubscribe(subscriber)
    }
    def unsubscribe[T <: IndexerEvent](subscriber : ActorRef, channel : Class[T]) : Boolean = {
      system.eventStream.unsubscribe(subscriber, channel)
    }
    def publish[T <: IndexerEvent](event : T) = {
      system.eventStream.publish(event)
    }
  }
}

private[indexer] object EventBusComponent {
  sealed trait IndexerEvent

  sealed trait Indexed extends IndexerEvent
  case class CharacterIndexed(nameLower : String) extends Indexed
  case class CharacterWeaponStatsIndexed(nameLower : String) extends Indexed
  case class CharacterProfileStatsIndexed(nameLower : String) extends Indexed
  case class OutfitIndexed(outfitAliasLower : String) extends Indexed

  sealed trait NeedsIndexing extends IndexerEvent
  case class CharacterNeedsIndexing(nameLower : String) extends NeedsIndexing
  case class OutfitNeedsIndexing(aliasLower : String) extends NeedsIndexing
}
