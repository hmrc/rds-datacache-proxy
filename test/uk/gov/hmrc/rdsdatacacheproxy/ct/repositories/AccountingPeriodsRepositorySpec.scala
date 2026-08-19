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
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.AccountingPeriodsHelper

import java.sql.{CallableStatement, Date, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class AccountingPeriodsRepositorySpec extends AnyFreeSpec with Matchers with BeforeAndAfter with AccountingPeriodsHelper {

  var db: Database = _
  var repo: AccountingPeriodsRepositoryImpl = _
  var mockConnection: java.sql.Connection = _
  var mockCallableStatement: CallableStatement = _
  var rs: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[java.sql.Connection])
    mockCallableStatement = mock(classOf[CallableStatement])
    rs                    = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repo = new AccountingPeriodsRepositoryImpl(db)
  }

  "getAccountPeriods" - {
    "return empty accounting periods" in {
      when(mockCallableStatement.getObject(eqTo(2), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenReturn(false)

      when(rs.getBigDecimal("accounting_period")).thenReturn(scala.math.BigDecimal(1).bigDecimal)
      when(rs.getDate("ap_start_date")).thenReturn(Date.valueOf("2025-05-01"))
      when(rs.getDate("ap_end_date")).thenReturn(Date.valueOf("2026-05-01"))
      when(rs.getString("ap_status")).thenReturn("F")
      when(rs.getBoolean("tax_charge_present")).thenReturn(true)
      when(rs.getBoolean("clerical_int_sig")).thenReturn(false)
      when(rs.getBoolean("credit_debit_interest_ind")).thenReturn(true)
      when(rs.getBigDecimal("tax_total")).thenReturn(scala.math.BigDecimal(10).bigDecimal)
      when(rs.getBigDecimal("interest_total")).thenReturn(scala.math.BigDecimal(20).bigDecimal)
      when(rs.getBigDecimal("penalty_total")).thenReturn(scala.math.BigDecimal(30).bigDecimal)
      when(rs.getBigDecimal("payslip_total")).thenReturn(scala.math.BigDecimal(40).bigDecimal)
      when(rs.getBigDecimal("repay_realloc_total")).thenReturn(scala.math.BigDecimal(50).bigDecimal)
      when(rs.getBigDecimal("adjustment_total")).thenReturn(scala.math.BigDecimal(60).bigDecimal)

      val result = repo.getAccountPeriods(2L).futureValue
      result shouldBe emptyAccountingPeriods

      verify(mockConnection).prepareCall("{call CT_DC_PK.getAccountPeriods(?, ?)}")

      verify(mockCallableStatement).setLong(1, 2L)
      verify(mockCallableStatement).registerOutParameter(2, OracleTypes.CURSOR)
      verify(mockCallableStatement).execute()

      verify(rs, times(1)).next()

      verify(mockCallableStatement).close()
    }

    "return accounting periods with one item" in {
      when(mockCallableStatement.getObject(eqTo(2), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenReturn(true, false)

      when(rs.getBigDecimal("accounting_period")).thenReturn(scala.math.BigDecimal(1).bigDecimal)
      when(rs.getDate("ap_start_date")).thenReturn(Date.valueOf("2027-07-24"))
      when(rs.getDate("ap_end_date")).thenReturn(Date.valueOf("2028-07-24"))
      when(rs.getString("ap_status")).thenReturn("S")
      when(rs.getBoolean("tax_charge_present")).thenReturn(true)
      when(rs.getBoolean("clerical_int_sig")).thenReturn(false)
      when(rs.getBoolean("credit_debit_interest_ind")).thenReturn(true)
      when(rs.getBigDecimal("tax_total")).thenReturn(scala.math.BigDecimal(10).bigDecimal)
      when(rs.getBigDecimal("interest_total")).thenReturn(scala.math.BigDecimal(20).bigDecimal)
      when(rs.getBigDecimal("penalty_total")).thenReturn(scala.math.BigDecimal(30).bigDecimal)
      when(rs.getBigDecimal("payslip_total")).thenReturn(scala.math.BigDecimal(40).bigDecimal)
      when(rs.getBigDecimal("repay_realloc_total")).thenReturn(scala.math.BigDecimal(50).bigDecimal)
      when(rs.getBigDecimal("adjustment_total")).thenReturn(scala.math.BigDecimal(60).bigDecimal)

      val result = repo.getAccountPeriods(4L).futureValue
      result shouldBe accountingPeriodsWithOneItem

      verify(mockConnection).prepareCall("{call CT_DC_PK.getAccountPeriods(?, ?)}")

      verify(mockCallableStatement).setLong(1, 4L)
      verify(mockCallableStatement).registerOutParameter(2, OracleTypes.CURSOR)
      verify(mockCallableStatement).execute()

      verify(rs, times(2)).next()

      verify(mockCallableStatement).close()
    }

    "return accounting periods with multiple items" in {
      when(mockCallableStatement.getObject(eqTo(2), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenReturn(true, true, false)

      when(rs.getBigDecimal("accounting_period")).thenReturn(scala.math.BigDecimal(2).bigDecimal, scala.math.BigDecimal(3).bigDecimal)
      when(rs.getDate("ap_start_date")).thenReturn(Date.valueOf("2027-03-05"), Date.valueOf("2016-11-22"))
      when(rs.getDate("ap_end_date")).thenReturn(Date.valueOf("2028-03-05"), Date.valueOf("2017-11-22"))
      when(rs.getString("ap_status")).thenReturn("C", "P")
      when(rs.getBoolean("tax_charge_present")).thenReturn(false, true)
      when(rs.getBoolean("clerical_int_sig")).thenReturn(true, true)
      when(rs.getBoolean("credit_debit_interest_ind")).thenReturn(false, true)
      when(rs.getBigDecimal("tax_total")).thenReturn(scala.math.BigDecimal(100).bigDecimal, scala.math.BigDecimal(110).bigDecimal)
      when(rs.getBigDecimal("interest_total")).thenReturn(scala.math.BigDecimal(200).bigDecimal, scala.math.BigDecimal(220).bigDecimal)
      when(rs.getBigDecimal("penalty_total")).thenReturn(scala.math.BigDecimal(300).bigDecimal, scala.math.BigDecimal(330).bigDecimal)
      when(rs.getBigDecimal("payslip_total")).thenReturn(scala.math.BigDecimal(400).bigDecimal, scala.math.BigDecimal(440).bigDecimal)
      when(rs.getBigDecimal("repay_realloc_total")).thenReturn(scala.math.BigDecimal(500).bigDecimal, scala.math.BigDecimal(550).bigDecimal)
      when(rs.getBigDecimal("adjustment_total")).thenReturn(scala.math.BigDecimal(600).bigDecimal, scala.math.BigDecimal(660).bigDecimal)

      val result = repo.getAccountPeriods(4L).futureValue
      result shouldBe accountingPeriodsWithMultipleItems

      verify(mockConnection).prepareCall("{call CT_DC_PK.getAccountPeriods(?, ?)}")

      verify(mockCallableStatement).setLong(1, 4L)

      verify(mockCallableStatement).registerOutParameter(2, OracleTypes.CURSOR)
      verify(mockCallableStatement).execute()

      verify(rs, times(3)).next()

      verify(mockCallableStatement).close()
    }

    "return an exception and close the connection" in {
      when(mockCallableStatement.getObject(eqTo(2), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenThrow(new RuntimeException("DB error"))

      val ex = repo.getAccountPeriods(1L).failed.futureValue
      ex.getMessage should include("DB error")

      verify(mockCallableStatement).close()
    }
  }
}
