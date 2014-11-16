package com.vipers.indexer.dao.slick

import com.vipers.dbms.SlickDB
import com.vipers.indexer.dao.DAOs.GameDataIndexedOnDAOComponent
import com.vipers.model.DatabaseModels.GameDataIndexedOn

private[indexer] trait SlickGameDataIndexedOnDAOComponent extends SlickDAOComponent with GameDataIndexedOnDAOComponent { this: SlickDB =>
  import driver.simple._

  override lazy val gameDataIndexedOnDAO = new SlickGameDataIndexedOnDAO

  sealed class SlickGameDataIndexedOnDAO extends SlickDAO[GameDataIndexedOn] with GameDataIndexedOnDAO {
    override val table = TableQuery[GameDataIndexedOnTable]

    sealed class GameDataIndexedOnTable(tag : Tag) extends TableWithID(tag, "game_data_indexed_on") {
      def lastIndexedOn = column[Long]("last_indexed_on", O.NotNull)
      def * = (id, lastIndexedOn) <> (GameDataIndexedOn.tupled, GameDataIndexedOn.unapply)
    }
  }
}
