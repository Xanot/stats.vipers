package com.vipers.indexer.dao.slick

import com.vipers.dbms.SlickDB
import com.vipers.indexer.dao.DAOs.DAOComponent
import com.vipers.model.WithID

private[indexer] trait SlickDAOComponent extends DAOComponent { this: SlickDB =>
  import driver.simple._

  trait SlickDAO[Model <: WithID] extends DAO[Model] {
    val table : TableQuery[_]

    private lazy val genericTable = table.asInstanceOf[TableQuery[TableWithID]]

    private lazy val genericTableCompiled = Compiled(genericTable)
    private lazy val findCompiled = Compiled((id : Column[String]) => genericTable.filter(_.id === id))
    private lazy val existsCompiled = Compiled((id : Column[String]) => genericTable.filter(_.id === id).exists)

    override def find(id: String)(implicit s : Session) : Option[Model] = findCompiled(id).firstOption
    override def update(model: Model)(implicit s : Session) : Boolean = findCompiled(model.id).update(model) == 1
    override def findAll(implicit s : Session) : List[Model] = genericTableCompiled.list
    override def deleteById(id: String)(implicit s : Session) : Boolean = findCompiled(id).delete == 1
    override def create(model: Model)(implicit s : Session) : Boolean = genericTableCompiled.insert(model) == 1
    override def createAll(models : Model*)(implicit s : Session) : Boolean = genericTableCompiled.insertAll(models:_*).get == models.length
    override def exists(id : String)(implicit s : Session) : Boolean = existsCompiled(id).run

    protected abstract class TableWithID(tag : Tag, name : String) extends Table[Model](tag, name) {
      def id = column[String]("id", O.PrimaryKey, O.DBType("VARCHAR(30)"))
    }
  }
}
