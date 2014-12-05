package com.vipers.indexer.dao

import java.sql.SQLException

import com.vipers.dao.DAOTest
import com.vipers.indexer.dao.slick.{SlickWeaponAttachmentEffectDAOComponent, SlickWeaponAttachmentDAOComponent, SlickWeaponPropsDAOComponent, SlickWeaponDAOComponent}
import org.scalatest.WordSpecLike

class SlickWeaponDAOTest extends WordSpecLike with DAOTest with SlickDBTest with Sample
  with SlickWeaponDAOComponent
  with SlickWeaponPropsDAOComponent
  with SlickWeaponAttachmentDAOComponent
  with SlickWeaponAttachmentEffectDAOComponent {
  import driver.simple._

  override protected val ddl = weaponDAO.table.ddl

  "Weapon" should {
    "be created" in {
      withTransaction { implicit s =>
        weaponDAO.create(SampleWeapons.Corvus) should be(true)
        weaponDAO.create(SampleWeapons.NS15) should be(true)

        intercept[SQLException] {
          weaponDAO.create(SampleWeapons.Corvus)
        }
      }
    }

    "s should be retrieved" in {
      withSession { implicit s =>
        weaponDAO.findAll match {
          case b :: c :: Nil =>
            b.name should be("Corvus VA55")
            c.name should be("NS-15M")
          case _ => throw new Error
        }
      }
    }

    "be updated" in {
      withTransaction { implicit s =>
        weaponDAO.update(SampleWeapons.Corvus.copy(name = "Maelstrom")) should be(true)
        weaponDAO.find(SampleWeapons.Corvus.id).get.name should be("Maelstrom")
      }
    }

    "be removed" in {
      withTransaction { implicit s =>
        weaponDAO.deleteById(SampleWeapons.Corvus.id) should be(true)
        weaponDAO.find(SampleWeapons.Corvus.id) should be(None)
      }
    }
  }
}
