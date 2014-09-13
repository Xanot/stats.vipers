package com.vipers.indexer.dao

import com.vipers.dbms.DB

private[indexer] object DAOs {
  sealed trait DAO

  trait OutfitDAOComponent extends DAO { this: DB =>
    val outfitDAO : OutfitDAO

    trait OutfitDAO extends DAO {
      type Outfit
      def findByName(name : String)(implicit s : Session) : Outfit
      def findByAliasLower(aliasLower : String)(implicit s : Session) : Outfit
    }
  }

  trait CharacterDAOComponent extends DAO { this: DB =>
    val characterDAO : CharacterDAO

    trait CharacterDAO extends DAO {
      type Character
      def findByName(name : String)(implicit s : Session) : Character
    }
  }
}