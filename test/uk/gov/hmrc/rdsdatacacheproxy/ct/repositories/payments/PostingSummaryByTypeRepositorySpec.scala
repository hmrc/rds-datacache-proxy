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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.payments

import oracle.jdbc.OracleTypes
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.payments.PostingSummaryByTypeStubData
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.PostingSummaryByTypeResponse

import java.sql.{CallableStatement, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class PostingSummaryByTypeRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter with PostingSummaryByTypeStubData {

  var db: Database = _
  var repository: PostingSummaryByTypeRepositoryImpl = _
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

    repository = new PostingSummaryByTypeRepositoryImpl(db)
  }

  "getPostingSummaryByType" should "return an empty PostingSummaryByTypeResponse when resultSet is empty" in {
    when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
    when(rs.next()).thenReturn(false)

    val result = repository.getPostingSummaryByType(2, "SAM").futureValue

    result shouldBe emptyPostingSummaryByTypeResponses

    verify(mockConnection).prepareCall("{call CT_DC_PK.getPostingSummaryByType(?,?,?)}")

    verify(mockCallableStatement).setLong(1, 2L)
    verify(mockCallableStatement).setString(2, "SAM")

    verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()
    verify(mockCallableStatement).close()
  }

  "getPostingSummaryByType" should "return PostingSummaryByTypeResponse with multiple results for a given taxPayerReference" in {
    when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
    when(rs.next()).thenReturn(true, true, true, false)

    when(rs.getBigDecimal("ACCOUNTING_PERIOD")).thenReturn(BigDecimal(1).bigDecimal, BigDecimal(2).bigDecimal, BigDecimal(3).bigDecimal)
    when(rs.getBigDecimal("SETTLEMENT_SEQUENCE_NO")).thenReturn(BigDecimal(1).bigDecimal, BigDecimal(2).bigDecimal, BigDecimal(3).bigDecimal)
    when(rs.getBigDecimal("POSTING_AMOUNT_SUM")).thenReturn(BigDecimal(123456).bigDecimal, BigDecimal(456789).bigDecimal, BigDecimal(159159).bigDecimal)

    val result = repository.getPostingSummaryByType(1, "PAF").futureValue

    result shouldBe postingSummaryByTypeRowMultiple

    verify(mockConnection).prepareCall("{call CT_DC_PK.getPostingSummaryByType(?,?,?)}")

    verify(mockCallableStatement).setLong(1, 1L)
    verify(mockCallableStatement).setString(2, "PAF")

    verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(rs, times(4)).next()
    verify(mockCallableStatement).close()
  }

  "getPostingSummaryByType" should "throw exception and close the connection when exception is returned from DB" in {
    when(mockCallableStatement.execute()).thenThrow(new RuntimeException("Boom"))

    val ex = repository.getPostingSummaryByType(3, "SAM").failed.futureValue

    ex.getMessage should include("Boom")

    verify(mockCallableStatement).close()
  }

}
