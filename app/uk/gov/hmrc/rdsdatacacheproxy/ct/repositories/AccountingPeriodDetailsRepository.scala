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
import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.APBalancedItem
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AccountingPeriodDetailsRepositoryImpl])
trait AccountingPeriodDetailsRepository {
  def getIsAPBalanced(taxRef: Long, accPeriod: Long): Future[APBalancedItem]
}

class AccountingPeriodDetailsRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends AccountingPeriodDetailsRepository
    with Logging {

  def getIsAPBalanced(taxRef: Long, accPeriod: Long): Future[APBalancedItem] = {
    logger.info(s"Input request: taxRef, accPeriod: <$taxRef>, <$accPeriod>")
    Future {
      db.withConnection { connection =>
        val cs = connection.prepareCall("{call CT_LNP_PK.isAPBalanced(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")

        cs.setLong(1, taxRef)
        cs.setLong(2, accPeriod)

        cs.registerOutParameter(3, java.sql.Types.VARCHAR) // p_Is_AP_Balanced
        cs.registerOutParameter(4, java.sql.Types.VARCHAR) // p_LPI_Calc_Flag
        cs.registerOutParameter(5, java.sql.Types.VARCHAR) // p_CR_DB_Calc_Flag

        cs.registerOutParameter(6, java.sql.Types.DECIMAL) // p_CR_Interest_Amount
        cs.registerOutParameter(7, java.sql.Types.DECIMAL) // p_DB_Interest_Amount
        cs.registerOutParameter(8, java.sql.Types.DECIMAL) // p_LPI_Interest_Amount
        cs.registerOutParameter(9, java.sql.Types.DECIMAL) // p_Repayment_Interest_Amount
        cs.registerOutParameter(10, java.sql.Types.DECIMAL) // p_Amount_Due_For_AP
        cs.execute()

        try {
          APBalancedItem(
            isApBalanced              = Option(cs.getString(3)),
            lpiCalcFlag               = Option(cs.getString(4)),
            crDbCalcFlag              = Option(cs.getString(5)),
            creditInterestAmount      = Option(cs.getBigDecimal(6)),
            debitInterestAmount       = Option(cs.getBigDecimal(7)),
            latePaymentInterestAmount = Option(cs.getBigDecimal(8)),
            repaymentInterestAmount   = Option(cs.getBigDecimal(9)),
            amountDueForAp            = Option(cs.getBigDecimal(10))
          )
        } finally {
          cs.close()
        }
      }
    }
  }

}
