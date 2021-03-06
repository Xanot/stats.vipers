package com.vipers.indexer.dao.slick

import com.vipers.dbms.SlickDB
import com.vipers.indexer.dao.DAOs.CharacterStatDAOComponent
import com.vipers.model.DatabaseModels.ProfileStat

private[indexer] trait SlickCharacterStatDAOComponent extends CharacterStatDAOComponent { this: SlickDB =>
  import driver.simple._

  override lazy val characterStatDAO = new SlickCharacterStatDAO

  sealed class SlickCharacterStatDAO extends CharacterStatDAO {
    val table = TableQuery[CharacterStats]
    private lazy val tableCompiled = Compiled(table)

    override def createAll(profileStats : ProfileStat*)(implicit s : Session) = {
      tableCompiled.insertAll(profileStats:_*)
    }

    override def createOrUpdate(profileStat : ProfileStat)(implicit s : Session) = tableCompiled.insertOrUpdate(profileStat)

    private lazy val filterCharacterCompiled = Compiled((characterId : Column[String]) => table.filter(_.characterId === characterId))
    override def getCharactersProfileStats(characterId : String)(implicit s : Session) : List[ProfileStat] = filterCharacterCompiled(characterId).list

    sealed class CharacterStats(tag : Tag) extends Table[ProfileStat](tag, "character_stats") {
      def characterId = column[String]("character_id", O.NotNull, O.DBType("VARCHAR(30)"))
      def profileId = column[Short]("profile_id", O.NotNull)
      def killedByCount = column[Long]("killed_by_count", O.NotNull)
      def secondsPlayed = column[Long]("seconds_played", O.NotNull)
      def score = column[Long]("score", O.NotNull)

      def pk = primaryKey(s"pk_$tableName", (characterId, profileId))

      def * = (characterId, profileId, killedByCount, secondsPlayed, score) <> (ProfileStat.tupled, ProfileStat.unapply)
    }
  }
}
