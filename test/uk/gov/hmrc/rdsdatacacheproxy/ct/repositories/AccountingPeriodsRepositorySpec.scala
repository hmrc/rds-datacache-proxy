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
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{RdsAccountingPeriod, RdsAccountingPeriodsRowResponse}

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class AccountingPeriodsRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var repository: AccountingPeriodsRepositoryImpl = _
  var db: Database = _
  var mockConnection: Connection = _
  var mockCallableStatement: CallableStatement = _
  var mockResultSet: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[Connection])
    mockCallableStatement = mock(classOf[CallableStatement])
    mockResultSet         = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repository = new AccountingPeriodsRepositoryImpl(db)

  }

  "getAccountingPeriods" should "return list of RdsAccountingPeriod containing single RdsAccountingPeriodsRowResponse from DB when stored procedure is invoked" in {
    val taxRef = 123456789L

    when(mockCallableStatement.getObject(eqTo(2), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenReturn(true, false)

    when(mockResultSet.getBigDecimal("accounting_period")).thenReturn(BigDecimal(7600.45).bigDecimal)
    when(mockResultSet.getDate("ap_start_date")).thenReturn(Date.valueOf("2019-12-7"))
    when(mockResultSet.getDate("ap_end_date")).thenReturn(Date.valueOf("2016-11-9"))
    when(mockResultSet.getString("ap_status")).thenReturn("Open")
    when(mockResultSet.getString("tax_charge_present")).thenReturn("Y")
    when(mockResultSet.getString("clerical_int_sig")).thenReturn("Y")
    when(mockResultSet.getString("credit_debit_interest_ind")).thenReturn("Y")
    when(mockResultSet.getBigDecimal("tax_total")).thenReturn(BigDecimal(12345.67).bigDecimal)
    when(mockResultSet.getBigDecimal("interest_total")).thenReturn(BigDecimal(89.10).bigDecimal)
    when(mockResultSet.getBigDecimal("penalty_total")).thenReturn(BigDecimal(250.00).bigDecimal)
    when(mockResultSet.getBigDecimal("payslip_total")).thenReturn(BigDecimal(5000.00).bigDecimal)
    when(mockResultSet.getBigDecimal("repay_realloc_total")).thenReturn(BigDecimal(300.00).bigDecimal)
    when(mockResultSet.getBigDecimal("adjustment_total")).thenReturn(BigDecimal(75.50).bigDecimal)

    val accPeriods = RdsAccountingPeriod(accountingPeriods =
      List(
        RdsAccountingPeriodsRowResponse(
          accountingPeriod       = BigDecimal(7600.45),
          apStartDate            = LocalDate.of(2019, 12, 7),
          apEndDate              = LocalDate.of(2016, 11, 9),
          apStatus               = "Open",
          taxChargePresent       = Some("Y"),
          clericalIntSig         = Some("Y"),
          creditDebitInterestInd = Some("Y"),
          taxTotal               = Some(BigDecimal(12345.67)),
          interestTotal          = Some(BigDecimal(89.10)),
          penaltyTotal           = Some(BigDecimal(250.00)),
          payslipTotal           = Some(BigDecimal(5000.00)),
          repayReallocTotal      = Some(BigDecimal(300.00)),
          adjustmentTotal        = Some(BigDecimal(75.50))
        )
      )
    )

    val result = repository.getAccountingPeriods(taxRef).futureValue

    result shouldBe accPeriods

    verify(mockConnection).prepareCall("{call CT_DC_PK.getAccountPeriods(?, ?)}")

    verify(mockCallableStatement).setLong(1, 123456789L)

    verify(mockCallableStatement).registerOutParameter(2, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockResultSet, times(2)).next()

    verify(mockCallableStatement).close()

  }

  "getAccountingPeriods" should "return an empty list of RdsAccountingPeriod from DB when stored procedure is invoked and resultSet is null" in {
    val taxRef = 123456789L

    when(mockCallableStatement.getObject(eqTo(2), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenReturn(false)

    val accDetails = List.empty

    val result = repository.getAccountingPeriods(taxRef).futureValue

    result shouldBe RdsAccountingPeriod(accountingPeriods = accDetails)

    verify(mockConnection).prepareCall("{call CT_DC_PK.getAccountPeriods(?, ?)}")

    verify(mockCallableStatement).setLong(1, 123456789L)

    verify(mockCallableStatement).registerOutParameter(2, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockResultSet, times(1)).next()

    verify(mockCallableStatement).close()

  }

  "getAccountingPeriods" should "return lists of RdsAccountingPeriodsRowResponse containing multiple RdsAccountingPeriod from DB when stored procedure is invoked" in {
    val taxRef = 123456789L

    when(mockCallableStatement.getObject(eqTo(2), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenReturn(true, true, false)

    when(mockResultSet.getBigDecimal("accounting_period")).thenReturn(BigDecimal(7600.45).bigDecimal, BigDecimal(7600.45).bigDecimal)
    when(mockResultSet.getDate("ap_start_date")).thenReturn(Date.valueOf("2019-12-7"), Date.valueOf("2019-12-7"))
    when(mockResultSet.getDate("ap_end_date")).thenReturn(Date.valueOf("2016-11-9"), Date.valueOf("2016-11-9"))
    when(mockResultSet.getString("ap_status")).thenReturn("Open", "Closed")
    when(mockResultSet.getString("tax_charge_present")).thenReturn("Y", "N")
    when(mockResultSet.getString("clerical_int_sig")).thenReturn("Y", "N")
    when(mockResultSet.getString("credit_debit_interest_ind")).thenReturn("Y", "N")
    when(mockResultSet.getBigDecimal("tax_total")).thenReturn(BigDecimal(12345.67).bigDecimal, BigDecimal(12345.67).bigDecimal)
    when(mockResultSet.getBigDecimal("interest_total")).thenReturn(BigDecimal(89.10).bigDecimal, BigDecimal(89.10).bigDecimal)
    when(mockResultSet.getBigDecimal("penalty_total")).thenReturn(BigDecimal(250.00).bigDecimal, BigDecimal(250.00).bigDecimal)
    when(mockResultSet.getBigDecimal("payslip_total")).thenReturn(BigDecimal(5000.00).bigDecimal, BigDecimal(5000.00).bigDecimal)
    when(mockResultSet.getBigDecimal("repay_realloc_total")).thenReturn(BigDecimal(300.00).bigDecimal, BigDecimal(300.00).bigDecimal)
    when(mockResultSet.getBigDecimal("adjustment_total")).thenReturn(BigDecimal(75.50).bigDecimal, BigDecimal(75.50).bigDecimal)

    val accPeriods = RdsAccountingPeriod(accountingPeriods =
      List(
        RdsAccountingPeriodsRowResponse(
          accountingPeriod       = BigDecimal(7600.45),
          apStartDate            = LocalDate.of(2019, 12, 7),
          apEndDate              = LocalDate.of(2016, 11, 9),
          apStatus               = "Open",
          taxChargePresent       = Some("Y"),
          clericalIntSig         = Some("Y"),
          creditDebitInterestInd = Some("Y"),
          taxTotal               = Some(BigDecimal(12345.67)),
          interestTotal          = Some(BigDecimal(89.10)),
          penaltyTotal           = Some(BigDecimal(250.00)),
          payslipTotal           = Some(BigDecimal(5000.00)),
          repayReallocTotal      = Some(BigDecimal(300.00)),
          adjustmentTotal        = Some(BigDecimal(75.50))
        ),
        RdsAccountingPeriodsRowResponse(
          accountingPeriod       = BigDecimal(7600.45),
          apStartDate            = LocalDate.of(2019, 12, 7),
          apEndDate              = LocalDate.of(2016, 11, 9),
          apStatus               = "Closed",
          taxChargePresent       = Some("N"),
          clericalIntSig         = Some("N"),
          creditDebitInterestInd = Some("N"),
          taxTotal               = Some(BigDecimal(12345.67)),
          interestTotal          = Some(BigDecimal(89.10)),
          penaltyTotal           = Some(BigDecimal(250.00)),
          payslipTotal           = Some(BigDecimal(5000.00)),
          repayReallocTotal      = Some(BigDecimal(300.00)),
          adjustmentTotal        = Some(BigDecimal(75.50))
        )
      )
    )
    val result = repository.getAccountingPeriods(taxRef).futureValue

    result shouldBe accPeriods

    verify(mockConnection).prepareCall("{call CT_DC_PK.getAccountPeriods(?, ?)}")

    verify(mockCallableStatement).setLong(1, 123456789L)

    verify(mockCallableStatement).registerOutParameter(2, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockResultSet, times(3)).next()

    verify(mockCallableStatement).close()
  }

  "getAccountingPeriods" should "return an exception and close the connection when an exception occurs in Downstream services" in {
    val taxRef = 123456789L

    when(mockCallableStatement.execute()).thenThrow(new RuntimeException("DB error"))

    val ex = repository.getAccountingPeriods(taxRef).failed.futureValue
    ex.getMessage should include("DB error")

    verify(mockCallableStatement).close()
  }

}
