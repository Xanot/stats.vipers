package com.vipers.indexer.dao.slick

import com.vipers.dbms.SlickDB
import com.vipers.indexer.dao.DAOs.WeaponDAOComponent
import com.vipers.model.DatabaseModels.Weapon

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
      def lastIndexedOn = column[Long]("last_indexed_on", O.NotNull)
      def profiles = column[Option[String]]("profiles")

      def * = (id,
        name,
        description,
        factionId,
        imagePath,
        isVehicleWeapon,
        lastIndexedOn,
        profiles) <> (Weapon.tupled, Weapon.unapply)
    }
  }
}
