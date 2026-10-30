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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.PaymentPostingsResponse
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.payments.PaymentPostingsStubData

import java.sql.{CallableStatement, Date, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class PaymentPostingsRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter with PaymentPostingsStubData {

  var db: Database = _
  var repository: PaymentPostingsRepositoryImpl = _
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

    repository = new PaymentPostingsRepositoryImpl(db)

  }

  "getPaymentPostings" should "return an empty list" in {
    val taxRef = 124252L

    when(mockCallableStatement.getObject(eqTo(1), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenReturn(false)

    val result = repository.getPaymentPostings(taxRef).futureValue

    result shouldBe emptyPaymentPostings

    verify(mockConnection).prepareCall("{call CT_DC_PK.getPaymentPostingList(?,?)}")

    verify(mockCallableStatement).setLong(1, taxRef)

    verify(mockCallableStatement).registerOutParameter(2, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockCallableStatement).close()
  }

  "getPaymentPostings" should "return a list with single Payment Postings" in {
    val taxRef = 7293742L

    when(mockCallableStatement.getObject(eqTo(2), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenReturn(true, false)

    when(mockResultSet.getLong("ACCOUNTING_PERIOD")).thenReturn(202408L)
    when(mockResultSet.getLong("SETTLEMENT_SEQUENCE_NO")).thenReturn(123456L)
    when(mockResultSet.getLong("PAYSLIP_ID")).thenReturn(987654L)
    when(mockResultSet.getBigDecimal("POSTING_AMOUNT")).thenReturn(BigDecimal(1500.75).bigDecimal)
    when(mockResultSet.getDate("EFFECTIVE_DATE_OF_PAYMENT")).thenReturn(Date.valueOf("2024-09-15"))
    when(mockResultSet.getString("PAYMENT_TYPE")).thenReturn("BAC")

    val result = repository.getPaymentPostings(taxRef).futureValue

    result shouldBe singlePaymentPosting

    verify(mockConnection).prepareCall("{call CT_DC_PK.getPaymentPostingList(?,?)}")

    verify(mockCallableStatement).setLong(1, taxRef)

    verify(mockCallableStatement).registerOutParameter(2, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockResultSet, times(2)).next()

    verify(mockCallableStatement).close()

  }

  "getPaymentPostings" should "return a list of multiple PaymentPostings" in {
    val taxRef = 93837L

    when(mockCallableStatement.getObject(eqTo(4), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenReturn(true, true, true, false)

    when(mockResultSet.getLong("ACCOUNTING_PERIOD")).thenReturn(202408L, 202407L, 202404L)
    when(mockResultSet.getLong("SETTLEMENT_SEQUENCE_NO")).thenReturn(123456L, 642736L, 99423764L)
    when(mockResultSet.getLong("PAYSLIP_ID")).thenReturn(987654L, 983123L, 985624L)
    when(mockResultSet.getBigDecimal("POSTING_AMOUNT"))
      .thenReturn(BigDecimal(1500.75).bigDecimal, BigDecimal(2712.63).bigDecimal, BigDecimal(2416.89).bigDecimal)
    when(mockResultSet.getDate("EFFECTIVE_DATE_OF_PAYMENT"))
      .thenReturn(Date.valueOf("2024-09-15"), Date.valueOf("2024-08-20"), Date.valueOf("2024-05-26"))
    when(mockResultSet.getString("PAYMENT_TYPE")).thenReturn("BAC", "BAC", "BAC")

    val result = repository.getPaymentPostings(taxRef).futureValue

    result shouldBe multiplePaymentPostings

    verify(mockConnection).prepareCall("{call CT_DC_PK.getPaymentPostingList(?,?)}")

    verify(mockCallableStatement).setLong(1, taxRef)

    verify(mockCallableStatement).registerOutParameter(2, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockResultSet, times(4)).next()

    verify(mockCallableStatement).close()

  }

  "getPaymentAllocationChequeDetails" should "close connection when there is exception from the downstream services" in {
    val taxRef = 123456789L

    when(mockCallableStatement.getObject(eqTo(1), eqTo(classOf[ResultSet]))).thenThrow(new RuntimeException("Error from DB"))
    when(mockResultSet.next()).thenReturn(true, false)

    val exception = repository.getPaymentPostings(taxRef).failed.futureValue
    exception.getMessage should be("Error from DB")
    verify(mockCallableStatement).close()
  }

}
