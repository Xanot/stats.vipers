package com.vipers.indexer.dao.slick

import com.vipers.dbms.SlickDB
import com.vipers.fetcher.FetcherActor.OutfitMember
import com.vipers.indexer.dao.DAOs.OutfitMembershipDAOComponent
import com.vipers.model.DatabaseModels.OutfitMembership

private[indexer] trait SlickOutfitMembershipDAOComponent extends OutfitMembershipDAOComponent  with SlickDAOComponent {
  this: SlickDB with SlickCharacterDAOComponent =>

  import driver.simple._

  override lazy val outfitMembershipDAO = new SlickOutfitMembershipDAO

  sealed class SlickOutfitMembershipDAO extends OutfitMembershipDAO with SlickDAO[OutfitMembership] {
    val table = TableQuery[OutfitMemberships]

    private val findAllCharactersByOutfitIdCompiled = Compiled((outfitId : Column[String]) => {
      for {
        membership <- table if membership.outfitId === outfitId
        character <- characterDAO.table if character.id === membership.id
      } yield (character, membership)
    })
    override def findAllCharactersByOutfitId(outfitId: String)(implicit s : Session) : List[OutfitMember] = findAllCharactersByOutfitIdCompiled(outfitId).list

    private val deleteAllMembershipsByOutfitIdCompiled = Compiled((outfitId : Column[String]) => { table.filter(_.outfitId === outfitId) })
    override def deleteAllByOutfitId(outfitId : String)(implicit s : Session) : Boolean = deleteAllMembershipsByOutfitIdCompiled(outfitId).delete > 0

    sealed class OutfitMemberships(tag : Tag) extends TableWithID(tag, "outfit_member") {
      def outfitId = column[String]("outfit_id", O.NotNull, O.DBType("VARCHAR(30)"))
      def rank = column[String]("rank", O.NotNull, O.DBType("VARCHAR(100)"))
      def rankOrdinal = column[Byte]("rank_ordinal", O.NotNull)
      def memberSinceDate = column[Long]("member_since_date", O.NotNull)

      def * = (outfitId, id, rank, rankOrdinal, memberSinceDate) <> (OutfitMembership.tupled, OutfitMembership.unapply)
    }
  }
}
