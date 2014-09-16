package com.vipers.model

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

case class Character(name : String,
                     override val id : String,

                     battleRank : Short,
                     battleRankPercent : Short,

                     availableCerts : Int,
                     earnedCerts : Int,
                     certPercent : Short,
                     spentCerts : Int,

                     factionCodeTag : String,

                     creationDate : Long,
                     lastLoginDate : Long,
                     lastSaveDate : Long,
                     loginCount : Int,
                     minutesPlayed : Int) extends WithID

case class OutfitMembership(outfitId : String,
                            outfitRank : String,
                            outfitRankOrdinal : Byte,
                            outfitMemberSinceDate : Long)