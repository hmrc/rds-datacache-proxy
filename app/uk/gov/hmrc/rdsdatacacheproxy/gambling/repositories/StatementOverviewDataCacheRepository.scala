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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError.RecordNotFound
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{Regime, StatementOverview}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait StatementOverviewDataSource {
  def getStatementOverview(regime: Regime, regNumber: String): Future[Either[StatementError, StatementOverview]]
}

@Singleton
class StatementOverviewDataCacheRepository @Inject() (
  @NamedDatabase("gambling") mgdDb: MGDDatabase,
  @NamedDatabase("gambling.gtr") gtrDb: GTRDatabase
)(implicit ec: ExecutionContext)
    extends StatementOverviewDataSource
    with RepositorySupport
    with Logging {

  override def getStatementOverview(regime: Regime, regNumber: String): Future[Either[StatementError, StatementOverview]] =
    Future {
      getDb(regime, mgdDb, gtrDb).underlying.withConnection { connection =>
        val cs =
          regime match
            case Regime.MGD => connection.prepareCall("{ call MGD_LNP_PK.getMGDAccountOverview(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }")
            case _          => connection.prepareCall("{ call GTR_LNP_PK.getGTRAccountOverview(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, regNumber) // IN  P_GTR_REGISTRATION_NUMBER
          cs.registerOutParameter(2, java.sql.Types.DATE) // OUT P_GTR_PERIOD_START_DATE
          cs.registerOutParameter(3, java.sql.Types.DATE) // OUT P_GTR_PERIOD_END_DATE
          cs.registerOutParameter(4, java.sql.Types.DECIMAL) // OUT P_TOTAL
          cs.registerOutParameter(5, java.sql.Types.DECIMAL) // OUT P_BALANCE
          cs.registerOutParameter(6, java.sql.Types.DECIMAL) // OUT P_AMOUNT_DECLARED
          cs.registerOutParameter(7, java.sql.Types.DECIMAL) // OUT P_ASSESSMENTS
          cs.registerOutParameter(8, java.sql.Types.DECIMAL) // OUT P_PENALTIES
          cs.registerOutParameter(9, java.sql.Types.DECIMAL) // OUT P_ADJUSTMENTS
          cs.registerOutParameter(10, java.sql.Types.DECIMAL) // OUT P_REALLOCATIONS
          cs.registerOutParameter(11, java.sql.Types.DECIMAL) // OUT P_OTHER_ASSESSMENTS
          cs.registerOutParameter(12, java.sql.Types.DECIMAL) // OUT P_INTEREST
          cs.registerOutParameter(13, java.sql.Types.DECIMAL) // OUT P_PAYMENTS
          cs.registerOutParameter(14, java.sql.Types.DECIMAL) // OUT P_REPAYMENTS (null if zero)
          cs.execute()

          // P_TOTAL being null signals operator not registered → 404
          optDecimalFromIndex(4, cs) match {
            case None => Left(RecordNotFound)
            case Some(total) =>
              Right(
                StatementOverview(
                  gtrPeriodStartDate = optDate(2, cs),
                  gtrPeriodEndDate   = optDate(3, cs),
                  total              = total,
                  balance            = optDecimalFromIndex(5, cs).getOrElse(0),
                  amountDeclared     = optDecimalFromIndex(6, cs).getOrElse(0),
                  assessments        = optDecimalFromIndex(7, cs).getOrElse(0),
                  penalties          = optDecimalFromIndex(8, cs).getOrElse(0),
                  adjustments        = optDecimalFromIndex(9, cs).getOrElse(0),
                  reallocations      = optDecimalFromIndex(10, cs).getOrElse(0),
                  otherAssessments   = optDecimalFromIndex(11, cs).getOrElse(0),
                  interest           = optDecimalFromIndex(12, cs).getOrElse(0),
                  payments           = optDecimalFromIndex(13, cs).getOrElse(0),
                  repayments         = optDecimalFromIndex(14, cs)
                )
              )
          }
        } finally {
          closeQuietly(cs)
        }
      }
    }(ec)
}
