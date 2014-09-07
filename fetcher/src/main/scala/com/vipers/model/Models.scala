package com.vipers.model

case class Outfit(name : String,
                  alias : String,
                  leaderCharacterId : String,
                  memberCount : Int,
                  id : String,
                  creationDate : Long,
                  leaderCharacter : Option[Character],
                  members : Option[Seq[Character]])

case class OutfitMember(rank : String,
                        rankOrdinal : Byte,
                        memberSinceData : Long)

case class BattleRank(rank : Short, percentToNext : Short)

case class Certs(availablePoints : Int,
                 earnedPoints : Int,
                 giftedPoints : Int,
                 percentToNext : Double,
                 spentPoints : Int)

case class Faction(name : String,
                   codeTag : String,
                   id : String,
                   imageId : String,
                   imagePath : String,
                   imageSetId : String)

case class Times(creationDate : Long,
                 lastLoginDate : Long,
                 lastSaveDate : Long,
                 loginCount : Int,
                 minutesPlayed : Int)

case class Character(name : String,
                     id : String,
                     battleRank : Option[BattleRank],
                     certs : Option[Certs],
                     faction : Option[Faction],
                     times : Option[Times],
                     membership : Option[OutfitMember])