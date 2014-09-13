package com.vipers.indexer.dao.impl

import com.vipers.dbms.SlickDB
import com.vipers.fetcher.model.{Character => CharacterModel}
import com.vipers.indexer.dao.DAOs.CharacterDAOComponent

private[indexer] trait SlickCharacterDAOComponent extends CharacterDAOComponent { this: SlickDB =>
  val characterDAO = new SlickCharacterDAO

  sealed class SlickCharacterDAO extends CharacterDAO {
    override type Character = CharacterModel
    override def findByName(name: String)(implicit s : Session) : Character = ???
  }
}
