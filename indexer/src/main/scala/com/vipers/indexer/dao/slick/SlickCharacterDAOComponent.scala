package com.vipers.indexer.dao.slick

import com.vipers.dbms.SlickDB
import com.vipers.indexer.dao.DAOs.CharacterDAOComponent
import com.vipers.model.DatabaseModels.Character

private[indexer] trait SlickCharacterDAOComponent extends CharacterDAOComponent with SlickDAOComponent { this: SlickDB =>
  import driver.simple._

  override lazy val characterDAO = new SlickCharacterDAO

  sealed class SlickCharacterDAO extends SlickDAO[Character] with CharacterDAO {
    override val table = TableQuery[Characters]

    private val findByNameLowerCompiled = Compiled((nameLower : Column[String]) => table.filter(_.nameLower === nameLower))
    override def findByNameLower(nameLower: String)(implicit s : Session) : Option[Character] = findByNameLowerCompiled(nameLower).firstOption

    sealed class Characters(tag : Tag) extends TableWithID(tag, "character") {
      def name = column[String]("name", O.NotNull, O.DBType("VARCHAR(100)"))
      def nameLower = column[String]("name_lower", O.NotNull, O.DBType("VARCHAR(100)"))

      def kills = column[Long]("kills", O.NotNull)
      def deaths = column[Long]("deaths", O.NotNull)
      def score = column[Long]("score", O.NotNull)

      def battleRank = column[Short]("battle_rank", O.NotNull)
      def battleRankPercent = column[Short]("battle_rank_percent", O.NotNull)

      def certsAvailable = column[Int]("certs_available", O.NotNull)
      def certsEarned = column[Int]("certs_earned", O.NotNull)
      def certPercent = column[Short]("cert_percent", O.NotNull)
      def certsSpent = column[Int]("certs_spent", O.NotNull)

      def factionId = column[Byte]("faction_id", O.NotNull)

      def creationDate = column[Long]("creation_date", O.NotNull)
      def lastLoginDate = column[Long]("last_login_date", O.NotNull)
      def lastSaveDate = column[Long]("last_save_date", O.NotNull)
      def loginCount = column[Int]("login_count", O.NotNull)
      def minutesPlayed = column[Int]("minutes_played", O.NotNull)

      def lastIndexedOn = column[Long]("last_indexed_on", O.NotNull)

      def * = (name, nameLower, id, kills, deaths, score, battleRank, battleRankPercent, certsAvailable, certsEarned, certPercent,
        certsSpent, factionId, creationDate, lastLoginDate, lastSaveDate, loginCount, minutesPlayed, lastIndexedOn) <> (Character.tupled, Character.unapply)
    }
  }
}
