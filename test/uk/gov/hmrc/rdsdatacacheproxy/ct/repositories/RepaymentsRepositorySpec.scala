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
import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.RepaymentsHelper

import java.sql.{CallableStatement, Date, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class RepaymentsRepositorySpec extends AnyFreeSpec with Matchers with BeforeAndAfter with RepaymentsHelper {

  var db: Database = _
  var repo: RepaymentsRepositoryImpl = _
  var mockConnection: java.sql.Connection = _
  var mockCallableStatement: CallableStatement = _
  var rs: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[java.sql.Connection])
    mockCallableStatement = mock(classOf[CallableStatement])
    rs                    = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repo = new RepaymentsRepositoryImpl(db)
  }

  "getRepayments" - {
    "return empty repayments" in {
      when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenReturn(false)

      when(rs.getBigDecimal("amount")).thenReturn(scala.math.BigDecimal(100).bigDecimal)
      when(rs.getString("repayment_type")).thenReturn("F", "G")
      when(rs.getDate("repayment_date")).thenReturn(Date.valueOf("2025-05-01"), Date.valueOf("2021-03-07"))

      val result = repo.getRepayments(taxRef = 2L, accPeriod = 3L).futureValue
      result shouldBe emptyRepayments

      verify(mockConnection).prepareCall("{call CT_DC_PK.getRepayments(?, ?, ?)}")

      verify(mockCallableStatement).setLong(1, 2L)
      verify(mockCallableStatement).setLong(2, 3L)

      verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
      verify(mockCallableStatement).execute()

      verify(rs, times(1)).next()

      verify(mockCallableStatement).close()
    }

    "return repayments with one item" in {
      when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenReturn(true, false)

      when(rs.getBigDecimal("amount")).thenReturn(scala.math.BigDecimal(10).bigDecimal)
      when(rs.getString("repayment_type")).thenReturn("S")
      when(rs.getDate("repayment_date")).thenReturn(Date.valueOf("2026-07-24"))

      val result = repo.getRepayments(taxRef = 4L, accPeriod = 2L).futureValue
      result shouldBe repaymentsWithOneItem

      verify(mockConnection).prepareCall("{call CT_DC_PK.getRepayments(?, ?, ?)}")

      verify(mockCallableStatement).setLong(1, 4L)
      verify(mockCallableStatement).setLong(2, 2L)

      verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
      verify(mockCallableStatement).execute()

      verify(rs, times(2)).next()

      verify(mockCallableStatement).close()
    }

    "return repayments with multiple items" in {
      when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenReturn(true, true, false)

      when(rs.getBigDecimal("amount")).thenReturn(
        scala.math.BigDecimal(20).bigDecimal,
        scala.math.BigDecimal(30).bigDecimal
      )
      when(rs.getString("repayment_type")).thenReturn("S", "T")
      when(rs.getDate("repayment_date")).thenReturn(Date.valueOf("2027-07-24"), Date.valueOf("2028-07-24"))

      val result = repo.getRepayments(taxRef = 4L, accPeriod = 2L).futureValue
      result shouldBe repaymentsWithMultipleItems

      verify(mockConnection).prepareCall("{call CT_DC_PK.getRepayments(?, ?, ?)}")

      verify(mockCallableStatement).setLong(1, 4L)
      verify(mockCallableStatement).setLong(2, 2L)

      verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
      verify(mockCallableStatement).execute()

      verify(rs, times(3)).next()

      verify(mockCallableStatement).close()
    }

    "return an exception and close the connection" in {
      when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenThrow(new RuntimeException("DB error"))

      val ex = repo.getRepayments(1L, 2L).failed.futureValue
      ex.getMessage should include("DB error")

      verify(mockCallableStatement).close()
    }
  }
}
