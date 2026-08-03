/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories

import oracle.jdbc.OracleTypes
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, times, verify, when}
import org.mockito.ArgumentMatchers.eq as eqTo
import org.scalatest.BeforeAndAfter
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database

import java.sql.ResultSet
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.AdjustmentTransactions

import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import java.sql.CallableStatement
import scala.concurrent.ExecutionContext.Implicits.global

class AdjustmentTransactionsRepositorySpec extends AnyFreeSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repo: AdjustmentTransactionsRepositoryImpl = _
  var mockConnection: java.sql.Connection = _
  var mockCallableStatement: CallableStatement = _
  var rs: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[java.sql.Connection])
    mockCallableStatement = mock(classOf[CallableStatement])

    rs = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repo = new AdjustmentTransactionsRepositoryImpl(db)
  }

  "getAdjustmentTransactions" - {
    "return an empty adjustment transaction list" in {

      when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenReturn(false)

      val result = repo.getAdjustmentTransactions(taxRef = 2L, accPeriod = 3L).futureValue
      result shouldBe List.empty

      verify(mockConnection).prepareCall("{call CT_DC_PK.getAdjustmentTransactionList(?, ?, ?)}")

      verify(mockCallableStatement).setLong(1, 2L)
      verify(mockCallableStatement).setLong(2, 3L)

      verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
      verify(mockCallableStatement).execute()

      verify(rs, times(1)).next()

      verify(mockCallableStatement).close()
    }

    "return an adjustment transactions list with a single item" in {
      when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenReturn(true, false)

      when(rs.getBigDecimal("amount")).thenReturn(scala.math.BigDecimal(50.00).bigDecimal)
      when(rs.getString("type")).thenReturn("N")

      val result = repo.getAdjustmentTransactions(taxRef = 2L, accPeriod = 3L).futureValue
      result shouldBe List(AdjustmentTransactions(amount = BigDecimal(50.00), `type` = "N"))

      verify(mockConnection).prepareCall("{call CT_DC_PK.getAdjustmentTransactionList(?, ?, ?)}")

      verify(mockCallableStatement).setLong(1, 2L)
      verify(mockCallableStatement).setLong(2, 3L)

      verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
      verify(mockCallableStatement).execute()

      verify(rs, times(2)).next()

      verify(mockCallableStatement).close()
    }

    "return adjustment transactions list with two items" in {
      when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenReturn(true, true, false)

      when(rs.getBigDecimal("amount")).thenReturn(scala.math.BigDecimal(50.00).bigDecimal, scala.math.BigDecimal(70.00).bigDecimal)
      when(rs.getString("type")).thenReturn("N", "F")

      val result = repo.getAdjustmentTransactions(taxRef = 2L, accPeriod = 3L).futureValue
      result shouldBe List(
        AdjustmentTransactions(amount = BigDecimal(50.00), `type` = "N"),
        AdjustmentTransactions(amount = BigDecimal(70.00), `type` = "F")
      )

      verify(mockConnection).prepareCall("{call CT_DC_PK.getAdjustmentTransactionList(?, ?, ?)}")

      verify(mockCallableStatement).setLong(1, 2L)
      verify(mockCallableStatement).setLong(2, 3L)

      verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
      verify(mockCallableStatement).execute()

      verify(rs, times(3)).next()

      verify(mockCallableStatement).close()
    }
  }
}
