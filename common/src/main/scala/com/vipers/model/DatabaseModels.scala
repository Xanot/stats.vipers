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
                  factionId : Byte,
                  override val id : String,
                  creationDate : Long) extends WithID

case class Character(name : String,
                     nameLower : String,
                     override val id : String,

                     battleRank : Short,
                     battleRankPercent : Short,

                     availableCerts : Int,
                     earnedCerts : Int,
                     certPercent : Short,
                     spentCerts : Int,

                     factionId : Byte,

                     creationDate : Long,
                     lastLoginDate : Long,
                     lastSaveDate : Long,
                     loginCount : Int,
                     minutesPlayed : Int) extends WithID

case class OutfitMembership(outfitId : String,
                            override val id : String, // Character id
                            outfitRank : String,
                            outfitRankOrdinal : Byte,
                            outfitMemberSinceDate : Long) extends WithID