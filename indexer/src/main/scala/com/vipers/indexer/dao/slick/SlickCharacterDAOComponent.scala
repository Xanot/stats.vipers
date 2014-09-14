package com.vipers.indexer.dao.slick

import com.vipers.dbms.SlickDB
import com.vipers.indexer.dao.DAOs.CharacterDAOComponent

private[indexer] trait SlickCharacterDAOComponent extends CharacterDAOComponent { this: SlickDB =>
  val characterDAO = null

//  sealed class SlickCharacterDAO extends CharacterDAO with SlickDAO {
//    override def findByName(name: String)(implicit s : Session) = ???
//  }
}
