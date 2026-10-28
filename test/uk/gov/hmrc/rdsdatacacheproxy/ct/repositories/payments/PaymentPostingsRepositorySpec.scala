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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.{PaymentPostingResponse, PaymentPostingRow}

import java.sql.{CallableStatement, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class PaymentPostingsRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: PaymentPostingsRepository = _
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

  "getPaymentPostings" should "return list of PaymentPostings" in {
    val taxRef = 123456789L

    when(mockCallableStatement.getObject(eqTo(1), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenReturn(true, true, false)

    when(mockResultSet.getLong("ACCOUNTING_PERIOD")).thenReturn(12820832002L, 1232002L)
    when(mockResultSet.getLong("SETTLEMENT_SEQUENCE_NO")).thenReturn(5678L, 8335435L)
    when(mockResultSet.getLong("PAYSLIP_ID")).thenReturn(999123L, 999123L)
    when(mockResultSet.getBigDecimal("POSTING_AMOUNT")).thenReturn(BigDecimal(123.45).bigDecimal, BigDecimal(700.43).bigDecimal)
    when(mockResultSet.getDate("EFFECTIVE_DATE_OF_PAYMENT")).thenReturn(Date.valueOf("2016-11-9"), Date.valueOf("2017-11-9"))
    when(mockResultSet.getString("PAYMENT_TYPE")).thenReturn("BAC", "BAC")

    val paymentPostingsList = List(
      PaymentPostingRow(
        accountingPeriod       = 12820832002L,
        settlementSequenceNo   = 5678L,
        payslipId              = 999123L,
        postingAmount          = 123.45,
        effectiveDateOfPayment = LocalDate.of(2016, 11, 9),
        paymentType            = "BAC"
      ),
      PaymentPostingRow(
        accountingPeriod       = 12820832002L,
        settlementSequenceNo   = 5678L,
        payslipId              = 999123L,
        postingAmount          = 123.45,
        effectiveDateOfPayment = LocalDate.of(2017, 11, 9),
        paymentType            = "BAC"
      )
    )

    val result = repository.getPaymentPostings(taxRef).futureValue

    result shouldBe PaymentPostingResponse(paymentPostings = paymentPostingsList)

    verify(mockConnection).prepareCall("{call CT_DC_PK.getPaymentPostingList(?,?)}")

    verify(mockCallableStatement).setLong(1, taxRef)

    verify(mockCallableStatement).registerOutParameter(2, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockResultSet, times(2)).next()

    verify(mockCallableStatement).close()

  }

  "getPaymentPostings" should "return an empty list" in {
    val taxRef = 123456789L

    when(mockCallableStatement.getObject(eqTo(1), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenReturn(false)

    val paymentPostingsList = List.empty

    val result = repository.getPaymentPostings(taxRef).futureValue

    result shouldBe PaymentPostingResponse(paymentPostings = paymentPostingsList)

    verify(mockConnection).prepareCall("{call CT_DC_PK.getPaymentPostingList(?,?)}")

    verify(mockCallableStatement).setLong(1, taxRef)

    verify(mockCallableStatement).registerOutParameter(2, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockResultSet, times(1)).next()

    verify(mockCallableStatement).close()
  }

  "getPaymentAllocationChequeDetails" should "return a list with single element" in {
    val taxRef = 123456789L

    when(mockCallableStatement.getObject(eqTo(1), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenReturn(true, false)

    when(mockResultSet.getLong("ACCOUNTING_PERIOD")).thenReturn(12820832002L)
    when(mockResultSet.getLong("SETTLEMENT_SEQUENCE_NO")).thenReturn(5678L)
    when(mockResultSet.getLong("PAYSLIP_ID")).thenReturn(999123L)
    when(mockResultSet.getBigDecimal("POSTING_AMOUNT")).thenReturn(BigDecimal(123.45).bigDecimal)
    when(mockResultSet.getDate("EFFECTIVE_DATE_OF_PAYMENT")).thenReturn(Date.valueOf("2016-11-9"))
    when(mockResultSet.getString("PAYMENT_TYPE")).thenReturn("BAC")

    val paymentPostingsList = List(
      PaymentPostingRow(
        accountingPeriod       = 12820832002L,
        settlementSequenceNo   = 5678L,
        payslipId              = 999123L,
        postingAmount          = 123.45,
        effectiveDateOfPayment = LocalDate.of(2016, 11, 9),
        paymentType            = "BAC"
      )
    )

    val result = repository.getPaymentPostings(taxRef).futureValue

    result shouldBe PaymentPostingResponse(paymentPostings = paymentPostingsList)

    verify(mockConnection).prepareCall("{call CT_DC_PK.getPaymentPostingList(?,?)}")

    verify(mockCallableStatement).setLong(1, taxRef)

    verify(mockCallableStatement).registerOutParameter(2, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockResultSet, times(2)).next()

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
