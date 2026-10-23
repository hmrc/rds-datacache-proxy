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

import com.google.inject.ImplementedBy
import oracle.jdbc.OracleTypes
import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.{AccountingPeriodList, AllAccountingPeriods}

import java.sql.*
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AllAccountingPeriodsRepositoryImpl])
trait AllAccountingPeriodsRepository {
  def getAccountingPeriodList(taxRef: Long): Future[AllAccountingPeriods]
}

class AllAccountingPeriodsRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends AllAccountingPeriodsRepository
    with Logging {

  def getAccountingPeriodList(taxRef: Long): Future[AllAccountingPeriods] = {
    Future {
      db.withConnection { connect =>
        val sp = connect.prepareCall("{call CT_DC_PK.getAccountingPeriodList(?, ?)}")

        try {
          sp.setLong(1, taxRef)
          sp.registerOutParameter(2, OracleTypes.CURSOR)

          sp.execute()

          val accountingPeriodDetailsList = sp.getObject(2, classOf[ResultSet])

          val accountingPeriodsList = readAccountingPeriodsList(accountingPeriodDetailsList)

          AllAccountingPeriods(
            accountingPeriodList = accountingPeriodsList
          )

        } finally {
          sp.close()
        }
      }
    }
  }

  private def readAccountingPeriodsList(rs: ResultSet): List[AccountingPeriodList] = {
    val buffer = ListBuffer[AccountingPeriodList]()
    while (rs.next()) {
      buffer += AccountingPeriodList(
        accountingPeriod = rs.getBigDecimal("accounting_period"),
        apEndDate        = rs.getDate("ap_end_date").toLocalDate,
        taxChargePresent = rs.getString("tax_charge_present"),
        penaltyTotal     = rs.getBigDecimal("penalty_total")
      )
    }
    buffer.toList
  }
}
