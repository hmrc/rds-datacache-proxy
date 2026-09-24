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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.PeriodWithinRange

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[GroupPaymentPeriodInValidRangeRepositoryImpl])
trait GroupPaymentPeriodInValidRangeRepository {
  def getGroupPaymentPeriodInValidRange(gpaUTR: Long, nominatedCompanyUTR: Long, pPeriod: Long, pMonthRestriction: Long): Future[PeriodWithinRange]
}

class GroupPaymentPeriodInValidRangeRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends GroupPaymentPeriodInValidRangeRepository
    with Logging {

  def getGroupPaymentPeriodInValidRange(gpaUTR: Long,
                                        nominatedCompanyUTR: Long,
                                        pPeriod: Long,
                                        pMonthRestriction: Long
                                       ): Future[PeriodWithinRange] = {
    logger.info(
      s"Input request: gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction: <$gpaUTR>, <$nominatedCompanyUTR>, <$pPeriod>, <$pMonthRestriction>"
    )
    Future {
      db.withConnection { connect =>
        val storedProcedure = connect.prepareCall("{call CT_DC_PK.isGrpPaymntPeriodInValidRange(?, ?, ?, ?, ?)}")
        try {
          storedProcedure.setLong(1, gpaUTR)
          storedProcedure.setLong(2, nominatedCompanyUTR)
          storedProcedure.setLong(3, pPeriod)
          storedProcedure.setLong(4, pMonthRestriction)

          storedProcedure.registerOutParameter(5, java.sql.Types.VARCHAR) // pIS_PERIOD_WITHIN_RANGE

          storedProcedure.execute()

          PeriodWithinRange(
            isPeriodWithinRange = storedProcedure.getString(5)
          )
        } finally {
          storedProcedure.close()
        }
      }
    }
  }

}
