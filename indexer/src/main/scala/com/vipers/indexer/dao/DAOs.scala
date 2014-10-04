package com.vipers.indexer.dao

import com.vipers.dbms.DB
import com.vipers.fetcher.FetcherActor.OutfitMember
import com.vipers.model._

private[indexer] object DAOs {
  trait DAOComponent { this: DB =>
    trait DAO[T <: WithID] {
      def find(id: String)(implicit s : Session) : Option[T]
      def findAll(implicit s : Session) : List[T]
      def create(model: T)(implicit s : Session) : Boolean
      def createAll(models : T*)(implicit s : Session) : Boolean
      def createOrUpdate(model : T)(implicit s : Session) : Boolean
      def update(model: T)(implicit s : Session) : Boolean
      def deleteById(id: String)(implicit s : Session) : Boolean
      def deleteAll(implicit s : Session) : Boolean
      def exists(id : String)(implicit s : Session) : Boolean
    }
  }

  trait OutfitDAOComponent extends DAOComponent { this: DB =>
    val outfitDAO : OutfitDAO

    trait OutfitDAO extends DAO[Outfit] {
      def findByNameLower(nameLower : String)(implicit s : Session) : Option[Outfit]
      def findByAliasLower(aliasLower : String)(implicit s : Session) : Option[Outfit]
    }
  }

  trait CharacterDAOComponent extends DAOComponent { this: DB =>
    val characterDAO : CharacterDAO

    trait CharacterDAO extends DAO[Character] {
      def findByNameLower(name : String)(implicit s : Session) : Option[Character]
    }
  }

  trait OutfitMembershipDAOComponent extends DAOComponent { this: DB =>
    val outfitMembershipDAO : OutfitMembershipDAO

    trait OutfitMembershipDAO extends DAO[OutfitMembership] {
      def findAllCharactersByOutfitId(outfitId: String)(implicit s : Session) : List[OutfitMember]
      def deleteAllByOutfitId(outfitId : String)(implicit s : Session) : Boolean
    }
  }

  trait WeaponDAOComponent extends DAOComponent { this : DB =>
    val weaponDAO : WeaponDAO

    trait WeaponDAO extends DAO[Weapon] {}
  }

  trait WeaponStatDAOComponent extends DAOComponent { this : DB =>
    val weaponStatDAO : WeaponStatDAO

    trait WeaponStatDAO {
      def createAll(weaponStats : WeaponStat*)(implicit s : Session) : Unit
      def getCharactersMostRecentWeaponStats(characterId : String)(implicit s : Session) : List[WeaponStat]
      def getCharactersWeaponProgress(characterId : String, weaponId : String)(implicit s : Session) : List[WeaponStat]
    }
  }
}