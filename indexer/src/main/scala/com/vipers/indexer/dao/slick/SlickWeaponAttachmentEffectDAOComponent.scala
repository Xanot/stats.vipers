package com.vipers.indexer.dao.slick

import com.vipers.dbms.SlickDB
import com.vipers.indexer.dao.DAOs.WeaponAttachmentEffectDAOComponent
import com.vipers.model.DatabaseModels.WeaponAttachmentEffect

private[indexer] trait SlickWeaponAttachmentEffectDAOComponent extends SlickDAOComponent
  with WeaponAttachmentEffectDAOComponent { this: SlickDB =>
  import driver.simple._

  override lazy val weaponAttachmentEffectDAO = new SlickWeaponAttachmentEffectDAO

  sealed class SlickWeaponAttachmentEffectDAO extends SlickDAO[WeaponAttachmentEffect] with WeaponAttachmentEffectDAO {
    override val table = TableQuery[WeaponAttachmentEffects]

    sealed class WeaponAttachmentEffects(tag : Tag) extends TableWithID(tag, "weapon_attachment_effects") {
      def passiveAbilityId = column[String]("passive_ability_id", O.NotNull, O.DBType("VARCHAR(10)"))
      def effectName = column[String]("effect_name", O.NotNull, O.DBType("VARCHAR(100)"))
      def fireGroupId = column[Option[String]]("fire_group_id", O.DBType("VARCHAR(10)"))
      def fireModeId = column[Option[String]]("fire_mode_id", O.DBType("VARCHAR(10)"))
      def added = column[Option[Float]]("added")
      def percentAdded = column[Option[Float]]("percent_added")
      def setDirectly = column[Option[Float]]("set_directly")
      def weaponMountId = column[Option[String]]("weapon_mount_id", O.DBType("VARCHAR(10)"))
      def equipSlot = column[Option[String]]("equip_slot", O.DBType("VARCHAR(10)"))

      def * = (id,
        passiveAbilityId,
        effectName,
        fireGroupId,
        fireModeId,
        added,
        percentAdded,
        setDirectly,
        weaponMountId,
        equipSlot) <> (WeaponAttachmentEffect.tupled, WeaponAttachmentEffect.unapply)
    }
  }
}
