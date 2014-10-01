package com.vipers.indexer.dao.slick

import com.vipers.dbms.SlickDB
import com.vipers.indexer.dao.DAOs.WeaponDAOComponent
import com.vipers.model.Weapon

private[indexer] trait SlickWeaponDAOComponent extends SlickDAOComponent with WeaponDAOComponent { this: SlickDB =>
  import driver.simple._

  override lazy val weaponDAO = new SlickWeaponDAO

  sealed class SlickWeaponDAO extends SlickDAO[Weapon] with WeaponDAO {
    override val table = TableQuery[Weapons]

    sealed class Weapons(tag : Tag) extends TableWithID(tag, "weapon") {
      def name = column[String]("name", O.NotNull, O.DBType("VARCHAR(100)"))
      def description = column[Option[String]]("description", O.DBType("VARCHAR(500)"))
      def imagePath = column[String]("image_path", O.NotNull, O.DBType("VARCHAR(100)"))
      def factionId = column[Option[Byte]]("faction_id")
      def isVehicleWeapon = column[Boolean]("is_vehicle_weapon", O.NotNull)
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
      def lastIndexedOn = column[Long]("last_indexed_on", O.NotNull)

      def * = (id,
        name,
        description,
        factionId,
        imagePath,
        isVehicleWeapon ,
        equipMs,
        fromIronSightsMs,
        toIronSightsMs,
        unEquipMs,
        sprintRecoveryMs,
        moveModifier,
        turnModifier,
        heatBleedOffRate,
        heatCapacity,
        heatOverheatPenaltyMs,
        lastIndexedOn) <> (Weapon.tupled, Weapon.unapply)
    }
  }
}
