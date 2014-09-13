package com.vipers.indexer.dao.impl

import com.vipers.dbms.SlickDB
import com.vipers.fetcher.model.{Outfit => OutfitModel}
import com.vipers.indexer.dao.DAOs.OutfitDAOComponent

private[indexer] trait SlickOutfitDAOComponent extends OutfitDAOComponent { this: SlickDB =>
  val outfitDAO = new SlickOutfitDAO

  sealed class SlickOutfitDAO extends OutfitDAO {
    override type Outfit = OutfitModel
    override def findByName(name: String)(implicit s : Session) : Outfit = ???
    override def findByAliasLower(aliasLower: String)(implicit s : Session): Outfit = ???
  }
}
