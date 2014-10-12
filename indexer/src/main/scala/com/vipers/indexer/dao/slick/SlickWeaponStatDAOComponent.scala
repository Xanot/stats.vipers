package com.vipers.indexer.dao.slick

import com.vipers.dbms.SlickDB
import com.vipers.indexer.dao.DAOs.WeaponStatDAOComponent
import com.vipers.model.{Weapon, WeaponStat}

private[indexer] trait SlickWeaponStatDAOComponent extends WeaponStatDAOComponent { this: SlickDB with SlickWeaponDAOComponent =>
  import driver.simple._

  override lazy val weaponStatDAO = new SlickWeaponStatDAO

  sealed class SlickWeaponStatDAO extends WeaponStatDAO {
    val weaponStatsTable = TableQuery[WeaponStats]
    private lazy val weaponStatsTableCompiled = Compiled(weaponStatsTable)

    val weaponStatsTimeSeriesTable = TableQuery[WeaponStatsTimeSeries]
    private lazy val weaponStatsTimeSeriesTableCompiled = Compiled(weaponStatsTimeSeriesTable)

    private lazy val deleteCharactersStatsCompiled = Compiled((characterId : Column[String]) => weaponStatsTable.filter(_.characterId === characterId))
    override def deleteCharactersStats(characterId : String)(implicit s : Session) = deleteCharactersStatsCompiled(characterId).delete > 0

    override def createAll(weaponStats : WeaponStat*)(implicit s : Session) = {
      weaponStatsTableCompiled.insertAll(weaponStats:_*)
      try {
        weaponStatsTimeSeriesTableCompiled.insertAll(weaponStats:_*)
      } catch {
        case _ : Exception => // Ignore primary key collisions
      }
    }

    private lazy val getCharactersMostRecentWeaponStatsCompiled = Compiled((characterId : Column[String]) => {
      for {
        weaponStat <- weaponStatsTable if weaponStat.characterId === characterId
        weapon <- weaponDAO.table if weapon.id === weaponStat.itemId
      } yield (weaponStat, weapon)
    })
    override def getCharactersMostRecentWeaponStats(characterId : String)(implicit s : Session) : List[(WeaponStat, Weapon)] = {
      getCharactersMostRecentWeaponStatsCompiled(characterId).list
    }

    private lazy val getCharactersWeaponStatsLastIndexedOnCompiled = Compiled((characterId : Column[String]) => {
      weaponStatsTable.filter(r => r.characterId === characterId).map(_.lastIndexedOn)
    })
    override def getCharactersWeaponStatsLastIndexedOn(characterId : String)(implicit s : Session) : Option[Long] = {
      getCharactersWeaponStatsLastIndexedOnCompiled(characterId).firstOption
    }

    private lazy val getCharactersWeaponProgressCompiled = Compiled((characterId : Column[String], weaponId : Column[String]) => {
      weaponStatsTimeSeriesTable.filter(r => r.characterId === characterId && r.itemId === weaponId).sortBy(_.lastSaved.asc)
    })
    override def getCharactersWeaponStatHistory(characterId : String, itemId : String)(implicit s : Session) : List[WeaponStat] = {
      getCharactersWeaponProgressCompiled(characterId, itemId).list
    }

    sealed class WeaponStats(tag : Tag) extends Table[WeaponStat](tag, "weapon_stats") with WeaponStatColumns {
      def pk = primaryKey(s"pk_$tableName", (characterId, itemId))
      def * = (characterId, itemId, fireCount, hitCount, headshotCount, killCount, deathCount, secondsPlayed, score, lastSaved, lastIndexedOn.?) <> (WeaponStat.tupled, WeaponStat.unapply)
    }

    sealed class WeaponStatsTimeSeries(tag : Tag) extends Table[WeaponStat](tag, "weapon_stats_time_series") with WeaponStatColumns {
      def pk = primaryKey(s"pk_$tableName", (characterId, itemId, lastSaved))
      // Time series do not need lastIndexedOn
      def * = (characterId, itemId, fireCount, hitCount, headshotCount, killCount, deathCount, secondsPlayed, score, lastSaved, lastIndexedOn.?).shaped <> (
        {
          case (characterId, itemId, fireCount, hitCount, headshotCount, killCount, deathCount, secondsPlayed, score, lastSaved, _) =>
            WeaponStat(characterId, itemId, fireCount, hitCount, headshotCount, killCount, deathCount, secondsPlayed, score, lastSaved, None)
        }, { s: WeaponStat => s match {
          case WeaponStat(characterId, itemId, fireCount, hitCount, headshotCount, killCount, deathCount, secondsPlayed, score, lastSaved, _) =>
            Some((characterId, itemId, fireCount, hitCount, headshotCount, killCount, deathCount, secondsPlayed, score, lastSaved, None))
        }}
      )
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
      def lastIndexedOn = column[Long]("last_indexed_on", O.Nullable)
    }
  }
}
