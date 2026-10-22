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

package uk.gov.hmrc.rdsdatacacheproxy.pas.repositories

import oracle.jdbc.OracleTypes
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatest.wordspec.AnyWordSpec
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.*

import java.sql.{CallableStatement, Connection, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class PasPayeRepositorySpec extends AnyWordSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: PasPayeRepository = _
  var mockConnection: Connection = _
  var mockCallableStatement: CallableStatement = _
  var mockResultSet: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[Connection])
    mockCallableStatement = mock(classOf[CallableStatement])
    mockResultSet         = mock(classOf[ResultSet])

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repository = new PasPayeRepository(db)
  }

  "getPayeAppealablePenalties" should {

    val testRequest = GetAppealablePenaltiesRequest(
      hmrcOfficeNumber      = "123",
      schemeReferenceNumber = "AB456",
      startRow              = 1L,
      endRow                = 10L
    )

    "call get_defaults_by_emp_ref stored procedure and return GetAppealablePenaltiesResponse with empty list" in {

      when(db.withConnection(any())).thenAnswer { invocation =>
        val func = invocation.getArgument(0, classOf[Connection => Any])
        func(mockConnection)
      }

      val expectedResponse = GetAppealablePenaltiesResponse(
        total               = 0L,
        appealablePenalties = List.empty
      )

      when(mockCallableStatement.getObject(eqTo(8), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
      when(mockResultSet.next()).thenReturn(false)

      val result = repository.getPayeAppealablePenalties(testRequest).futureValue

      when(mockResultSet.getLong("total")).thenReturn(0L)

      result shouldBe expectedResponse

      verify(mockConnection).prepareCall("{call PAYE_PAS.get_defaults_by_emp_ref(?, ?, ?, ?, ?, ?, ?, ?)}")
      verify(mockCallableStatement).setString(1, testRequest.hmrcOfficeNumber)
      verify(mockCallableStatement).setString(2, testRequest.schemeReferenceNumber)
      verify(mockCallableStatement).setLong(3, testRequest.startRow)
      verify(mockCallableStatement).setLong(4, testRequest.endRow)
      verify(mockCallableStatement).setString(5, "unique_id")
      verify(mockCallableStatement).setString(6, "desc")
      verify(mockCallableStatement).registerOutParameter(7, OracleTypes.NUMERIC)
      verify(mockCallableStatement).registerOutParameter(8, OracleTypes.CURSOR)
      verify(mockCallableStatement).execute()
      verify(mockResultSet, times(1)).next()
      verify(mockCallableStatement).close()
    }

    "call get_defaults_by_emp_ref stored procedure and return GetAppealablePenaltiesResponse with two items" in {

      when(db.withConnection(any())).thenAnswer { invocation =>
        val func = invocation.getArgument(0, classOf[Connection => Any])
        func(mockConnection)
      }

      val expectedResponse = GetAppealablePenaltiesResponse(
        total = 2,
        appealablePenalties = List(
          GetAppealablePenaltyResponse(
            uniqueId         = Some(10001),
            issueDate        = Some(LocalDate.of(2023, 1, 1)),
            taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
            penaltyType      = Some("F"),
            penaltyAmount    = Some(BigDecimal(999.23))
          ),
          GetAppealablePenaltyResponse(
            uniqueId         = Some(10002),
            issueDate        = Some(LocalDate.of(2023, 1, 1)),
            taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
            penaltyType      = Some("F"),
            penaltyAmount    = Some(BigDecimal(999.23))
          )
        )
      )

      when(mockCallableStatement.getObject(eqTo(8), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
      when(mockResultSet.next()).thenReturn(true, true, false)

      when(mockCallableStatement.getLong(7)).thenReturn(2L)
      when(mockResultSet.getLong("UNIQUE_ID")).thenReturn(10001L, 10002L)
      when(mockResultSet.getDate("ISSUE_DATE")).thenReturn(java.sql.Date.valueOf("2023-01-01"))
      when(mockResultSet.getDate("TAX_PERIOD_END_DATE")).thenReturn(java.sql.Date.valueOf("2023-04-01"))
      when(mockResultSet.getString("DEFAULT_TYPE")).thenReturn("F")
      when(mockResultSet.getBigDecimal("PENALTY_AMOUNT")).thenReturn(new java.math.BigDecimal("999.23"))

      val result = repository.getPayeAppealablePenalties(testRequest).futureValue

      result shouldBe expectedResponse

      verify(mockConnection).prepareCall("{call PAYE_PAS.get_defaults_by_emp_ref(?, ?, ?, ?, ?, ?, ?, ?)}")
      verify(mockCallableStatement).setString(1, testRequest.hmrcOfficeNumber)
      verify(mockCallableStatement).setString(2, testRequest.schemeReferenceNumber)
      verify(mockCallableStatement).setLong(3, testRequest.startRow)
      verify(mockCallableStatement).setLong(4, testRequest.endRow)
      verify(mockCallableStatement).setString(5, "unique_id")
      verify(mockCallableStatement).setString(6, "desc")
      verify(mockCallableStatement).registerOutParameter(7, OracleTypes.NUMERIC)
      verify(mockCallableStatement).registerOutParameter(8, OracleTypes.CURSOR)
      verify(mockCallableStatement).execute()
      verify(mockResultSet, times(3)).next()
      verify(mockCallableStatement).close()
    }
  }
}
