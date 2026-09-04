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
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database

import java.sql.{Date, ResultSet}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.PenaltyTransaction

import java.time.LocalDate
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import java.sql.CallableStatement
import scala.concurrent.ExecutionContext.Implicits.global

class PenaltiesDatacacheRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: PenaltiesDatacacheRepositoryImpl = _
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

    repository = new PenaltiesDatacacheRepositoryImpl(db)
  }

  "getPenaltyTransactionList" should "return empty penalties list" in {

    when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
    when(rs.next()).thenReturn(false) // return empty list

    val result = repository.getPenaltyTransactionList(taxRef = 17L, accPeriod = 2L).futureValue
    result shouldBe List.empty

    verify(mockConnection).prepareCall("{call CT_DC_PK.getPenaltyTransactionList(?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, 17L)
    verify(mockCallableStatement).setLong(2, 2L)

    verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)

    verify(mockCallableStatement).execute()

    verify(rs, times(1)).next()

    verify(mockCallableStatement).close()
  }

  "getPenaltyTransactionList" should "return Penalties list with a single item" in {
    when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
    when(rs.next()).thenReturn(true, false)

    when(rs.getDate("penalty_date")).thenReturn(Date.valueOf("2025-05-01"))
    when(rs.getString("type")).thenReturn("F")
    when(rs.getBigDecimal("posting_amount")).thenReturn(scala.math.BigDecimal(100.13).bigDecimal)

    val result = repository.getPenaltyTransactionList(taxRef = 17L, accPeriod = 2L).futureValue
    result shouldBe List(PenaltyTransaction(penaltyDate = LocalDate.of(2025, 5, 1), `type` = "F", postingAmount = BigDecimal(100.13)))

    verify(mockConnection).prepareCall("{call CT_DC_PK.getPenaltyTransactionList(?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, 17L)
    verify(mockCallableStatement).setLong(2, 2L)

    verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(rs, times(2)).next()

    verify(mockCallableStatement).close()
  }

  "getPenaltyTransactionList" should "return Penalties list with two items" in {
    when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
    when(rs.next()).thenReturn(true, true, false)

    when(rs.getDate("penalty_date")).thenReturn(Date.valueOf("2025-05-01"), Date.valueOf("2021-03-07"))
    when(rs.getString("type")).thenReturn("F", "G")
    when(rs.getBigDecimal("posting_amount")).thenReturn(scala.math.BigDecimal(100.13).bigDecimal, scala.math.BigDecimal(27.19).bigDecimal)

    val result = repository.getPenaltyTransactionList(taxRef = 27L, accPeriod = 19L).futureValue
    result shouldBe List(
      PenaltyTransaction(penaltyDate = LocalDate.of(2025, 5, 1), `type` = "F", postingAmount = BigDecimal(100.13)),
      PenaltyTransaction(penaltyDate = LocalDate.of(2021, 3, 7), `type` = "G", postingAmount = BigDecimal(27.19))
    )

    verify(mockConnection).prepareCall("{call CT_DC_PK.getPenaltyTransactionList(?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, 27L)
    verify(mockCallableStatement).setLong(2, 19L)

    verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(rs, times(3)).next()

    verify(mockCallableStatement).close()
  }
}
