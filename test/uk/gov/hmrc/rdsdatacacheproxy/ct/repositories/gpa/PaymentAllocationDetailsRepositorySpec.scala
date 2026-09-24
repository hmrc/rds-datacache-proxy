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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.gpa

import oracle.jdbc.OracleTypes
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.gpa.PaymentAllocationDetailsHelper

import java.sql.{CallableStatement, Date, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class PaymentAllocationDetailsRepositorySpec extends AnyFreeSpec with Matchers with BeforeAndAfter with PaymentAllocationDetailsHelper {

  var db: Database = _
  var repo: PaymentAllocationDetailsRepositoryImpl = _
  var mockConnection: java.sql.Connection = _
  var mockCS: CallableStatement = _
  var rs: ResultSet = _

  before {
    db             = mock(classOf[Database])
    mockConnection = mock(classOf[java.sql.Connection])
    mockCS         = mock(classOf[CallableStatement])
    rs             = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCS)

    repo = new PaymentAllocationDetailsRepositoryImpl(db)
  }

  "getGPAPaymentAllocationDetail" - {
    "return payment allocation details" in {
      when(mockCS.getObject(eqTo(18), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenReturn(true, false)

      when(mockCS.getDate(7)).thenReturn(Date.valueOf("2024-07-24"))
      when(mockCS.getBigDecimal(8)).thenReturn(scala.math.BigDecimal(10).bigDecimal)
      when(mockCS.getBigDecimal(9)).thenReturn(scala.math.BigDecimal(10).bigDecimal)
      when(mockCS.getString(10)).thenReturn("OPEN")
      when(mockCS.getString(11)).thenReturn("HMRC")
      when(mockCS.getString(12)).thenReturn("ABC Limited")
      when(mockCS.getDate(13)).thenReturn(Date.valueOf("2024-07-24"))
      when(mockCS.getBigDecimal(14)).thenReturn(scala.math.BigDecimal(10).bigDecimal)
      when(mockCS.getBigDecimal(15)).thenReturn(scala.math.BigDecimal(10).bigDecimal)
      when(mockCS.getBigDecimal(16)).thenReturn(scala.math.BigDecimal(10).bigDecimal)
      when(mockCS.getBigDecimal(17)).thenReturn(scala.math.BigDecimal(10).bigDecimal)

      when(rs.getDate("effective_payment_date")).thenReturn(Date.valueOf("2024-07-24"))
      when(rs.getBigDecimal("payment_amount")).thenReturn(scala.math.BigDecimal(10).bigDecimal)
      when(mockCS.getBigDecimal(19)).thenReturn(scala.math.BigDecimal(10).bigDecimal)

      val result = repo.getGPAPaymentAllocationDetail(2L, 3L, 4L, 5L, 6L, 7L).futureValue
      result shouldBe fullPaymentAllocationDetails

      verify(mockConnection).prepareCall("{call CT_GPA_PK.getGPAPaymentAllocationDetail(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")

      verify(mockCS).setLong(1, 2L)
      verify(mockCS).setLong(2, 3L)
      verify(mockCS).setLong(3, 4L)
      verify(mockCS).setLong(4, 5L)
      verify(mockCS).setLong(5, 6L)
      verify(mockCS).setLong(6, 7L)

      verify(mockCS).registerOutParameter(7, OracleTypes.DATE) // pGPP_END_DATE
      verify(mockCS).registerOutParameter(8, OracleTypes.NUMERIC) // pGPP_TOTAL_GROUP_PAYMENT
      verify(mockCS).registerOutParameter(9, OracleTypes.NUMERIC) // pGPP_TOTAL_GROUP_TAX
      verify(mockCS).registerOutParameter(10, OracleTypes.VARCHAR) // pGPP_STATUS
      verify(mockCS).registerOutParameter(11, OracleTypes.CHAR) // pGPP_APPORTIONMENT_METHOD
      verify(mockCS).registerOutParameter(12, OracleTypes.VARCHAR) // pPARTICIP_COMPANY_DESC
      verify(mockCS).registerOutParameter(13, OracleTypes.DATE) // pPARTICIP_ACC_PERIOD_ENDING
      verify(mockCS).registerOutParameter(14, OracleTypes.NUMERIC) // pPARTICIP_TAX_CHARGE
      verify(mockCS).registerOutParameter(15, OracleTypes.NUMERIC) // pPARTICIP_ALLOCATED_PAYMENTS
      verify(mockCS).registerOutParameter(16, OracleTypes.NUMERIC) // pGPA_UTR_OUT
      verify(mockCS).registerOutParameter(17, OracleTypes.NUMERIC) // pGPP_CONTRACT_VERSION_OUT

      verify(mockCS).registerOutParameter(18, OracleTypes.REF_CURSOR) // pPAYMENTS
      verify(mockCS).registerOutParameter(19, OracleTypes.NUMERIC) // pTOTAL_NUM_OF_RECORDS

      verify(mockCS).execute()

      verify(rs, times(2)).next()

      verify(mockCS).close()
    }

    "return payment allocation details with multiple allocation details" in {
      when(mockCS.getObject(eqTo(18), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenReturn(true, true, false)

      when(mockCS.getDate(7)).thenReturn(Date.valueOf("2025-07-24"))
      when(mockCS.getBigDecimal(8)).thenReturn(scala.math.BigDecimal(20).bigDecimal)
      when(mockCS.getBigDecimal(9)).thenReturn(scala.math.BigDecimal(20).bigDecimal)
      when(mockCS.getString(10)).thenReturn("CLOSED")
      when(mockCS.getString(11)).thenReturn("HMRC")
      when(mockCS.getString(12)).thenReturn("DEF Limited")
      when(mockCS.getDate(13)).thenReturn(Date.valueOf("2025-07-24"))
      when(mockCS.getBigDecimal(14)).thenReturn(scala.math.BigDecimal(20).bigDecimal)
      when(mockCS.getBigDecimal(15)).thenReturn(scala.math.BigDecimal(20).bigDecimal)
      when(mockCS.getBigDecimal(16)).thenReturn(scala.math.BigDecimal(20).bigDecimal)
      when(mockCS.getBigDecimal(17)).thenReturn(scala.math.BigDecimal(20).bigDecimal)

      when(rs.getDate("effective_payment_date")).thenReturn(Date.valueOf("2025-07-24"), Date.valueOf("2025-07-24"))
      when(rs.getBigDecimal("payment_amount")).thenReturn(scala.math.BigDecimal(20).bigDecimal, scala.math.BigDecimal(20).bigDecimal)
      when(mockCS.getBigDecimal(19)).thenReturn(scala.math.BigDecimal(20).bigDecimal)

      val result = repo.getGPAPaymentAllocationDetail(2L, 3L, 4L, 5L, 6L, 7L).futureValue
      result shouldBe paymentAllocationDetailsWithMultipleAllocationDetails

      verify(mockConnection).prepareCall("{call CT_GPA_PK.getGPAPaymentAllocationDetail(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")

      verify(mockCS).setLong(1, 2L)
      verify(mockCS).setLong(2, 3L)
      verify(mockCS).setLong(3, 4L)
      verify(mockCS).setLong(4, 5L)
      verify(mockCS).setLong(5, 6L)
      verify(mockCS).setLong(6, 7L)

      verify(mockCS).registerOutParameter(7, OracleTypes.DATE) // pGPP_END_DATE
      verify(mockCS).registerOutParameter(8, OracleTypes.NUMERIC) // pGPP_TOTAL_GROUP_PAYMENT
      verify(mockCS).registerOutParameter(9, OracleTypes.NUMERIC) // pGPP_TOTAL_GROUP_TAX
      verify(mockCS).registerOutParameter(10, OracleTypes.VARCHAR) // pGPP_STATUS
      verify(mockCS).registerOutParameter(11, OracleTypes.CHAR) // pGPP_APPORTIONMENT_METHOD
      verify(mockCS).registerOutParameter(12, OracleTypes.VARCHAR) // pPARTICIP_COMPANY_DESC
      verify(mockCS).registerOutParameter(13, OracleTypes.DATE) // pPARTICIP_ACC_PERIOD_ENDING
      verify(mockCS).registerOutParameter(14, OracleTypes.NUMERIC) // pPARTICIP_TAX_CHARGE
      verify(mockCS).registerOutParameter(15, OracleTypes.NUMERIC) // pPARTICIP_ALLOCATED_PAYMENTS
      verify(mockCS).registerOutParameter(16, OracleTypes.NUMERIC) // pGPA_UTR_OUT
      verify(mockCS).registerOutParameter(17, OracleTypes.NUMERIC) // pGPP_CONTRACT_VERSION_OUT

      verify(mockCS).registerOutParameter(18, OracleTypes.REF_CURSOR) // pPAYMENTS
      verify(mockCS).registerOutParameter(19, OracleTypes.NUMERIC) // pTOTAL_NUM_OF_RECORDS

      verify(mockCS).execute()

      verify(rs, times(3)).next()

      verify(mockCS).close()
    }

    "return payment allocation details with minimal details" in {
      when(mockCS.getObject(eqTo(18), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenReturn(true, false)

      when(mockCS.getDate(7)).thenReturn(Date.valueOf("2026-07-24"))
      when(mockCS.getBigDecimal(8)).thenReturn(null)
      when(mockCS.getBigDecimal(9)).thenReturn(null)
      when(mockCS.getString(10)).thenReturn("PARTIAL")
      when(mockCS.getString(11)).thenReturn(null)
      when(mockCS.getString(12)).thenReturn("GHI Limited")
      when(mockCS.getDate(13)).thenReturn(Date.valueOf("2026-07-24"))
      when(mockCS.getBigDecimal(14)).thenReturn(scala.math.BigDecimal(30).bigDecimal)
      when(mockCS.getBigDecimal(15)).thenReturn(scala.math.BigDecimal(30).bigDecimal)
      when(mockCS.getBigDecimal(16)).thenReturn(scala.math.BigDecimal(30).bigDecimal)
      when(mockCS.getBigDecimal(17)).thenReturn(scala.math.BigDecimal(30).bigDecimal)

      when(rs.getDate("effective_payment_date")).thenReturn(Date.valueOf("2026-07-24"))
      when(rs.getBigDecimal("payment_amount")).thenReturn(scala.math.BigDecimal(30).bigDecimal)
      when(mockCS.getBigDecimal(19)).thenReturn(scala.math.BigDecimal(30).bigDecimal)

      val result = repo.getGPAPaymentAllocationDetail(2L, 3L, 4L, 5L, 6L, 7L).futureValue
      result shouldBe minimalPaymentAllocationDetails

      verify(mockConnection).prepareCall("{call CT_GPA_PK.getGPAPaymentAllocationDetail(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")

      verify(mockCS).setLong(1, 2L)
      verify(mockCS).setLong(2, 3L)
      verify(mockCS).setLong(3, 4L)
      verify(mockCS).setLong(4, 5L)
      verify(mockCS).setLong(5, 6L)
      verify(mockCS).setLong(6, 7L)

      verify(mockCS).registerOutParameter(7, OracleTypes.DATE) // pGPP_END_DATE
      verify(mockCS).registerOutParameter(8, OracleTypes.NUMERIC) // pGPP_TOTAL_GROUP_PAYMENT
      verify(mockCS).registerOutParameter(9, OracleTypes.NUMERIC) // pGPP_TOTAL_GROUP_TAX
      verify(mockCS).registerOutParameter(10, OracleTypes.VARCHAR) // pGPP_STATUS
      verify(mockCS).registerOutParameter(11, OracleTypes.CHAR) // pGPP_APPORTIONMENT_METHOD
      verify(mockCS).registerOutParameter(12, OracleTypes.VARCHAR) // pPARTICIP_COMPANY_DESC
      verify(mockCS).registerOutParameter(13, OracleTypes.DATE) // pPARTICIP_ACC_PERIOD_ENDING
      verify(mockCS).registerOutParameter(14, OracleTypes.NUMERIC) // pPARTICIP_TAX_CHARGE
      verify(mockCS).registerOutParameter(15, OracleTypes.NUMERIC) // pPARTICIP_ALLOCATED_PAYMENTS
      verify(mockCS).registerOutParameter(16, OracleTypes.NUMERIC) // pGPA_UTR_OUT
      verify(mockCS).registerOutParameter(17, OracleTypes.NUMERIC) // pGPP_CONTRACT_VERSION_OUT

      verify(mockCS).registerOutParameter(18, OracleTypes.REF_CURSOR) // pPAYMENTS
      verify(mockCS).registerOutParameter(19, OracleTypes.NUMERIC) // pTOTAL_NUM_OF_RECORDS

      verify(mockCS).execute()

      verify(rs, times(2)).next()

      verify(mockCS).close()
    }

    "return an exception and close the connection" in {
      when(mockCS.execute()).thenThrow(new RuntimeException("DB error"))

      val ex = repo.getGPAPaymentAllocationDetail(2L, 3L, 4L, 5L, 6L, 7L).failed.futureValue
      ex.getMessage should include("DB error")

      verify(mockCS).close()
    }
  }
}
