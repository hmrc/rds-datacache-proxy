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
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.ReallocationRow

import java.sql.{CallableStatement, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class ReallocationDatacacheRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: ReallocationDatacacheRepositoryImpl = _
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

    repository = new ReallocationDatacacheRepositoryImpl(db)
  }

  "getReallocationsTo" should "return empty reallocations list" in {

    when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
    when(rs.next()).thenReturn(false)

    val result = repository.getByAccountingPeriod(taxRef = 17L, accPeriod = 2L).futureValue
    result shouldBe List.empty

    verify(mockConnection).prepareCall("{call CT_DC_PK.getReallocationsTo(?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, 17L)
    verify(mockCallableStatement).setLong(2, 2L)

    verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)

    verify(mockCallableStatement).execute()

    verify(rs, times(1)).next()

    verify(mockCallableStatement).close()
  }

  "getReallocationsTo" should "return Reallocations list with a single item" in {
    when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
    when(rs.next()).thenReturn(true, false)

    when(rs.getDate("reallocation_date")).thenReturn(Date.valueOf("2025-05-01"))
    when(rs.getDate("source_ap_end_date")).thenReturn(Date.valueOf("2026-07-01"))
    when(rs.getString("source_taxpayer_reference")).thenReturn("9369369363")
    when(rs.getBigDecimal("AMOUNT")).thenReturn(scala.math.BigDecimal(117.01).bigDecimal)

    val result = repository.getByAccountingPeriod(taxRef = 17L, accPeriod = 2L).futureValue
    result shouldBe List(
      ReallocationRow(
        amount                  = BigDecimal(117.01),
        reallocationDate        = LocalDate.of(2025, 5, 1),
        sourceApEndDate         = LocalDate.of(2026, 7, 1),
        sourceTaxpayerReference = "9369369363"
      )
    )

    verify(mockConnection).prepareCall("{call CT_DC_PK.getReallocationsTo(?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, 17L)
    verify(mockCallableStatement).setLong(2, 2L)

    verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(rs, times(2)).next()

    verify(mockCallableStatement).close()
  }

  "getReallocationsTo" should "return Reallocations list with two items" in {
    when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
    when(rs.next()).thenReturn(true, true, false)

    when(rs.getDate("reallocation_date")).thenReturn(Date.valueOf("2025-05-01"), Date.valueOf("2021-05-01"))
    when(rs.getDate("source_ap_end_date")).thenReturn(Date.valueOf("2026-07-01"), Date.valueOf("2027-07-01"))
    when(rs.getString("source_taxpayer_reference")).thenReturn("9369369363", "1369369361")
    when(rs.getBigDecimal("AMOUNT")).thenReturn(scala.math.BigDecimal(117.01).bigDecimal, scala.math.BigDecimal(23.91).bigDecimal)

    val result = repository.getByAccountingPeriod(taxRef = 17L, accPeriod = 2L).futureValue
    result shouldBe List(
      ReallocationRow(
        amount                  = BigDecimal(117.01),
        reallocationDate        = LocalDate.of(2025, 5, 1),
        sourceApEndDate         = LocalDate.of(2026, 7, 1),
        sourceTaxpayerReference = "9369369363"
      ),
      ReallocationRow(
        amount                  = BigDecimal(23.91),
        reallocationDate        = LocalDate.of(2021, 5, 1),
        sourceApEndDate         = LocalDate.of(2027, 7, 1),
        sourceTaxpayerReference = "1369369361"
      )
    )

    verify(mockConnection).prepareCall("{call CT_DC_PK.getReallocationsTo(?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, 17L)
    verify(mockCallableStatement).setLong(2, 2L)

    verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(rs, times(3)).next()

    verify(mockCallableStatement).close()
  }

}
