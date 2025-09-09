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

package uk.gov.hmrc.rdsdatacacheproxy.repositories

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.models.responses.{DirectDebit, UserDebits}

import java.sql.{CallableStatement, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class RdsDatacacheRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: RdsDatacacheRepository = _
  var mockConnection: java.sql.Connection = _
  var mockCallableStatement: CallableStatement = _
  var mockResultSet: ResultSet = _

  before {
    // Mocking the database connection and callable statement
    db = mock(classOf[Database])
    mockConnection = mock(classOf[java.sql.Connection])
    mockCallableStatement = mock(classOf[CallableStatement])
    mockResultSet = mock(classOf[ResultSet])

    // When db.withConnection is called, it should invoke the passed-in function and return the result
    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)  // Return the result of the lambda function passed to withConnection
    }

    // When prepareCall is invoked on the connection, return the mocked callable statement
    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    // Initialize the repository with the mocked db connection
    repository = new RdsDatacacheRepository(db)
  }

  "getDirectDebits" should "return UserDebits with correct data" in {
    // Arrange
    val id = "test-cred-id"
    val start = 0
    val max = 10

    val directDebits = Seq(
      DirectDebit(
        ddiRefNumber = "DDI001",
        submissionDateTime = LocalDate.now().atStartOfDay(),
        bankSortCode = "123456",
        bankAccountNumber = "654321",
        bankAccountName = "Test Bank",
        auDdisFlag = true,
        numberOfPayPlans = 1
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
    val result = repository.getDirectDebits(id, start, max).futureValue

    // Assert
    result shouldBe UserDebits(1, directDebits)
    result.directDebitList shouldBe directDebits
  }

  "getEarliestPaymentDate" should "return EarliestPaymentDate with correct date" in {
    // Arrange
    val baseDate = LocalDate.now()
    val offsetWorkingDays = 5
    val expectedDate = LocalDate.now().plusDays(5) // This is just a mock

    // Mocking stored procedure behavior
    when(mockCallableStatement.getDate("pOutputDate")).thenReturn(Date.valueOf(expectedDate))

    // Act
    val result = repository.getEarliestPaymentDate(baseDate, offsetWorkingDays).futureValue

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
}
