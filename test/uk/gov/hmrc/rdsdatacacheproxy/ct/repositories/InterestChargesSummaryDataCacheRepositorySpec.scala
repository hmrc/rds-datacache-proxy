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
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.InterestChargesResponse

import java.sql.{CallableStatement, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class InterestChargesSummaryDataCacheRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: InterestChargeSummaryDataCacheRepositoryImpl = _
  var mockConnection: java.sql.Connection = _
  var mockCallableStatement: CallableStatement = _
  var mockResultSet: ResultSet = _

  before {
    // Mocking the database connection and callable statement
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[java.sql.Connection])
    mockCallableStatement = mock(classOf[CallableStatement])
    mockResultSet         = mock(classOf[ResultSet])

    // When db.withConnection is called, it should invoke the passed-in function and return the result
    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection) // Return the result of the lambda function passed to withConnection
    }

    // When prepareCall is invoked on the connection, return the mocked callable statement
    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    // Initialize the repository with the mocked db connection
    repository = new InterestChargeSummaryDataCacheRepositoryImpl(db)

  }

  "getInterestSummary" should "return empty InterestCharges with correct data" in {
    val interestChargesRequest = "123456789"

    val taxPayerReference = interestChargesRequest.toLong

    when(mockCallableStatement.getObject(eqTo(2), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)

    // Mocking the ResultSet to return the empty data
    when(mockResultSet.next()).thenReturn(false)

    val result = repository.getInterestSummary(interestChargesRequest).futureValue.interestCharges

    result shouldBe List.empty

    verify(mockConnection).prepareCall("{call CT_DC_PK.getInterestChargeSummary(?, ?) }")

    verify(mockCallableStatement).setLong(1, taxPayerReference)

    verify(mockCallableStatement).registerOutParameter(2, OracleTypes.CURSOR)

    verify(mockCallableStatement).execute()

    verify(mockResultSet, times(1)).next()

    verify(mockCallableStatement).close()

  }
  "getInterestSummary" should "return a List containing only one InterestCharges with correct data" in {
    val interestChargesRequest = "123456789"

    val taxPayerReference = interestChargesRequest.toLong

    when(mockCallableStatement.getObject(eqTo(2), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)

    when(mockResultSet.getBigDecimal("ACCOUNTING_PERIOD")).thenReturn(BigDecimal(12).bigDecimal)
    when(mockResultSet.getBigDecimal("INTEREST_CHARGE_SUM")).thenReturn(BigDecimal(245.67).bigDecimal)

    // Mocking the ResultSet to return the single data
    when(mockResultSet.next()).thenReturn(true, false)

    val expectedOutput = List(InterestChargesResponse(accountingPeriod = 12, interestChargeSummary = 245.67))

    val result = repository.getInterestSummary(interestChargesRequest).futureValue.interestCharges

    result shouldBe expectedOutput

    verify(mockConnection).prepareCall("{call CT_DC_PK.getInterestChargeSummary(?, ?) }")

    verify(mockCallableStatement).setLong(1, taxPayerReference)

    verify(mockCallableStatement).registerOutParameter(2, OracleTypes.CURSOR)

    verify(mockCallableStatement).execute()

    verify(mockResultSet, times(2)).next()

    verify(mockCallableStatement).close()

  }
  "getInterestSummary" should "return a List containing multiple InterestCharges with correct data" in {
    val interestChargesRequest = "123456789"

    val taxPayerReference = interestChargesRequest.toLong

    when(mockCallableStatement.getObject(eqTo(2), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)

    when(mockResultSet.getBigDecimal("ACCOUNTING_PERIOD")).thenReturn(BigDecimal(12).bigDecimal, BigDecimal(16).bigDecimal)
    when(mockResultSet.getBigDecimal("INTEREST_CHARGE_SUM")).thenReturn(BigDecimal(245.67).bigDecimal, BigDecimal(967.27).bigDecimal)

    // Mocking the ResultSet to return list containing two data
    when(mockResultSet.next()).thenReturn(true, true, false)

    val expectedOutput = List(InterestChargesResponse(accountingPeriod = 12, interestChargeSummary = 245.67),
                              InterestChargesResponse(accountingPeriod = 16, interestChargeSummary = 967.27)
                             )

    val result = repository.getInterestSummary(interestChargesRequest).futureValue.interestCharges

    result shouldBe expectedOutput

    verify(mockConnection).prepareCall("{call CT_DC_PK.getInterestChargeSummary(?, ?) }")

    verify(mockCallableStatement).setLong(1, taxPayerReference)

    verify(mockCallableStatement).registerOutParameter(2, OracleTypes.CURSOR)

    verify(mockCallableStatement).execute()

    verify(mockResultSet, times(3)).next()

    verify(mockCallableStatement).close()

  }

}
