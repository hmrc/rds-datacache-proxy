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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories

import play.api.db.{Database, NamedDatabase}
import play.api.{Logging, db}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{Regime, RepaymentsSummary}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait RepaymentsDataSource {
  def getRepaymentsSummary(regime: Regime, regNumber: String): Future[RepaymentsSummary]
}

@Singleton
class RepaymentsDataCacheRepository @Inject() (@NamedDatabase("gambling") mgdDb: Database, @NamedDatabase("gambling.gtr") gtrDb: Database)(implicit
  ec: ExecutionContext
) extends RepaymentsDataSource
    with RepositorySupport
    with Logging {

  override def getRepaymentsSummary(regime: Regime, regNumber: String): Future[RepaymentsSummary] =
    Future {
      getDb(regime, mgdDb, gtrDb).withConnection { connection =>
        val cs =
          regime match
            case Regime.MGD => connection.prepareCall("{ call MGD_LNP_PK.getMGDRepaymentSummary(?, ?, ?, ?, ?, ?) }")
            case _          => connection.prepareCall("{ call GTR_LNP_PK.getGTRRepaymentSummary(?, ?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, regNumber) // IN  P_REG_NUMBER
          cs.registerOutParameter(2, java.sql.Types.DATE) // OUT P_GTR_PERIOD_START_DATE
          cs.registerOutParameter(3, java.sql.Types.DATE) // OUT P_GTR_PERIOD_END_DATE
          cs.registerOutParameter(4, java.sql.Types.DECIMAL) // OUT P_ACTUAL_REPAYMENT_AMOUNT (NUMBER)
          cs.registerOutParameter(5, java.sql.Types.DECIMAL) // OUT P_REPAY_INTEREST_REPAID_AMOUNT (NUMBER)
          cs.registerOutParameter(6, java.sql.Types.DECIMAL) // OUT P_TOTAL (NUMBER)

          cs.execute()

          RepaymentsSummary(
            periodStartDate                = optDate(2, cs),
            periodEndDate                  = optDate(3, cs),
            actualRepaymentsAmount         = optDecimalFromIndex(4, cs).getOrElse(0),
            repaymentsInterestRepaidAmount = optDecimalFromIndex(5, cs).getOrElse(0),
            total                          = optDecimalFromIndex(6, cs).getOrElse(0)
          )

        } finally {
          closeQuietly(cs)
        }
      }
    }(ec)
}
