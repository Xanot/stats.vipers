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
      def update(model: T)(implicit s : Session) : Boolean
      def deleteById(id: String)(implicit s : Session) : Boolean
      def exists(id : String)(implicit s : Session) : Boolean
    }
  }

  trait OutfitDAOComponent extends DAOComponent { this: DB =>
    val outfitDAO : OutfitDAO

    trait OutfitDAO { this: DAO[Outfit] =>
      def findLeader(outfitId : String)(implicit s : Session) : Option[Character]
      def findByNameLower(nameLower : String)(implicit s : Session) : Option[Outfit]
      def findByAliasLower(aliasLower : String)(implicit s : Session) : Option[Outfit]
    }
  }

  trait CharacterDAOComponent extends DAOComponent { this: DB =>
    val characterDAO : CharacterDAO

    trait CharacterDAO { this: DAO[Character] =>
      def findByNameLower(name : String)(implicit s : Session) : Option[Character]
      def deleteAllByOutfitId(outfitId : String)(implicit s : Session) : Boolean
    }
  }

  trait OutfitMembershipDAOComponent extends DAOComponent { this: DB =>
    val outfitMembershipDAO : OutfitMembershipDAO

    trait OutfitMembershipDAO { this: DAO[OutfitMembership] =>
      def findAllCharactersByOutfitId(outfitId: String)(implicit s : Session) : List[OutfitMember]
      def deleteAllByOutfitId(outfitId : String)(implicit s : Session) : Boolean
    }
  }
}