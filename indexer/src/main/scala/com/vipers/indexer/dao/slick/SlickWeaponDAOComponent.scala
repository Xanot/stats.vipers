package com.vipers.indexer.dao.slick

import com.vipers.dbms.SlickDB
import com.vipers.indexer.dao.DAOs.WeaponDAOComponent
import com.vipers.model.DatabaseModels.{Weapon, ItemProfile}

private[indexer] trait SlickWeaponDAOComponent extends SlickDAOComponent with WeaponDAOComponent { this: SlickDB =>
  import driver.simple._

  override lazy val weaponDAO = new SlickWeaponDAO

  sealed class SlickWeaponDAO extends SlickDAO[Weapon] with WeaponDAO {
    override val table = TableQuery[Weapons]

    val itemProfileTable = TableQuery[ItemProfileTable]
    private lazy val itemProfileTableCompiled = Compiled(itemProfileTable)

    override def createItemProfiles(itemProfiles : ItemProfile*)(implicit s : Session) : Unit = {
      itemProfileTableCompiled.insertAll(itemProfiles:_*)
    }

    override def deleteItemProfiles(implicit s : Session) : Unit = {
      itemProfileTableCompiled.delete
    }

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

    sealed class ItemProfileTable(tag : Tag) extends Table[ItemProfile](tag, "item_profile") {
      def itemId = column[String]("item_id", O.NotNull, O.DBType("VARCHAR(20)"))
      def profileId = column[Short]("profile_id", O.NotNull)
      def * = (itemId, profileId)

      def pk = primaryKey(s"pk_$tableName", (itemId, profileId))
    }
  }
}
