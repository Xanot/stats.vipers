package com.vipers.fetcher.model

//================================================================================
// Character request/response
//================================================================================
case class EnrichCharacter(withBattleRank : Option[Boolean] = None,
                           withCerts : Option[Boolean] = None,
                           withFaction : Boolean = false,
                           withTimes : Option[Boolean] = None)

case class FetchCharacterRequest(characterName : Option[String],
                                 characterId : Option[String],
                                 enrich : Option[EnrichCharacter])

case class FetchMultipleCharactersByIdRequest(enrich : Option[EnrichCharacter],
                                              characterIds : String*)

//================================================================================
// Outfit request/response
//================================================================================
case class EnrichOutfit(withLeaderCharacter : Option[EnrichCharacter],
                        withMemberCharacters : Option[EnrichCharacter])

case class FetchOutfitRequest(alias : Option[String],
                              id : Option[String],
                              enrich : Option[EnrichOutfit])

case class FetchOutfitCharactersRequest(alias : Option[String],
                                        id : Option[String],
                                        enrich : Option[EnrichCharacter],
                                        page : Page)

case class FetchOutfitCharactersResponse(total : Int,
                                         characters : Seq[Character])
