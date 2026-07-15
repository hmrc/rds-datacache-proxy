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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.TaxTransactionsItem

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class TaxTransactionsDataCacheRepositorySpec extends AnyWordSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: TaxTransactionsDataCacheRepository = _
  var mockConnection: java.sql.Connection = _
  var mockCallableStatement: CallableStatement = _
  var mockResultSet: ResultSet = _

  before { // Mocking the database connection and callable statement
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
    repository = new TaxTransactionsDataCacheRepository(db)
  }

  "getTaxTransactions" should {
    "return a list of Tax Transactions when stored procedure returns data" in {

      val taxRef = 123456789L
      val accPeriod = 1L

      when(
        mockCallableStatement.getObject(3, classOf[ResultSet])
      ).thenReturn(mockResultSet)
      when(mockResultSet.next()).thenReturn(true, false)

      when(mockResultSet.getBigDecimal("current_amount")).thenReturn(BigDecimal(123.44).bigDecimal)
      when(mockResultSet.getString("assessment_type")).thenReturn("A")
      when(mockResultSet.getDate("tax_date")).thenReturn(Date.valueOf("2026-01-01"))
      when(mockResultSet.getString("correction_claim_signal")).thenReturn("1")

      val result: List[TaxTransactionsItem] = repository.getTaxTransactions(taxRef, accPeriod).futureValue

      result shouldBe List(
        TaxTransactionsItem(currentAmount = 123.44, assessmentType = "A", taxDate = LocalDate.of(2026, 1, 1), correctionClaimSignal = Some("1"))
      )

      verify(mockConnection).prepareCall("{call CT_DC_PK.getTaxTransactionList(?, ?, ?)}")

      verify(mockCallableStatement).setLong(1, 123456789L)
      verify(mockCallableStatement).setLong(2, 1L)

      verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
      verify(mockCallableStatement).execute()

      verify(mockResultSet, times(2)).next()

      verify(mockCallableStatement).close()
    }
    "return a list of multiple Tax Transactions when stored procedure returns data" in {

      val taxRef = 123456789L
      val accPeriod = 1L

      when(
        mockCallableStatement.getObject(3, classOf[ResultSet])
      ).thenReturn(mockResultSet)
      when(mockResultSet.next()).thenReturn(true, true, false)

      when(mockResultSet.getBigDecimal("current_amount")).thenReturn(BigDecimal(123.44).bigDecimal, BigDecimal(456.77).bigDecimal)
      when(mockResultSet.getString("assessment_type")).thenReturn("A", "D")
      when(mockResultSet.getDate("tax_date")).thenReturn(Date.valueOf("2026-01-01"), Date.valueOf("2026-02-01"))
      when(mockResultSet.getString("correction_claim_signal")).thenReturn("1", "2")

      val result: List[TaxTransactionsItem] = repository.getTaxTransactions(taxRef, accPeriod).futureValue

      result shouldBe List(
        TaxTransactionsItem(currentAmount = 123.44, assessmentType = "A", taxDate = LocalDate.of(2026, 1, 1), correctionClaimSignal = Some("1")),
        TaxTransactionsItem(currentAmount = 456.77, assessmentType = "D", taxDate = LocalDate.of(2026, 2, 1), correctionClaimSignal = Some("2"))
      )

      verify(mockConnection).prepareCall("{call CT_DC_PK.getTaxTransactionList(?, ?, ?)}")

      verify(mockCallableStatement).setLong(1, 123456789L)
      verify(mockCallableStatement).setLong(2, 1L)

      verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
      verify(mockCallableStatement).execute()

      verify(mockResultSet, times(3)).next()

      verify(mockCallableStatement).close()
    }
    "return an empty list of Tax Transactions when stored procedure returns no data" in {

      val taxRef = 123456789L
      val accPeriod = 1L

      when(
        mockCallableStatement.getObject(3, classOf[ResultSet])
      ).thenReturn(mockResultSet)
      when(mockResultSet.next()).thenReturn(false)

      val result: List[TaxTransactionsItem] = repository.getTaxTransactions(taxRef, accPeriod).futureValue

      result shouldBe List.empty

      verify(mockConnection).prepareCall("{call CT_DC_PK.getTaxTransactionList(?, ?, ?)}")

      verify(mockCallableStatement).setLong(1, 123456789L)
      verify(mockCallableStatement).setLong(2, 1L)

      verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
      verify(mockCallableStatement).execute()

      verify(mockResultSet, times(1)).next()

      verify(mockCallableStatement).close()
    }
  }
}
