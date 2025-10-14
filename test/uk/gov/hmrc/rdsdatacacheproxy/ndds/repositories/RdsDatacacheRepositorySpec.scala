/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy.ndds.repositories

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ndds.config.AppConfig
import uk.gov.hmrc.rdsdatacacheproxy.ndds.models.responses.*

import java.sql.{CallableStatement, Date, ResultSet, Timestamp}
import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext.Implicits.global

class RdsDatacacheRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: RdsDatacacheRepository = _
  var mockConnection: java.sql.Connection = _
  var mockCallableStatement: CallableStatement = _
  var mockResultSet: ResultSet = _
  var mockConfig: AppConfig = _

  before {
    // Mocking the database connection and callable statement
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[java.sql.Connection])
    mockCallableStatement = mock(classOf[CallableStatement])
    mockResultSet         = mock(classOf[ResultSet])
    mockConfig            = mock(classOf[AppConfig])

    // When db.withConnection is called, it should invoke the passed-in function and return the result
    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection) // Return the result of the lambda function passed to withConnection
    }

    // When prepareCall is invoked on the connection, return the mocked callable statement
    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    // Initialize the repository with the mocked db connection
    repository = new RdsDatacacheRepository(db, mockConfig)
  }

  "getDirectDebits" should "return UserDebits with correct data" in {
    // Arrange
    val id = "test-cred-id"

    val directDebits = Seq(
      DirectDebit(
        ddiRefNumber       = "DDI001",
        submissionDateTime = LocalDate.now().atStartOfDay(),
        bankSortCode       = "123456",
        bankAccountNumber  = "654321",
        bankAccountName    = "Test Bank",
        auDdisFlag         = true,
        numberOfPayPlans   = 1
      )
    )

    // Mocking stored procedure behavior for pTotalRecords and pDDSummary
    when(mockCallableStatement.getInt("pTotalRecords")).thenReturn(1)
    when(mockCallableStatement.getObject("pDDSummary", classOf[ResultSet])).thenReturn(mockResultSet)

    // Mock the ResultSet to return the correct data
    when(mockResultSet.next()).thenReturn(true).thenReturn(false) // First call returns true, then false (no more rows)
    when(mockResultSet.getString("DDIRefNumber")).thenReturn("DDI001")
    when(mockResultSet.getTimestamp("SubmissionDateTime")).thenReturn(java.sql.Timestamp.valueOf(LocalDate.now().atStartOfDay()))
    when(mockResultSet.getString("BankSortCode")).thenReturn("123456")
    when(mockResultSet.getString("BankAccountNumber")).thenReturn("654321")
    when(mockResultSet.getString("BankAccountName")).thenReturn("Test Bank")
    when(mockResultSet.getBoolean("AuddisFlag")).thenReturn(true)
    when(mockResultSet.getInt("NumberofPayPlans")).thenReturn(1)

    // Act
    val result = repository.getDirectDebits(id).futureValue

    // Assert
    result                 shouldBe UserDebits(1, directDebits)
    result.directDebitList shouldBe directDebits
  }

  "addFutureWorkingDays" should "return EarliestPaymentDate with correct date" in {
    // Arrange
    val baseDate = LocalDate.now()
    val offsetWorkingDays = 5
    val expectedDate = LocalDate.now().plusDays(5) // This is just a mock

    // Mocking stored procedure behavior
    when(mockCallableStatement.getDate("pOutputDate")).thenReturn(Date.valueOf(expectedDate))

    // Act
    val result = repository.addFutureWorkingDays(baseDate, offsetWorkingDays).futureValue

    // Assert
    result.date shouldBe expectedDate
  }

  "getDirectDebitReference" should "return DDIReference with correct reference" in {
    // Arrange
    val paymentReference = "paymentRef123"
    val credId = "credId123"
    val sessionId = "sessionId123"
    val expectedDDIReference = "DDIReference123"

    // Mocking stored procedure behavior
    when(mockCallableStatement.getString("pDDIRefNumber")).thenReturn(expectedDDIReference)

    // Act
    val result = repository.getDirectDebitReference(paymentReference, credId, sessionId).futureValue

    // Assert
    result.ddiRefNumber shouldBe expectedDDIReference
  }

  "getDirectDebitPaymentPlans" should "return DDPaymentPlans with correct data" in {
    // Arrange
    val ddReference = "test reference"
    val id = "test-cred-id"
    val submissionDateTime = LocalDate.now().atStartOfDay()

    val paymentPlans = Seq(
      PaymentPlan(
        scheduledPaymentAmount = 100,
        planRefNumber          = ddReference,
        planType               = "01",
        paymentReference       = "plan ref number",
        hodService             = "CESA",
        submissionDateTime     = submissionDateTime
      )
    )

    // Mocking stored procedure behavior
    when(mockCallableStatement.getString("pBankSortCode")).thenReturn("sort code")
    when(mockCallableStatement.getString("pBankAccountNumber")).thenReturn("account number")
    when(mockCallableStatement.getString("pBankAccountName")).thenReturn("account name")
    when(mockCallableStatement.getString("pAUDDISFlag")).thenReturn("dd")
    when(mockCallableStatement.getInt("pTotalRecords")).thenReturn(1)
    when(mockCallableStatement.getObject("pPayPlanSummary", classOf[ResultSet])).thenReturn(mockResultSet)

    // Mock the ResultSet to return the correct data
    when(mockResultSet.next()).thenReturn(true).thenReturn(false) // First call returns true, then false (no more rows)
    when(mockResultSet.getBigDecimal("ScheduledPayAmount")).thenReturn(scala.math.BigDecimal(100.0).bigDecimal)
    when(mockResultSet.getString("PPRefNumber")).thenReturn(ddReference)
    when(mockResultSet.getString("PayPlanType")).thenReturn("01")
    when(mockResultSet.getString("PayReference")).thenReturn("plan ref number")
    when(mockResultSet.getString("PayPlanHodService")).thenReturn("CESA")
    when(mockResultSet.getTimestamp("SubmissionDateTime"))
      .thenReturn(java.sql.Timestamp.valueOf(LocalDate.now().atStartOfDay()))

    // Act
    val result = repository.getDirectDebitPaymentPlans(ddReference, id).futureValue

    // Assert
    result                 shouldBe DDPaymentPlans("sort code", "account number", "account name", "dd", 1, paymentPlans)
    result.paymentPlanList shouldBe paymentPlans
  }

  "getPaymentPlanDetails" should "return PaymentPlanDetails with correct data" in {
    // Arrange
    val ddReference = "test dd reference"
    val id = "test-cred-id"
    val paymentReference = "test payment reference"

    val currentTime = LocalDateTime.now()
    val currentDate = LocalDate.now()

    val mockPaymentDetails = PaymentPlanDetails(
      directDebitDetails = DirectDebitDetail(bankSortCode = Some("sort code"),
                                             bankAccountNumber  = Some("account number"),
                                             bankAccountName    = None,
                                             auDdisFlag         = true,
                                             submissionDateTime = currentTime
                                            ),
      paymentPlanDetails = PaymentPlanDetail(
        hodService                = "CESA",
        planType                  = "01",
        paymentReference          = paymentReference,
        submissionDateTime        = currentTime,
        scheduledPaymentAmount    = Some(1000),
        scheduledPaymentStartDate = Some(currentTime.toLocalDate),
        initialPaymentStartDate   = Some(currentTime.toLocalDate),
        initialPaymentAmount      = Some(150),
        scheduledPaymentEndDate   = Some(currentTime.toLocalDate),
        scheduledPaymentFrequency = Some(1),
        suspensionStartDate       = Some(currentTime.toLocalDate),
        suspensionEndDate         = None,
        balancingPaymentAmount    = Some(600),
        balancingPaymentDate      = Some(currentTime.toLocalDate),
        totalLiability            = None,
        paymentPlanEditable       = false
      )
    )

    // Mocking stored procedure behavior
    when(mockCallableStatement.getString("pBankAccountNumber")).thenReturn("account number")
    when(mockCallableStatement.getString("pBankSortCode")).thenReturn("sort code")
    when(mockCallableStatement.getString("pBankAccountName")).thenReturn(null)
    when(mockCallableStatement.getTimestamp("pDDISubmissionDateTime")).thenReturn(Timestamp.valueOf(currentTime))
    when(mockCallableStatement.getString("pAUDDISFlag")).thenReturn("01")
    when(mockCallableStatement.getString("pPayPlanHodService")).thenReturn("CESA")
    when(mockCallableStatement.getString("pPayPlanType")).thenReturn("01")
    when(mockCallableStatement.getString("pPayReference")).thenReturn("test payment reference")
    when(mockCallableStatement.getTimestamp("pSubmissionDateTime")).thenReturn(Timestamp.valueOf(currentTime))
    when(mockCallableStatement.getBigDecimal("pScheduledPayAmount")).thenReturn(scala.math.BigDecimal(1000.0).bigDecimal)
    when(mockCallableStatement.getDate("pScheduledPayStartDate")).thenReturn(Date.valueOf(currentDate))
    when(mockCallableStatement.getDate("pInitialPayStartDate")).thenReturn(Date.valueOf(currentDate))
    when(mockCallableStatement.getBigDecimal("pInitialPayAmount")).thenReturn(scala.math.BigDecimal(150.0).bigDecimal)
    when(mockCallableStatement.getDate("pScheduledPayEndDate")).thenReturn(Date.valueOf(currentDate))
    when(mockCallableStatement.getInt("pScheduledPayFreq")).thenReturn(1)
    when(mockCallableStatement.getDate("pSuspensionStartDate")).thenReturn(Date.valueOf(currentDate))
    when(mockCallableStatement.getDate("pSuspensionEndDate")).thenReturn(null)
    when(mockCallableStatement.getBigDecimal("pBalancingPayAmount")).thenReturn(scala.math.BigDecimal(600.0).bigDecimal)
    when(mockCallableStatement.getDate("pBalancingPayDate")).thenReturn(Date.valueOf(currentDate))
    when(mockCallableStatement.getBigDecimal("pTotalLiability")).thenReturn(null)
    when(mockCallableStatement.getInt("pPayPlanEditFlag")).thenReturn(0)
    when(mockCallableStatement.getString("pResponseStatus")).thenReturn("PP FOUND")

    // Act
    val result = repository.getPaymentPlanDetails(ddReference, id, paymentReference).futureValue

    // Assert
    result shouldBe mockPaymentDetails
  }
}
