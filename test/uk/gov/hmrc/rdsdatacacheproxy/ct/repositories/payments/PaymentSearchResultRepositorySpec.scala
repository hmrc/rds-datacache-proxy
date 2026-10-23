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
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.payments.PaymentSearchResultStubData
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.PaymentSearchResultResponse

import java.sql.{CallableStatement, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class PaymentSearchResultRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter with PaymentSearchResultStubData {

  var db: Database = _
  var repository: PaymentSearchResultRepositoryImpl = _
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

    repository = new PaymentSearchResultRepositoryImpl(db)
  }

  "getPaymentSearchResult" should "return an empty PaymentSearchResultResponse when resultSet is null" in {
    when(mockCallableStatement.getObject(eqTo(2), eqTo(classOf[ResultSet]))).thenReturn(rs)
    when(rs.next()).thenReturn(true, false)

    val result = repository.getPaymentSearchResult(taxRef = 1L, Some(LocalDate.parse("2006-01-01")), Some(LocalDate.parse("2006-12-31"))).futureValue

    result shouldBe PaymentSearchResultResponse(paymentSearchResult = List.empty)

    verify(mockConnection).prepareCall("{call CT_DC_PK.getPaymentSearchResults(?,?,?,?)}")

    verify(mockCallableStatement).setLong(1, 1L)

    verify(mockCallableStatement).registerOutParameter(4, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()
    verify(mockCallableStatement).close()
  }

  "getPaymentSearchResult" should "return PaymentSearchResultResponse with multiple CompanyDetails for a given taxPayerReference" in {
    val taxRef = 1L

    when(mockCallableStatement.getObject(eqTo(4), eqTo(classOf[ResultSet]))).thenReturn(rs)
    when(rs.next()).thenReturn(true, true, true, false)

    when(rs.getBigDecimal("ACCOUNTING_PERIOD")).thenReturn(BigDecimal(1).bigDecimal, BigDecimal(1).bigDecimal, BigDecimal(1).bigDecimal)
    when(rs.getBigDecimal("PAYSLIP_ID")).thenReturn(BigDecimal(1).bigDecimal, BigDecimal(2).bigDecimal, BigDecimal(3).bigDecimal)
    when(rs.getBigDecimal("AMOUNT")).thenReturn(BigDecimal(37500).bigDecimal, BigDecimal(31250).bigDecimal, BigDecimal(37500).bigDecimal)
    when(rs.getDate("EFFECTIVE_DATE_OF_PAYMENT")).thenReturn(Date.valueOf("2007-7-14"), Date.valueOf("2006-10-14"), Date.valueOf("2005-07-14"))
    when(rs.getString("PAYMENT_TYPE")).thenReturn("BGP", "BGP", "BGP")
    when(rs.getBigDecimal("BANK_SORT_CODE")).thenReturn(BigDecimal(114433).bigDecimal, BigDecimal(114433).bigDecimal, BigDecimal(114433).bigDecimal)
    when(rs.getBigDecimal("BANK_ACCOUNT_NUMBER"))
      .thenReturn(BigDecimal(1004005).bigDecimal, BigDecimal(1004005).bigDecimal, BigDecimal(1004005).bigDecimal)
    when(rs.getBigDecimal("CHEQUE_NUMBER")).thenReturn(BigDecimal(100301).bigDecimal, BigDecimal(100301).bigDecimal, BigDecimal(100301).bigDecimal)
    when(rs.getBigDecimal("PAYMENT_AMOUNT")).thenReturn(BigDecimal(31250).bigDecimal, BigDecimal(31250).bigDecimal, BigDecimal(31250).bigDecimal)
    when(rs.getBigDecimal("CHEQUE_ID")).thenReturn(BigDecimal(916).bigDecimal, BigDecimal(917).bigDecimal, BigDecimal(918).bigDecimal)

    val result = repository.getPaymentSearchResult(taxRef, None, None).futureValue

    result shouldBe multiplePaymentSearchResultResponse

    verify(mockConnection).prepareCall("{call CT_DC_PK.getPaymentSearchResults(?,?,?,?)}")

    verify(mockCallableStatement).setLong(1, 1L)

    verify(mockCallableStatement).registerOutParameter(4, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(rs, times(4)).next()
    verify(mockCallableStatement).close()
  }

  "getPaymentSearchResult" should "throw exception and close the connection when exception is returned from DB" in {
    val taxRef = 123456L

    when(mockCallableStatement.execute()).thenThrow(new RuntimeException("Boom"))

    val ex = repository.getPaymentSearchResult(taxRef, None, None).failed.futureValue

    ex.getMessage should include("Boom")

    verify(mockCallableStatement).close()
  }

}
