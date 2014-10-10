package com.vipers.indexer.dao

import com.vipers.dbms.DB
import com.vipers.indexer.dao.DAOs._

private[indexer] trait DBComponent extends DB
  with OutfitDAOComponent
  with CharacterDAOComponent
  with OutfitMembershipDAOComponent
  with WeaponDAOComponent
  with WeaponStatDAOComponent
