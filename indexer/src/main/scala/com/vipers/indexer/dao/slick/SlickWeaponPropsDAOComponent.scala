package com.vipers.indexer.dao.slick

import com.vipers.dbms.SlickDB
import com.vipers.indexer.dao.DAOs.WeaponPropsDAOComponent
import com.vipers.model.DatabaseModels.WeaponProps

private[indexer] trait SlickWeaponPropsDAOComponent extends SlickDAOComponent with WeaponPropsDAOComponent { this: SlickDB =>
  import driver.simple._

  override lazy val weaponPropsDAO = new SlickWeaponPropsDAO

  class SlickWeaponPropsDAO extends SlickDAO[WeaponProps] with WeaponPropsDAO {
    override val table = TableQuery[WeaponProps]

    sealed class WeaponProps(tag : Tag) extends TableWithID(tag, "weapon_props") {
      def weaponGroupId = column[Option[String]]("weapon_group_id", O.DBType("VARCHAR(10)"))
      def equipMs = column[Option[Int]]("equip_ms")
      def fromIronSightsMs = column[Option[Int]]("from_iron_sights_ms")
      def toIronSightsMs = column[Option[Int]]("to_iron_sights_ms")
      def unEquipMs = column[Option[Int]]("un_equip_ms")
      def sprintRecoveryMs = column[Option[Int]]("sprint_recovery_ms")
      def moveModifier = column[Float]("move_modifier", O.NotNull)
      def turnModifier = column[Float]("turn_modifier", O.NotNull)
      def heatBleedOffRate = column[Option[Float]]("heat_bleed_off_rate")
      def heatCapacity = column[Option[Int]]("heat_capacity")
      def heatOverheatPenaltyMs = column[Option[Int]]("heat_overheat_penalty_ms")

      def * = (id,
        weaponGroupId,
        equipMs,
        fromIronSightsMs,
        toIronSightsMs,
        unEquipMs,
        sprintRecoveryMs,
        moveModifier,
        turnModifier,
        heatBleedOffRate,
        heatCapacity,
        heatOverheatPenaltyMs) <> (WeaponProps.tupled, WeaponProps.unapply)
    }

  }
}

