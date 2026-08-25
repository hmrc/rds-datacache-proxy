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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.DisplayNeeded

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[DisplayNeededImpl])
trait DisplayNeededRepository {
  def getDisplayNeeded(taxRef: Long, accPeriod: Long): Future[DisplayNeeded]
}

class DisplayNeededImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends DisplayNeededRepository
    with Logging {

  def getDisplayNeeded(taxRef: Long, accPeriod: Long): Future[DisplayNeeded] = {
    logger.info(s"[InterestChargeSummaryDataCacheRepository][getDisplayNeeded] taxPayerReference: $taxRef, accountingPeriod: $accPeriod")
    Future {
      db.withConnection { connect =>
        val storedProcedure = connect.prepareCall("{call CT_DC_PK.isDisplayNeeded(?, ?, ?, ?, ?, ?)}")
        try {
          storedProcedure.setLong(1, taxRef)
          storedProcedure.setLong(2, accPeriod)

          storedProcedure.registerOutParameter(3, java.sql.Types.VARCHAR) // P_TAX_ISDISPLAYNEEDED_FLAG
          storedProcedure.registerOutParameter(4, java.sql.Types.VARCHAR) // P_INT_ISDISPLAYNEEDED_FLAG
          storedProcedure.registerOutParameter(5, java.sql.Types.VARCHAR) // P_PAY_ISDISPLAYNEEDED_FLAG
          storedProcedure.registerOutParameter(6, java.sql.Types.VARCHAR) // P_REPAY_ISDISPLAYNEEDED_FLAG

          storedProcedure.execute()

          DisplayNeeded(
            taxIsDisplayNeededFlag          = storedProcedure.getBoolean(3),
            interestIsDisplayNeededFlag     = storedProcedure.getBoolean(4),
            paymentIsDisplayNeededFlag      = storedProcedure.getBoolean(5),
            repayReallocIsDisplayNeededFlag = storedProcedure.getBoolean(6)
          )
        } finally {
          storedProcedure.close()
        }
      }
    }
  }

}
