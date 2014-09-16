package com.vipers.fetcher.util

import com.vipers.model.Sort
import Sort.{Order, ASC, DESC}
import com.vipers.fetcher.util.CensusQuery.{Search, CensusQueryCommand}

private[fetcher] sealed case class CensusQuery(search : Option[Search], commands : CensusQueryCommand*) {
  def construct : Seq[(String, String)] = search.map(s => Seq(s.field -> s.value)).getOrElse(Nil) ++ commands.map(_.construct)
  def ++(r : CensusQuery) : CensusQuery = CensusQuery(search, commands ++ r.commands :_*)
  def ++(r : Option[CensusQuery]) : CensusQuery = r.map(q => this ++ q).getOrElse(this)
  def +(r : CensusQueryCommand) : CensusQuery = CensusQuery(search, commands :+ r :_*)
  def +(r : Option[CensusQueryCommand]) : CensusQuery = r.map(c => this + c).getOrElse(this)
}

private[fetcher] object CensusQuery {
  sealed case class Search(field : String, value : String)

  /**
   * SOE Census query command
   * @param command command keys such as "limit", "start", etc.
   */
  sealed abstract class CensusQueryCommand(command : String) {
    def construct : (String, String) = "c:" + command -> renderValue()
    protected def renderValue() : String
  }

  /**
   * a command which is just a KV pair such as c:lang=en
   * @param command command keys such as "limit", "start", etc.
   * @param value command value
   * @tparam T type of the command value
   */
  sealed abstract class SimpleCensusQueryCommand[T](command : String, value : T) extends CensusQueryCommand(command) {
    override protected def renderValue(): String = value.toString
  }

  /**
   * a command which has a comma separated value such as c:show=field1,field2
   * @param command command keys such as "limit", "start", etc.
   * @tparam T type to be comma separated
   */
  sealed abstract class CommaSeparatedCensusQueryCommand[T](command : String) extends CensusQueryCommand(command) {
    protected val values : Seq[T]
    override protected def renderValue() : String = values.mkString(",")
  }

  object CensusQueryCommand {
    //================================================================================
    // Commands
    //================================================================================
    case class Case(protected val value : Boolean) extends SimpleCensusQueryCommand("case", value)
    case class Limit(protected val value : Int) extends SimpleCensusQueryCommand("limit", value)
    case class LimitPerDB(protected val value : Int) extends SimpleCensusQueryCommand("limitPerDB", value)
    case class Start(protected val value : Int) extends SimpleCensusQueryCommand("start", value)
    case class IncludeNull(protected val value : Boolean) extends SimpleCensusQueryCommand("includeNull", value)
    case class Lang(protected val value : String) extends SimpleCensusQueryCommand("lang", value)
    case class Timing(protected val value : Boolean) extends SimpleCensusQueryCommand("timing", value)

    case class Resolve(protected val values : String*) extends CommaSeparatedCensusQueryCommand[String]("resolve")
    case class Hide(protected val values : String*) extends CommaSeparatedCensusQueryCommand[String]("hide")
    case class Show(protected val values : String*) extends CommaSeparatedCensusQueryCommand[String]("show")
    case class Has(protected val values : String*) extends CommaSeparatedCensusQueryCommand[String]("has")

    case class Sort(protected val pairs : (String ,Order)*) extends CommaSeparatedCensusQueryCommand[String]("sort") {
      override protected val values =  pairs.map { case (field, order) =>
        val o = order match {
          case DESC => -1
          case ASC => 1
        }
        s"$field:$o"
      }
    }

    case class Join(protected val values : JoinQuery*) extends CommaSeparatedCensusQueryCommand[JoinQuery]("join")
    case class Tree(protected val value : TreeQuery) extends SimpleCensusQueryCommand("tree", value)

    //================================================================================
    // Tree
    //================================================================================
    sealed abstract class TreeQuery() { ??? }

    //================================================================================
    // Join
    //================================================================================
    abstract class JoinQuery(collection : String,
                        injectAt : String,
                        isList : Option[Boolean] = None,
                        on : Option[String] = None,
                        to : Option[String] = None,
                        terms : Option[Seq[(String, String)]] = None,
                        hide : Option[Seq[String]] = None,
                        show : Option[Seq[String]] = None,
                        nested : Option[JoinQuery] = None,
                        isOuter: Option[Boolean] = None) {

      override def toString : String = {
        val builder = new StringBuilder
        builder ++= collection + s"^inject_at:$injectAt"
        isList.map { isList => if(isList) builder ++= "^list:1" else builder ++= "^list:0" }
        on.map { on => builder ++= s"^on:$on"}
        to.map { to => builder ++= s"^to:$to"}
        terms.map { seq =>
          if(seq.nonEmpty) {
            builder ++= "^terms:"
            val last = seq.last
            seq.foreach { term =>
              builder ++= s"${term._1}=${term._2}"
              if(term != last)
                builder ++= "'"
            }
          }
        }
        hide.map { seq =>
          if(seq.nonEmpty) {
            builder ++= "^hide:"
            builder ++= seq.mkString("'")
          }
        }
        show.map { seq =>
          if(seq.nonEmpty) {
            builder ++= "^show:"
            builder ++= seq.mkString("'")
          }
        }
        isOuter.map { isOuter => if(isOuter) builder ++= "^outer:1" else builder ++= "^outer:0"}
        nested.map { nested => builder ++= s"(${nested.toString})"}
        builder.toString()
      }
    }

    case class OutfitMemberJoin(injectAt : String = "members",
                                on : Option[String] = None,
                                to : Option[String] = None,
                                terms : Option[Seq[(String, String)]] = None,
                                hide : Option[Seq[String]] = None,
                                show : Option[Seq[String]] = None,
                                nested : Option[JoinQuery] = None,
                                isOuter : Option[Boolean] = None) extends JoinQuery("outfit_member", injectAt, Some(true), on, to, terms, hide, show ,nested, isOuter)

    case class CharacterJoin(injectAt : String = "character",
                             on : Option[String] = None,
                             to : Option[String] = None,
                             terms : Option[Seq[(String, String)]] = None,
                             hide : Option[Seq[String]] = None,
                             show : Option[Seq[String]] = None,
                             nested : Option[JoinQuery] = None,
                             isOuter : Option[Boolean] = None) extends JoinQuery("character", injectAt, None, on, to, terms, hide, show ,nested, isOuter)

    case class FactionJoin(injectAt : String = "faction",
                           terms : Option[Seq[(String, String)]] = None,
                           hide : Option[Seq[String]] = None,
                           show : Option[Seq[String]] = None,
                           nested : Option[JoinQuery] = None,
                           isOuter : Option[Boolean] = None) extends JoinQuery("faction", injectAt, None, None, None, terms, hide, show ,nested, isOuter)
  }
}
