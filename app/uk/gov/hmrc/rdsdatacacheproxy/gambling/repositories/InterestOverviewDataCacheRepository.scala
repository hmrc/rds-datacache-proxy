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

import play.api.Logging
import play.api.db.NamedDatabase
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{InterestOverview, Regime}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait InterestOverviewDataSource {
  def getInterestOverview(regime: Regime, regNumber: String): Future[Either[StatementError, InterestOverview]]
}

@Singleton
class InterestOverviewDataCacheRepository @Inject() (@NamedDatabase("gambling") mgdDb: MGDDatabase,
                                                     @NamedDatabase("gambling.gtr") gtrDb: GTRDatabase
                                                    )(implicit
  ec: ExecutionContext
) extends InterestOverviewDataSource
    with RepositorySupport
    with Logging {

  override def getInterestOverview(regime: Regime, regNumber: String): Future[Either[StatementError, InterestOverview]] =
    Future {
      getDb(regime, mgdDb, gtrDb).underlying.withConnection { connection =>
        val cs =
          regime match
            case Regime.MGD => connection.prepareCall("{ call MGD_LNP_PK.getMGDInterestOverview(?, ?, ?, ?, ?, ?, ?) }")
            case _          => connection.prepareCall("{ call GTR_LNP_PK.getGTRInterestOverview(?, ?, ?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, regNumber) // IN  P_REG_NUMBER
          cs.registerOutParameter(2, java.sql.Types.DATE) // OUT P_GTR_PERIOD_START_DATE
          cs.registerOutParameter(3, java.sql.Types.DATE) // OUT P_GTR_PERIOD_END_DATE
          cs.registerOutParameter(4, java.sql.Types.DECIMAL) // OUT P_INTEREST_AMOUNT (NUMBER)
          cs.registerOutParameter(5, java.sql.Types.DECIMAL) // OUT P_INTEREST_ACCRUING_AMOUNT (NUMBER)
          cs.registerOutParameter(6, java.sql.Types.DECIMAL) // OUT P_REPAYMENT_INTEREST_AMOUNT (NUMBER)
          cs.registerOutParameter(7, java.sql.Types.DECIMAL) // OUT P_TOTAL (NUMBER)

          cs.execute()

          Right(
            InterestOverview(
              periodStartDate         = optDate(2, cs),
              periodEndDate           = optDate(3, cs),
              interestAmount          = optDecimalFromIndex(4, cs).getOrElse(0),
              interestAccruingAmount  = optDecimalFromIndex(5, cs).getOrElse(0),
              repaymentInterestAmount = optDecimalFromIndex(6, cs).getOrElse(0),
              total                   = optDecimalFromIndex(7, cs).getOrElse(0)
            )
          )

        } finally {
          closeQuietly(cs)
        }
      }
    }(ec)
}
