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

import com.google.inject.ImplementedBy
import oracle.jdbc.OracleTypes
import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{AccountingPeriods, AccountingPeriodsDetails}

import java.sql.*
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AccountingPeriodsRepositoryImpl])
trait AccountingPeriodsRepository {
  def getAccountPeriods(taxRef: Long): Future[AccountingPeriods]
}

class AccountingPeriodsRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends AccountingPeriodsRepository
    with Logging {

  def getAccountPeriods(taxRef: Long): Future[AccountingPeriods] = {
    Future {
      db.withConnection { connect =>
        val storedProcedure = connect.prepareCall("{call CT_DC_PK.getAccountPeriods(?, ?)}")

        try {
          storedProcedure.setLong(1, taxRef)
          storedProcedure.registerOutParameter(2, OracleTypes.CURSOR)

          storedProcedure.execute()

          val accountingPeriodsDetails = storedProcedure.getObject(2, classOf[ResultSet])

          val accountingPeriods = Option(accountingPeriodsDetails).map(readAccountingPeriods).getOrElse(List.empty)

          AccountingPeriods(
            accountingPeriods = accountingPeriods
          )

        } finally {
          storedProcedure.close()
        }
      }
    }
  }

  private def readAccountingPeriods(rs: ResultSet): List[AccountingPeriodsDetails] = {
    val buffer = ListBuffer[AccountingPeriodsDetails]()
    while (rs.next()) {
      buffer += AccountingPeriodsDetails(
        accountingPeriod       = rs.getBigDecimal("accounting_period"),
        apStartDate            = rs.getDate("ap_start_date").toLocalDate,
        apEndDate              = rs.getDate("ap_end_date").toLocalDate,
        apStatus               = rs.getString("ap_status"),
        taxChargePresent       = rs.getBoolean("tax_charge_present"),
        clericalIntSig         = rs.getBoolean("clerical_int_sig"),
        creditDebitInterestInd = rs.getBoolean("credit_debit_interest_ind"),
        taxTotal               = Some(rs.getBigDecimal("tax_total")),
        interestTotal          = Some(rs.getBigDecimal("interest_total")),
        penaltyTotal           = Some(rs.getBigDecimal("penalty_total")),
        payslipTotal           = Some(rs.getBigDecimal("payslip_total")),
        repayReallocTotal      = Some(rs.getBigDecimal("repay_realloc_total")),
        adjustmentTotal        = Some(rs.getBigDecimal("adjustment_total"))
      )
    }
    buffer.toList
  }
}
