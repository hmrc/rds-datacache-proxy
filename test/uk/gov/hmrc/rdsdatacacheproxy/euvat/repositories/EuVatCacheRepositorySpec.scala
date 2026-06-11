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

package uk.gov.hmrc.rdsdatacacheproxy.euvat.repositories

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.euvat.models.responses.TradersKnownFacts

import java.sql.{CallableStatement, ResultSet}
import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global

class EuVatCacheRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: EuVatCacheRepository = _
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
    repository = new EuVatCacheRepository(db)
  }

  "getTraderByVrn" should "return KnownFacts with correct data" in {
    // Arrange
    val vrn = "123"

    val knownFactsResponse: TradersKnownFacts =
      TradersKnownFacts(
        123456789,
        "TestData",
        "Line 1",
        "Line 2",
        "Line 3",
        "Line 4",
        "Line 5",
        "NE3 9TG",
        "7020",
        LocalDateTime.of(2025, 1, 11, 10, 38),
        LocalDateTime.of(2026, 1, 11, 10, 38),
        "N",
        1
      )

    // Mocking stored procedure behavior for pTotalRecords and pDDSummary
    when(mockCallableStatement.getInt("p_vrn")).thenReturn(1)
    when(mockCallableStatement.getObject("p_trader", classOf[ResultSet])).thenReturn(mockResultSet)

    // Mock the ResultSet to return the correct data
    when(mockResultSet.next()).thenReturn(true)
    when(mockResultSet.getInt("vat_reg_number")).thenReturn(123456789)
    when(mockResultSet.getString("trader_name")).thenReturn("TestData")
    when(mockResultSet.getString("bus_address_1")).thenReturn("Line 1")
    when(mockResultSet.getString("bus_address_2")).thenReturn("Line 2")
    when(mockResultSet.getString("bus_address_3")).thenReturn("Line 3")
    when(mockResultSet.getString("bus_address_4")).thenReturn("Line 4")
    when(mockResultSet.getString("bus_address_5")).thenReturn("Line 5")
    when(mockResultSet.getString("bus_postcode")).thenReturn("NE3 9TG")
    when(mockResultSet.getString("trade_class")).thenReturn("7020")
    when(mockResultSet.getTimestamp("date_of_reg")).thenReturn(java.sql.Timestamp.valueOf(LocalDateTime.of(2025, 1, 11, 10, 38)))
    when(mockResultSet.getTimestamp("date_of_dereg")).thenReturn(java.sql.Timestamp.valueOf(LocalDateTime.of(2026, 1, 11, 10, 38)))
    when(mockResultSet.getString("missing_trader_ind")).thenReturn("N")
    when(mockResultSet.getInt("ph_sem_trader_ind")).thenReturn(1)

    // Act
    val result = repository.getTraderByVrn(vrn).futureValue

    // Assert
    result shouldBe Some(knownFactsResponse)
  }

  "getTraderByVrn" should "return None" in {
    val vrn = "123"
    // Mocking stored procedure behavior for pTotalRecords and pDDSummary
    when(mockCallableStatement.getInt("p_vrn")).thenReturn(1)
    when(mockCallableStatement.getObject("p_trader", classOf[ResultSet])).thenReturn(mockResultSet)

    when(mockResultSet.next()).thenReturn(false)
    val result = repository.getTraderByVrn(vrn).futureValue

    result shouldBe None
  }

}
