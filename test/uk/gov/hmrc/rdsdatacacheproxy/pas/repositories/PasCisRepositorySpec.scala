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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, verify, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatest.wordspec.AnyWordSpec
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.{CreateCisAppealRequest, CreateCisAppealResponse, GetCisAppealablePenaltyRequest, GetCisAppealablePenaltyResponse}

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class PasCisRepositorySpec extends AnyWordSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: PasCisRepository = _
  var mockConnection: Connection = _
  var mockCallableStatement: CallableStatement = _
  var mockResultSet: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[Connection])
    mockCallableStatement = mock(classOf[CallableStatement])
    mockResultSet         = mock(classOf[ResultSet])

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repository = new PasCisRepository(db)
  }

  "createCisAppeal" should {

    "call create_appeal stored procedure and return CreateCisAppealResponse" in {

      when(db.withTransaction(any())).thenAnswer { invocation =>
        val func = invocation.getArgument(0, classOf[Connection => Any])
        func(mockConnection)
      }

      val createCisAppealRequest = CreateCisAppealRequest(1L)
      val createCisAppealResponse = CreateCisAppealResponse(true)

      val result = repository.createCisAppeal(createCisAppealRequest).futureValue

      result shouldBe createCisAppealResponse

      verify(mockConnection).prepareCall("{call CIS_PAS.create_appeal(?)}")
      verify(mockCallableStatement).setLong(1, createCisAppealRequest.penaltyId)
      verify(mockCallableStatement).execute()
      verify(mockCallableStatement).close()
    }
  }

  "getCisAppealablePenalty" should {

    "call get_appealable_penalty_by_id stored procedure and return GetCisAppealablePenaltyResponse" in {

      when(db.withConnection(any())).thenAnswer { invocation =>
        val func = invocation.getArgument(0, classOf[Connection => Any])
        func(mockConnection)
      }

      when(mockCallableStatement.getString(4)).thenReturn("CIS003")
      when(mockCallableStatement.getDate(5)).thenReturn(Date.valueOf(LocalDate.of(2023, 1, 1)))
      when(mockCallableStatement.getBigDecimal(6)).thenReturn(scala.math.BigDecimal(999.23).bigDecimal)
      when(mockCallableStatement.getDate(7)).thenReturn(Date.valueOf(LocalDate.of(2023, 4, 1)))

      val getCisAppealablePenaltyRequest = GetCisAppealablePenaltyRequest(
        penaltyId             = 1L,
        hmrcOfficeNumber      = "840123",
        schemeReferenceNumber = "098345"
      )
      val getCisAppealablePenaltyResponse = GetCisAppealablePenaltyResponse(
        uniqueId         = None,
        issueDate        = Some(LocalDate.of(2023, 1, 1)),
        taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
        penaltyType      = Some("CIS003"),
        penaltyAmount    = Some(999.23)
      )

      val result = repository.getCisAppealablePenalty(getCisAppealablePenaltyRequest).futureValue

      result shouldBe getCisAppealablePenaltyResponse

      verify(mockConnection).prepareCall("{call CIS_PAS.get_appealable_penalty_by_id(?, ?, ?, ?, ?, ?, ?)}")
      verify(mockCallableStatement).setString(1, getCisAppealablePenaltyRequest.hmrcOfficeNumber)
      verify(mockCallableStatement).setString(2, getCisAppealablePenaltyRequest.schemeReferenceNumber)
      verify(mockCallableStatement).setLong(3, getCisAppealablePenaltyRequest.penaltyId)
      verify(mockCallableStatement).registerOutParameter(4, java.sql.Types.VARCHAR)
      verify(mockCallableStatement).registerOutParameter(5, java.sql.Types.DATE)
      verify(mockCallableStatement).registerOutParameter(6, java.sql.Types.NUMERIC)
      verify(mockCallableStatement).registerOutParameter(7, java.sql.Types.DATE)
      verify(mockCallableStatement).execute()
      verify(mockCallableStatement).close()
    }
  }
}
