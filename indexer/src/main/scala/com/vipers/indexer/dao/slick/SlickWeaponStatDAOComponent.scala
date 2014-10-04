package com.vipers.indexer.dao.slick

import com.vipers.dbms.SlickDB
import com.vipers.indexer.dao.DAOs.WeaponStatDAOComponent
import com.vipers.model.WeaponStat

private[indexer] trait SlickWeaponStatDAOComponent extends WeaponStatDAOComponent { this: SlickDB =>
  import driver.simple._

  override lazy val weaponStatDAO = new SlickWeaponStatDAO

  sealed class SlickWeaponStatDAO extends WeaponStatDAO {
    val weaponStatsTable = TableQuery[WeaponStats]
    private lazy val weaponStatsTableCompiled = Compiled(weaponStatsTable)

    val weaponStatsTimeSeriesTable = TableQuery[WeaponStatsTimeSeries]
    private lazy val weaponStatsTimeSeriesTableCompiled = Compiled(weaponStatsTimeSeriesTable)

    override def createAll(weaponStats : WeaponStat*)(implicit s : Session) = {
      weaponStats.foreach(stat => weaponStatsTableCompiled.insertOrUpdate(stat))
      weaponStatsTimeSeriesTableCompiled.insertAll(weaponStats:_*)
    }

    private lazy val getCharactersMostRecentWeaponStatsCompiled = Compiled((characterId : Column[String]) => {
      weaponStatsTable.filter(r => r.characterId === characterId)
    })
    override def getCharactersMostRecentWeaponStats(characterId : String)(implicit s : Session) : List[WeaponStat] = {
      getCharactersMostRecentWeaponStatsCompiled(characterId).list
    }

    private lazy val getCharactersWeaponProgressCompiled = Compiled((characterId : Column[String], weaponId : Column[String]) => {
      weaponStatsTimeSeriesTable.filter(r => r.characterId === characterId && r.weaponId === weaponId)
    })
    override def getCharactersWeaponProgress(characterId : String, weaponId : String)(implicit s : Session) : List[WeaponStat] = {
      getCharactersWeaponProgressCompiled(characterId, weaponId).list
    }

    sealed class WeaponStats(tag : Tag) extends Table[WeaponStat](tag, "weapon_stats") with WeaponStatColumns {
      def pk = primaryKey(s"pk_$tableName", (characterId, weaponId))
      def * = (characterId, weaponId, fireCount, hitCount, headshotCount, timestamp) <> (WeaponStat.tupled, WeaponStat.unapply)
    }

    sealed class WeaponStatsTimeSeries(tag : Tag) extends Table[WeaponStat](tag, "weapon_stats_time_series") with WeaponStatColumns {
      def pk = primaryKey(s"pk_$tableName", (characterId, weaponId, timestamp))
      def * = (characterId, weaponId, fireCount, hitCount, headshotCount, timestamp) <> (WeaponStat.tupled, WeaponStat.unapply)
    }

    sealed trait WeaponStatColumns { this: Table[WeaponStat] =>
      def characterId = column[String]("character_id", O.NotNull, O.DBType("VARCHAR(30)"))
      def weaponId = column[String]("weapon_id", O.NotNull, O.DBType("VARCHAR(20)"))
      def fireCount = column[Long]("fire_count", O.NotNull)
      def hitCount = column[Long]("hit_count", O.NotNull)
      def headshotCount = column[Long]("headshot_count", O.NotNull)
      def timestamp = column[Long]("timestamp", O.NotNull)
    }
  }
}
