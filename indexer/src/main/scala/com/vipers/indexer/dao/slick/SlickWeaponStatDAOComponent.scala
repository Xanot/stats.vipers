package com.vipers.indexer.dao.slick

import com.vipers.dbms.SlickDB
import com.vipers.indexer.Configuration
import com.vipers.indexer.dao.DAOs.WeaponStatDAOComponent
import com.vipers.model.DatabaseModels.{Weapon, WeaponStat}

private[indexer] trait SlickWeaponStatDAOComponent extends WeaponStatDAOComponent { this: SlickDB with SlickWeaponDAOComponent =>
  import driver.simple._

  override lazy val weaponStatDAO = new SlickWeaponStatDAO

  sealed class SlickWeaponStatDAO extends WeaponStatDAO {
    val weaponStatsTable = TableQuery[WeaponStats]
    private lazy val weaponStatsTableCompiled = Compiled(weaponStatsTable)

    val weaponStatsTimeSeriesTable = TableQuery[WeaponStatsTimeSeries]
    private lazy val weaponStatsTimeSeriesTableCompiled = Compiled(weaponStatsTimeSeriesTable)

    val weaponStatsIndexed = TableQuery[WeaponStatsIndexed]
    private lazy val weaponStatsIndexedCompiled = Compiled(weaponStatsIndexed)

    override def insertTimeSeries(weaponStats : WeaponStat*)(implicit s : Session) = {
      try { weaponStatsTimeSeriesTableCompiled.insertAll(weaponStats:_*) }
      catch { case _ : Exception => } // Ignore primary key collisions
    }

    override def createAll(weaponStats : WeaponStat*)(implicit s : Session) = {
      weaponStatsTableCompiled.insertAll(weaponStats:_*)
    }

    override def createOrUpdate(weaponStat : WeaponStat)(implicit s : Session) = weaponStatsTableCompiled.insertOrUpdate(weaponStat)

    private lazy val getCharactersMostRecentWeaponStatsCompiled = Compiled((characterId : Column[String]) => {
      for {
        weaponStat <- weaponStatsTable if weaponStat.characterId === characterId && weaponStat.killCount >= Configuration.weaponStatKillThreshold
        weapon <- weaponDAO.table if weapon.id === weaponStat.itemId
      } yield (weaponStat, weapon)
    })
    override def getCharactersMostRecentWeaponStats(characterId : String)(implicit s : Session) : List[(WeaponStat, Weapon)] = {
      getCharactersMostRecentWeaponStatsCompiled(characterId).list
    }

    private lazy val getCharactersWeaponStatsLastIndexedOnCompiled = Compiled((characterId : Column[String]) => {
      weaponStatsIndexed.filter(_.characterId === characterId).map(_.lastIndexedOn)
    })
    override def getCharactersWeaponStatsLastIndexedOn(characterId : String)(implicit s : Session) : Option[Long] = {
      getCharactersWeaponStatsLastIndexedOnCompiled(characterId).firstOption
    }

    private lazy val getCharactersWeaponStatsLastSavedOnCompiled = Compiled((characterId : Column[String]) => {
      weaponStatsTable.filter(_.characterId === characterId).map(_.lastSaved).max
    })
    override def getCharactersWeaponStatsLastSavedOn(characterId : String)(implicit s : Session) : Option[Long] = {
      getCharactersWeaponStatsLastSavedOnCompiled(characterId).run
    }

    private lazy val getCharactersMostRecentWeaponStatCompiled = Compiled((characterId : Column[String], itemId : Column[String]) => {
      weaponStatsTable.filter(r => r.characterId === characterId && r.itemId === itemId)
    })
    def getCharactersMostRecentWeaponStat(characterId : String, itemId : String)(implicit s : Session) : Option[WeaponStat] = {
      getCharactersMostRecentWeaponStatCompiled(characterId, itemId).firstOption
    }

    override def createOrUpdateLastIndexedOn(characterId : String, stamp : Long)(implicit s : Session) = {
      weaponStatsIndexedCompiled.insertOrUpdate((characterId, stamp))
    }

    private lazy val getCharactersWeaponStatHistoryCompiled = Compiled((characterId : Column[String], weaponId : Column[String]) => {
      weaponStatsTimeSeriesTable.filter(r => r.characterId === characterId && r.itemId === weaponId).sortBy(_.lastSaved.asc)
    })
    override def getCharactersWeaponStatHistory(characterId : String, itemId : String)(implicit s : Session) : List[WeaponStat] = {
      getCharactersWeaponStatHistoryCompiled(characterId, itemId).list
    }

    sealed class WeaponStats(tag : Tag) extends Table[WeaponStat](tag, "weapon_stats") with WeaponStatColumns {
      def pk = primaryKey(s"pk_$tableName", (characterId, itemId))
      def * = (characterId, itemId, fireCount, hitCount, headshotCount, killCount, deathCount, secondsPlayed, score, lastSaved) <> (WeaponStat.tupled, WeaponStat.unapply)
    }

    sealed class WeaponStatsTimeSeries(tag : Tag) extends Table[WeaponStat](tag, "weapon_stats_time_series") with WeaponStatColumns {
      def pk = primaryKey(s"pk_$tableName", (characterId, itemId, lastSaved))
      def * = (characterId, itemId, fireCount, hitCount, headshotCount, killCount, deathCount, secondsPlayed, score, lastSaved) <> (WeaponStat.tupled, WeaponStat.unapply)
    }

    sealed class WeaponStatsIndexed(tag : Tag) extends Table[(String, Long)](tag, "weapon_stats_indexed") {
      def characterId = column[String]("character_id", O.PrimaryKey, O.DBType("VARCHAR(30)"))
      def lastIndexedOn = column[Long]("last_indexed_on", O.NotNull)

      def * = (characterId, lastIndexedOn)
    }

    sealed trait WeaponStatColumns { this: Table[WeaponStat] =>
      def characterId = column[String]("character_id", O.NotNull, O.DBType("VARCHAR(30)"))
      def itemId = column[String]("item_id", O.NotNull, O.DBType("VARCHAR(20)"))
      def fireCount = column[Long]("fire_count", O.NotNull)
      def hitCount = column[Long]("hit_count", O.NotNull)
      def headshotCount = column[Long]("headshot_count", O.NotNull)
      def killCount = column[Long]("kill_count", O.NotNull)
      def deathCount = column[Long]("death_count", O.NotNull)
      def secondsPlayed = column[Long]("seconds_played", O.NotNull)
      def score = column[Long]("score", O.NotNull)
      def lastSaved = column[Long]("last_saved", O.NotNull)
    }
  }
}
