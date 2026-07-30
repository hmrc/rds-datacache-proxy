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
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{ReallocationFromAccDetails, ReallocationFromAccPeriod}

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class ReallocationFromAccPeriodRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var repository: ReallocationFromAccPeriodRepositoryImpl = _
  var db: Database = _
  var mockConnection: Connection = _
  var mockCallableStatement: CallableStatement = _
  var mockResultSet: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[Connection])
    mockCallableStatement = mock(classOf[CallableStatement])
    mockResultSet         = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repository = new ReallocationFromAccPeriodRepositoryImpl(db)

  }

  "getReallocationFromAccPeriod" should "return list of reallocationFromAccPeriod containing single reallocation from DB when stored procedure is invoked" in {

    val taxRef = 123456789L
    val accPeriod = 1L

    when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenReturn(true, false)

    when(mockResultSet.getBigDecimal("AMOUNT")).thenReturn(BigDecimal(7600.45).bigDecimal)
    when(mockResultSet.getDate("REALLOCATION_DATE")).thenReturn(Date.valueOf("2019-12-7"))
    when(mockResultSet.getDate("DESTINATION_AP_END_DATE")).thenReturn(Date.valueOf("2016-11-9"))
    when(mockResultSet.getString("DESTINATION_TAXPAYER_REFERENCE")).thenReturn("8754000057")

    val reallocationFromAccPeriod = List(
      ReallocationFromAccDetails(
        amount                       = Some(BigDecimal(7600.45)),
        reallocationDate             = Some(LocalDate.of(2019, 12, 7)),
        destinationApEndDate         = Some(LocalDate.of(2016, 11, 9)),
        destinationTaxPayerReference = Some("8754000057")
      )
    )

    val result = repository.getReallocationFromAccPeriod(taxRef, accPeriod).futureValue

    result shouldBe ReallocationFromAccPeriod(reallocation = reallocationFromAccPeriod)

    verify(mockConnection).prepareCall("{call CT_DC_PK.getReallocationsFrom(?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, 123456789L)
    verify(mockCallableStatement).setLong(2, 1L)

    verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockResultSet, times(2)).next()

    verify(mockCallableStatement).close()

  }

  "getReallocationFromAccPeriod" should "return an empty list of reallocationFromAccPeriod from DB when stored procedure is invoked and resultSet is null" in {

    val taxRef = 123456789L
    val accPeriod = 1L

    when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenReturn(false)

    val reallocationFromAccPeriodList = List.empty

    val result = repository.getReallocationFromAccPeriod(taxRef, accPeriod).futureValue

    result shouldBe ReallocationFromAccPeriod(reallocation = reallocationFromAccPeriodList)

    verify(mockConnection).prepareCall("{call CT_DC_PK.getReallocationsFrom(?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, 123456789L)
    verify(mockCallableStatement).setLong(2, 1L)

    verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockResultSet, times(1)).next()

    verify(mockCallableStatement).close()

  }

  "getReallocationFromAccPeriod" should "return lists of reallocationFromAccPeriod containing multiple reallocation from DB when stored procedure is invoked" in {
    val taxRef = 123456789L
    val accPeriod = 1L

    when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenReturn(true, true, false)

    when(mockResultSet.getBigDecimal("AMOUNT")).thenReturn(BigDecimal(7600.45).bigDecimal, BigDecimal(8700.25).bigDecimal)
    when(mockResultSet.getDate("REALLOCATION_DATE")).thenReturn(Date.valueOf("2019-12-7"), Date.valueOf("2020-12-8"))
    when(mockResultSet.getDate("DESTINATION_AP_END_DATE")).thenReturn(Date.valueOf("2016-11-9"), Date.valueOf("2020-11-9"))
    when(mockResultSet.getString("DESTINATION_TAXPAYER_REFERENCE")).thenReturn("8754000057", "9875786242")

    val reallocationFromAccPeriodList = List(
      ReallocationFromAccDetails(
        amount                       = Some(BigDecimal(7600.45)),
        reallocationDate             = Some(LocalDate.of(2019, 12, 7)),
        destinationApEndDate         = Some(LocalDate.of(2016, 11, 9)),
        destinationTaxPayerReference = Some("8754000057")
      ),
      ReallocationFromAccDetails(
        amount                       = Some(BigDecimal(8700.25)),
        reallocationDate             = Some(LocalDate.of(2020, 12, 8)),
        destinationApEndDate         = Some(LocalDate.of(2020, 11, 9)),
        destinationTaxPayerReference = Some("9875786242")
      )
    )

    val result = repository.getReallocationFromAccPeriod(taxRef, accPeriod).futureValue

    result shouldBe ReallocationFromAccPeriod(reallocation = reallocationFromAccPeriodList)

    verify(mockConnection).prepareCall("{call CT_DC_PK.getReallocationsFrom(?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, 123456789L)
    verify(mockCallableStatement).setLong(2, 1L)

    verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockResultSet, times(3)).next()

    verify(mockCallableStatement).close()
  }

  "getReallocationFromAccPeriod" should "return an exception and close the connection when an exception occurs in Downstream services" in {
    val taxRef = 123456789L
    val accPeriod = 1L

    when(mockCallableStatement.execute()).thenThrow(new RuntimeException("DB error"))

    val ex = repository.getReallocationFromAccPeriod(taxRef, accPeriod).failed.futureValue
    ex.getMessage should include("DB error")

    verify(mockCallableStatement).close()
  }

}
