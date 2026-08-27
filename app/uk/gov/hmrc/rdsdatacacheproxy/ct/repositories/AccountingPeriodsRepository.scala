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
import play.api.db.Database
import play.db.NamedDatabase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{RdsAccountingPeriod, RdsAccountingPeriodsRowResponse}

import java.sql.{Connection, ResultSet}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AccountingPeriodsRepositoryImpl])
trait AccountingPeriodsRepository {
  def getAccountingPeriods(taxPayerReference: Long): Future[RdsAccountingPeriod]
}

class AccountingPeriodsRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends AccountingPeriodsRepository
    with RepositoryDataSupport
    with Logging {

  override def getAccountingPeriods(taxPayerReference: Long): Future[RdsAccountingPeriod] = {
    logger.info(
      s"Retrieving accounting Periods for taxPayerReference: $taxPayerReference"
    )
    Future {
      db.withConnection { connection =>
        retrieveAccountingPeriodsFromDB(connection, taxPayerReference)
      }
    }
  }

  private def processAccountingPeriodsRowResponse(rs: ResultSet): RdsAccountingPeriodsRowResponse =
    RdsAccountingPeriodsRowResponse(
      accountingPeriod       = rs.getBigDecimal("accounting_period"),
      apStartDate            = rs.getDate("ap_start_date").toLocalDate,
      apEndDate              = rs.getDate("ap_end_date").toLocalDate,
      apStatus               = rs.getString("ap_status"),
      taxChargePresent       = Option(rs.getString("tax_charge_present")),
      clericalIntSig         = Option(rs.getString("clerical_int_sig")),
      creditDebitInterestInd = Option(rs.getString("credit_debit_interest_ind")),
      taxTotal               = Option(rs.getBigDecimal("tax_total")),
      interestTotal          = Option(rs.getBigDecimal("interest_total")),
      penaltyTotal           = Option(rs.getBigDecimal("penalty_total")),
      payslipTotal           = Option(rs.getBigDecimal("payslip_total")),
      repayReallocTotal      = Option(rs.getBigDecimal("repay_realloc_total")),
      adjustmentTotal        = Option(rs.getBigDecimal("adjustment_total"))
    )

  private def retrieveAccountingPeriodsFromDB(conn: Connection, taxRef: Long): RdsAccountingPeriod = {
    val context: String = s"Retrieving AccountingPeriods"

    val cs = conn.prepareCall("{call CT_DC_PK.getAccountPeriods(?, ?)}")
    try {
      cs.setLong(1, taxRef)

      cs.registerOutParameter(2, OracleTypes.CURSOR)

      cs.execute()

      val rdsAccountingPeriodsRowResponse: List[RdsAccountingPeriodsRowResponse] =
        processResultSetList(cs, 2, processAccountingPeriodsRowResponse, context)

      RdsAccountingPeriod(
        accountingPeriods = rdsAccountingPeriodsRowResponse
      )

    } finally {
      cs.close()
    }

  }
}
