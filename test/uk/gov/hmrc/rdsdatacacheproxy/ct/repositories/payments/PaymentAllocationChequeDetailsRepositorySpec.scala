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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.{PaymentAllocationChequeDetails, PaymentAllocationChequeRow}

import java.sql.{CallableStatement, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class PaymentAllocationChequeDetailsRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: PaymentAllocationChequeDetailsRepositoryImpl = _
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

    repository = new PaymentAllocationChequeDetailsRepositoryImpl(db)

  }

  "getPaymentAllocationChequeDetails" should "return list of PaymentAllocationChequeDetails" in {
    val taxRef = 123456789L
    val chequeId = 13L

    when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenReturn(true, true, false)

    when(mockResultSet.getLong("ACCOUNTING_PERIOD")).thenReturn(12820832002L, 1232002L)
    when(mockResultSet.getDate("AP_END_DATE")).thenReturn(Date.valueOf("2016-11-9"), Date.valueOf("2017-11-9"))
    when(mockResultSet.getBigDecimal("AMOUNT")).thenReturn(BigDecimal(7205720.982).bigDecimal, BigDecimal(7200.982).bigDecimal)
    when(mockResultSet.getDate("EFFECTIVE_DATE_OF_PAYMENT")).thenReturn(Date.valueOf("2026-11-12"), Date.valueOf("2026-12-12"))

    val paymentAllocationChequeDetailsRow = List(
      PaymentAllocationChequeRow(
        accountingPeriod       = 12820832002L,
        apEndDate              = LocalDate.of(2016, 11, 9),
        amount                 = Some(BigDecimal(7205720.982)),
        effectiveDateOfPayment = LocalDate.of(2026, 11, 12)
      ),
      PaymentAllocationChequeRow(
        accountingPeriod       = 1232002L,
        apEndDate              = LocalDate.of(2017, 11, 9),
        amount                 = Some(BigDecimal(7200.982)),
        effectiveDateOfPayment = LocalDate.of(2026, 12, 12)
      )
    )

    val result = repository.getPaymentAllocationChequeDetails(taxRef, chequeId).futureValue

    result shouldBe PaymentAllocationChequeDetails(paymentAllocationDetails = paymentAllocationChequeDetailsRow)

    verify(mockConnection).prepareCall("{call CT_DC_PK.getPayAllocationChequeDtls(?,?,?)}")

    verify(mockCallableStatement).setLong(1, taxRef)
    verify(mockCallableStatement).setLong(2, chequeId)

    verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockResultSet, times(3)).next()

    verify(mockCallableStatement).close()

  }

  "getPaymentAllocationChequeDetails" should "return an empty list" in {
    val taxRef = 123456789L
    val chequeId = 13L

    when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenReturn(false)

    val paymentAllocationChequeDetailsRow = List.empty

    val result = repository.getPaymentAllocationChequeDetails(taxRef, chequeId).futureValue

    result shouldBe PaymentAllocationChequeDetails(paymentAllocationDetails = paymentAllocationChequeDetailsRow)

    verify(mockConnection).prepareCall("{call CT_DC_PK.getPayAllocationChequeDtls(?,?,?)}")

    verify(mockCallableStatement).setLong(1, taxRef)
    verify(mockCallableStatement).setLong(2, chequeId)

    verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockResultSet, times(1)).next()

    verify(mockCallableStatement).close()
  }

  "getPaymentAllocationChequeDetails" should "return a list  with single element" in {
    val taxRef = 123456789L
    val chequeId = 13L

    when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenReturn(true, false)

    when(mockResultSet.getLong("ACCOUNTING_PERIOD")).thenReturn(12820832002L)
    when(mockResultSet.getDate("AP_END_DATE")).thenReturn(Date.valueOf("2016-11-9"))
    when(mockResultSet.getBigDecimal("AMOUNT")).thenReturn(BigDecimal(7205720.982).bigDecimal)
    when(mockResultSet.getDate("EFFECTIVE_DATE_OF_PAYMENT")).thenReturn(Date.valueOf("2026-11-12"))

    val paymentAllocationChequeDetailsRow = List(
      PaymentAllocationChequeRow(
        accountingPeriod       = 12820832002L,
        apEndDate              = LocalDate.of(2016, 11, 9),
        amount                 = Some(BigDecimal(7205720.982)),
        effectiveDateOfPayment = LocalDate.of(2026, 11, 12)
      )
    )

    val result = repository.getPaymentAllocationChequeDetails(taxRef, chequeId).futureValue

    result shouldBe PaymentAllocationChequeDetails(paymentAllocationDetails = paymentAllocationChequeDetailsRow)

    verify(mockConnection).prepareCall("{call CT_DC_PK.getPayAllocationChequeDtls(?,?,?)}")

    verify(mockCallableStatement).setLong(1, taxRef)
    verify(mockCallableStatement).setLong(2, chequeId)

    verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockResultSet, times(2)).next()

    verify(mockCallableStatement).close()

  }

  "getPaymentAllocationChequeDetails" should "close connection when there is exception from the downstream services" in {
    val taxRef = 123456789L
    val chequeId = 13L

    when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenThrow(new RuntimeException("Error from DB"))
    when(mockResultSet.next()).thenReturn(true, false)

    val exception = repository.getPaymentAllocationChequeDetails(taxRef, chequeId).failed.futureValue
    exception.getMessage should be("Error from DB")
    verify(mockCallableStatement).close()
  }

}
