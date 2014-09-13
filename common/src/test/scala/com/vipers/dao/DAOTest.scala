package com.vipers.dao

import com.vipers.Test
import com.vipers.dbms.DB
import org.scalatest.Suite

trait DAOTest extends Test { this: DB with Suite => }
