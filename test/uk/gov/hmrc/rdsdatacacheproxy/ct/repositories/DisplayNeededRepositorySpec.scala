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

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfter, concurrent}
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.DisplayNeeded

import java.sql.{CallableStatement, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class DisplayNeededRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: DisplayNeededImpl = _
  var mockConnection: java.sql.Connection = _
  var mockCallableStatement: CallableStatement = _
  var mockResultSet: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[java.sql.Connection])
    mockCallableStatement = mock(classOf[CallableStatement])
    mockResultSet         = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repository = new DisplayNeededImpl(db)
  }

  "getDisplayNeeded" should "return Display Needed with all flags set to false" in {
    val taxPayerRef: Long = 10L
    val accountingPeriod: Long = 1L

    val expectedOutput = DisplayNeeded(
      taxIsDisplayNeededFlag          = false,
      interestIsDisplayNeededFlag     = false,
      paymentIsDisplayNeededFlag      = false,
      repayReallocIsDisplayNeededFlag = false
    )

    val result = repository.getDisplayNeeded(taxPayerRef, accountingPeriod).futureValue

    result shouldBe expectedOutput

    verify(mockConnection).prepareCall("{call CT_DC_PK.isDisplayNeeded(?, ?, ?, ?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, taxPayerRef)
    verify(mockCallableStatement).setLong(2, accountingPeriod)

    verify(mockCallableStatement).registerOutParameter(3, java.sql.Types.VARCHAR) // P_TAX_ISDISPLAYNEEDED_FLAG
    verify(mockCallableStatement).registerOutParameter(4, java.sql.Types.VARCHAR) // P_INT_ISDISPLAYNEEDED_FLAG
    verify(mockCallableStatement).registerOutParameter(5, java.sql.Types.VARCHAR) // P_PAY_ISDISPLAYNEEDED_FLAG
    verify(mockCallableStatement).registerOutParameter(6, java.sql.Types.VARCHAR) // P_REPAY_ISDISPLAYNEEDED_FLAG

    verify(mockCallableStatement).execute()

    verify(mockCallableStatement).close()
  }

  "getDisplayNeeded" should "return Display Needed with all flags set to true" in {
    val taxPayerRef: Long = 20L
    val accountingPeriod: Long = 1L

    val expectedOutput = DisplayNeeded(
      taxIsDisplayNeededFlag          = true,
      interestIsDisplayNeededFlag     = true,
      paymentIsDisplayNeededFlag      = true,
      repayReallocIsDisplayNeededFlag = true
    )

    val result = repository.getDisplayNeeded(taxPayerRef, accountingPeriod).futureValue

    result shouldBe expectedOutput

    verify(mockConnection).prepareCall("{call CT_DC_PK.isDisplayNeeded(?, ?, ?, ?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, taxPayerRef)
    verify(mockCallableStatement).setLong(2, accountingPeriod)

    verify(mockCallableStatement).registerOutParameter(3, oracle.jdbc.OracleTypes.VARCHAR) // P_TAX_ISDISPLAYNEEDED_FLAG
    verify(mockCallableStatement).registerOutParameter(4, oracle.jdbc.OracleTypes.VARCHAR) // P_INT_ISDISPLAYNEEDED_FLAG
    verify(mockCallableStatement).registerOutParameter(5, oracle.jdbc.OracleTypes.VARCHAR) // P_PAY_ISDISPLAYNEEDED_FLAG
    verify(mockCallableStatement).registerOutParameter(6, oracle.jdbc.OracleTypes.VARCHAR) // P_REPAY_ISDISPLAYNEEDED_FLAG

    verify(mockCallableStatement).execute()

    verify(mockCallableStatement).close()
  }

  "getDisplayNeeded" should "return Display Needed with some flags set to true or false" in {
    val taxPayerRef: Long = 30L
    val accountingPeriod: Long = 1L

    val expectedOutput = DisplayNeeded(
      taxIsDisplayNeededFlag          = true,
      interestIsDisplayNeededFlag     = false,
      paymentIsDisplayNeededFlag      = true,
      repayReallocIsDisplayNeededFlag = false
    )

    val result = repository.getDisplayNeeded(taxPayerRef, accountingPeriod).futureValue

    result shouldBe expectedOutput

    verify(mockConnection).prepareCall("{call CT_DC_PK.isDisplayNeeded(?, ?, ?, ?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, taxPayerRef)
    verify(mockCallableStatement).setLong(2, accountingPeriod)

    verify(mockCallableStatement).registerOutParameter(3, java.sql.Types.VARCHAR) // P_TAX_ISDISPLAYNEEDED_FLAG
    verify(mockCallableStatement).registerOutParameter(4, java.sql.Types.VARCHAR) // P_INT_ISDISPLAYNEEDED_FLAG
    verify(mockCallableStatement).registerOutParameter(5, java.sql.Types.VARCHAR) // P_PAY_ISDISPLAYNEEDED_FLAG
    verify(mockCallableStatement).registerOutParameter(6, java.sql.Types.VARCHAR) // P_REPAY_ISDISPLAYNEEDED_FLAG

    verify(mockCallableStatement).execute()

    verify(mockCallableStatement).close()
  }

  "getDisplayNeeded" should "close resources when execution throws exception" in {
    val taxPayerRef: Long = 999L
    val accountingPeriod: Long = 1L

    when(mockCallableStatement.execute()).thenThrow(new RuntimeException("Error from downstream"))

    val ex = repository.getDisplayNeeded(taxPayerRef, accountingPeriod).failed.futureValue

    ex.getMessage should include("Error from downstream")

    verify(mockCallableStatement).close()
  }

}
