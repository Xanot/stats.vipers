package com.vipers.indexer

import com.vipers.dbms.DB
import com.vipers.fetcher.FetcherActor.FetchAllWeaponAttachmentsResponse
import com.vipers.indexer.EventBusComponent.WeaponAttachmentsNeedIndexing
import com.vipers.indexer.dao.DAOs.{GameDataIndexedOnDAOComponent, WeaponAttachmentDAOComponent, WeaponAttachmentEffectDAOComponent}

private[indexer] trait WeaponAttachmentIndexerComponent extends GameDataIndexerComponent {
  this: DB
    with WeaponAttachmentDAOComponent
    with WeaponAttachmentEffectDAOComponent
    with EventBusComponent
    with GameDataIndexedOnDAOComponent =>

  val weaponAttachmentIndexer = new WeaponAttachmentIndexer

  class WeaponAttachmentIndexer extends GameDataIndexer[FetchAllWeaponAttachmentsResponse] {
    override protected val staleAfter: Long = Configuration.weaponAttachmentsStaleAfter
    override protected val needsIndexingEvent = WeaponAttachmentsNeedIndexing

    override protected def indexImpl(response : FetchAllWeaponAttachmentsResponse)(implicit s: Session) : Unit = {
      weaponAttachmentDAO.deleteAll
      weaponAttachmentDAO.createAll(response.attachments:_*)

      weaponAttachmentEffectDAO.deleteAll
      weaponAttachmentEffectDAO.createAll(response.effects:_*)
    }
  }
}
