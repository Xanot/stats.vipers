package com.vipers.indexer.dao

import com.vipers.dbms.DB
import com.vipers.fetcher.FetcherActor.OutfitMember
import com.vipers.model.DatabaseModels._

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

  trait WeaponDAOComponent extends DAOComponent { this: DB =>
    val weaponDAO : WeaponDAO

    trait WeaponDAO extends DAO[Weapon] {
      def createItemProfiles(itemProfiles : ItemProfile*)(implicit s : Session) : Unit
      def deleteItemProfiles(implicit s : Session) : Unit
    }
  }

  trait WeaponStatDAOComponent extends DAOComponent { this: DB =>
    val weaponStatDAO : WeaponStatDAO

    trait WeaponStatDAO {
      def insertTimeSeries(weaponStats : WeaponStat*)(implicit s : Session) : Unit
      def createAll(weaponStats : WeaponStat*)(implicit s : Session)
      def createOrUpdate(weaponStat : WeaponStat)(implicit s : Session)
      def createOrUpdateLastIndexedOn(characterId : String, stamp : Long)(implicit s : Session)
      def getCharactersWeaponStatsLastIndexedOn(characterId : String)(implicit s : Session) : Option[Long]
      def getCharactersWeaponStatsLastSavedOn(characterId : String)(implicit s : Session) : Option[Long]
      def getCharactersMostRecentWeaponStat(characterId : String, itemId : String)(implicit s : Session) : Option[WeaponStat]
      def getCharactersMostRecentWeaponStats(characterId : String)(implicit s : Session) : List[(WeaponStat, Weapon)]
      def getCharactersWeaponStatHistory(characterId : String, weaponId : String)(implicit s : Session) : List[WeaponStat]
    }
  }

  trait CharacterStatDAOComponent extends DAOComponent { this: DB =>
    val characterStatDAO : CharacterStatDAO

    trait CharacterStatDAO {
      def createAll(profileStats : ProfileStat*)(implicit s : Session)
      def createOrUpdate(profileStat : ProfileStat)(implicit s : Session)
      def getCharactersProfileStats(characterId : String)(implicit s : Session) : List[ProfileStat]
    }
  }

  trait ProfileDAOComponent extends DAOComponent { this: DB =>
    val profileDAO : ProfileDAO

    trait ProfileDAO extends DAO[Profile] {}
  }

  trait VehicleDAOComponent extends DAOComponent { this: DB =>
    val vehicleDAO : VehicleDAO

    trait VehicleDAO extends DAO[Vehicle] {}
  }
}