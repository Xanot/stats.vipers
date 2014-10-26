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
                  creationDate : Long,
                  lastIndexedOn : Long) extends WithID

case class Character(name : String,
                     nameLower : String,
                     override val id : String,

                     kills : Long,
                     deaths : Long,
                     score : Long,

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
                     minutesPlayed : Int,
                     lastIndexedOn : Long) extends WithID

case class OutfitMembership(outfitId : String,
                            override val id : String, // Character id
                            outfitRank : String,
                            outfitRankOrdinal : Byte,
                            outfitMemberSinceDate : Long) extends WithID

case class Weapon(override val id : String, // Item id
                  name : String,
                  description : Option[String],
                  factionId : Option[Byte],
                  imagePath : String,
                  isVehicleWeapon : Boolean,
                  equipMs : Option[Int],
                  fromIronSightsMs : Option[Int],
                  toIronSightsMs : Option[Int],
                  unEquipMs : Option[Int],
                  sprintRecoveryMs : Option[Int],
                  moveModifier : Float,
                  turnModifier : Float,
                  heatBleedOffRate : Option[Float],
                  heatCapacity : Option[Int],
                  heatOverheatPenaltyMs : Option[Int],
                  lastIndexedOn : Long) extends WithID

case class WeaponStat(characterId : String,
                      itemId : String,
                      fireCount : Long,
                      hitCount : Long,
                      headshotCount : Long,
                      killCount : Long,
                      deathCount : Long,
                      secondsPlayed : Long,
                      score : Long,
                      lastSaveDate : Long)

case class ProfileStat(characterId : String,
                       profileId : Short,
                       killedBy : Long,
                       playTime : Long,
                       score : Long)