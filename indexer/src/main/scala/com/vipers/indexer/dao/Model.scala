package com.vipers.indexer.dao

private[indexer] object Model {
  trait WithID {
    val id : String
  }

  case class Outfit(name : String,
                    nameLower : String,
                    alias : String,
                    aliasLower : String,
                    leaderCharacterId : String,
                    memberCount : Int,
                    override val id : String,
                    creationDate : Long) extends WithID
}
