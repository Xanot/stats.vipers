package com.vipers.indexer.dao.slick

import com.vipers.dbms.SlickDB
import com.vipers.indexer.dao.DAOs.WeaponDAOComponent
import com.vipers.model.DatabaseModels.{WeaponAttachmentEffect, WeaponAttachment, WeaponProps, Weapon}

private[indexer] trait SlickWeaponDAOComponent extends SlickDAOComponent with WeaponDAOComponent {
  this: SlickDB
    with SlickWeaponPropsDAOComponent
    with SlickWeaponAttachmentDAOComponent
    with SlickWeaponAttachmentEffectDAOComponent =>
  import driver.simple._

  override lazy val weaponDAO = new SlickWeaponDAO

  sealed class SlickWeaponDAO extends SlickDAO[Weapon] with WeaponDAO {
    override val table = TableQuery[Weapons]

    private lazy val findWeaponWithPropsCompiled = Compiled((itemId : Column[String]) => {
      for {
        weapon <- table if weapon.id === itemId
        weaponProps <- weaponPropsDAO.table if weaponProps.id === weapon.id
      } yield(weapon, weaponProps)
    })
    override def findWeaponWithAttachments(itemId : String)(implicit s : Session) :
      Option[(Weapon, WeaponProps, List[(WeaponAttachment, List[WeaponAttachmentEffect])])] = {
      findWeaponWithPropsCompiled(itemId).firstOption flatMap { case(weapon, weaponProps) =>
        weaponProps.weaponGroupId.map { groupId =>
          val attachments = weaponAttachmentDAO.filterByWeaponGroupId(groupId)
            .map { attachment => (attachment, weaponAttachmentEffectDAO.filterByAbilityId(attachment.passiveAbilityId)) }
          (weapon, weaponProps, attachments)
        }
      }
    }

    sealed class Weapons(tag : Tag) extends TableWithID(tag, "weapon") {
      def name = column[String]("name", O.NotNull, O.DBType("VARCHAR(100)"))
      def description = column[Option[String]]("description", O.DBType("VARCHAR(500)"))
      def imagePath = column[String]("image_path", O.NotNull, O.DBType("VARCHAR(100)"))
      def factionId = column[Option[Byte]]("faction_id")
      def isVehicleWeapon = column[Boolean]("is_vehicle_weapon", O.NotNull)
      def profiles = column[Option[String]]("profiles")

      def * = (id,
        name,
        description,
        factionId,
        imagePath,
        isVehicleWeapon,
        profiles) <> (Weapon.tupled, Weapon.unapply)
    }
  }
}
