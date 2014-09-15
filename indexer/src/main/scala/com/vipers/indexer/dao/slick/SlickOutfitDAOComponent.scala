package com.vipers.indexer.dao.slick

import com.vipers.dbms.SlickDB
import com.vipers.indexer.dao.DAOs.OutfitDAOComponent
import com.vipers.indexer.dao.Model.Outfit

private[indexer] trait SlickOutfitDAOComponent extends SlickDAOComponent with OutfitDAOComponent { this: SlickDB =>
  import driver.simple._

  override lazy val outfitDAO = new SlickOutfitDAO

  sealed class SlickOutfitDAO extends SlickDAO[Outfit] with OutfitDAO {
    override val table = TableQuery[Outfits]

    private val findByNameLowerCompiled = Compiled((nameLower : Column[String]) => table.filter(_.nameLower === nameLower))
    private val findByAliasLowerCompiled = Compiled((aliasLower : Column[String]) => table.filter(_.aliasLower === aliasLower))

    override def findByNameLower(nameLower: String)(implicit s : Session) : Option[Outfit] = findByNameLowerCompiled(nameLower).firstOption
    override def findByAliasLower(aliasLower: String)(implicit s : Session): Option[Outfit] = findByAliasLowerCompiled(aliasLower).firstOption

    sealed class Outfits(tag : Tag) extends TableWithID(tag, "outfit") {
      def name = column[String]("name", O.NotNull, O.DBType("VARCHAR(100)"))
      def nameLower = column[String]("name_lower", O.NotNull, O.DBType("VARCHAR(100)"))
      def alias = column[String]("alias", O.DBType("VARCHAR(4)"))
      def aliasLower = column[String]("alias_lower", O.DBType("VARCHAR(4)"))
      def leaderCharacterId = column[String]("leader_character_id", O.NotNull, O.DBType("VARCHAR(20)"))
      def creationDate = column[Long]("creation_date", O.NotNull)
      def memberCount = column[Int]("member_count", O.NotNull)

      def * = (name, nameLower, alias, aliasLower, leaderCharacterId, memberCount, id, creationDate) <> (Outfit.tupled, Outfit.unapply)
    }
  }
}
