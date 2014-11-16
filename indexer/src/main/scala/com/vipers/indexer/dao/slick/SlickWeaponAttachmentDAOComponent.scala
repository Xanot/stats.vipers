package com.vipers.indexer.dao.slick

import com.vipers.dbms.SlickDB
import com.vipers.indexer.dao.DAOs.WeaponAttachmentDAOComponent
import com.vipers.model.DatabaseModels.WeaponAttachment

private[indexer] trait SlickWeaponAttachmentDAOComponent extends SlickDAOComponent with WeaponAttachmentDAOComponent {
  this: SlickDB =>
  import driver.simple._

  override lazy val weaponAttachmentDAO = new SlickWeaponAttachmentDAO

  sealed class SlickWeaponAttachmentDAO extends SlickDAO[WeaponAttachment] with WeaponAttachmentDAO {
    override val table = TableQuery[WeaponAttachments]

    sealed class WeaponAttachments(tag : Tag) extends TableWithID(tag, "weapon_attachments") {
      def weaponGroupId = column[String]("weapon_group_id", O.NotNull, O.DBType("VARCHAR(10)"))
      def name = column[String]("name", O.NotNull, O.DBType("VARCHAR(100)"))
      def imagePath = column[String]("image_path", O.NotNull, O.DBType("VARCHAR(100)"))
      def description = column[String]("description", O.NotNull, O.DBType("VARCHAR(500)"))
      def passiveAbilityId = column[String]("passive_ability_id", O.NotNull, O.DBType("VARCHAR(10)"))

      def * = (id,
        weaponGroupId,
        name,
        imagePath,
        description,
        passiveAbilityId) <> (WeaponAttachment.tupled, WeaponAttachment.unapply)
    }
  }
}
