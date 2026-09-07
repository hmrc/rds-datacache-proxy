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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{ActualRepaymentItem, ActualRepayments, Regime, RepaymentsSummary}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait RepaymentsDataSource {
  def getRepaymentsSummary(regime: Regime, regNumber: String): Future[Either[StatementError, RepaymentsSummary]]
  def getActualRepayments(regime: Regime, regNumber: String, pageStart: Int, pageMaxRows: Int): Future[ActualRepayments]
}

@Singleton
class RepaymentsDataCacheRepository @Inject() (@NamedDatabase("gambling") mgdDb: MGDDatabase, @NamedDatabase("gambling.gtr") gtrDb: GTRDatabase)(
  implicit ec: ExecutionContext
) extends RepaymentsDataSource
    with RepositorySupport
    with Logging {

  override def getRepaymentsSummary(regime: Regime, regNumber: String): Future[Either[StatementError, RepaymentsSummary]] =
    Future {
      getDb(regime, mgdDb, gtrDb).underlying.withConnection { connection =>
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

          Right(
            RepaymentsSummary(
              periodStartDate                = optDate(2, cs),
              periodEndDate                  = optDate(3, cs),
              actualRepaymentsAmount         = optDecimalFromIndex(4, cs).getOrElse(0),
              repaymentsInterestRepaidAmount = optDecimalFromIndex(5, cs).getOrElse(0),
              total                          = optDecimalFromIndex(6, cs).getOrElse(0)
            )
          )

        } finally {
          closeQuietly(cs)
        }
      }
    }(ec)

  override def getActualRepayments(regime: Regime, regNumber: String, pageStart: Int, pageMaxRows: Int): Future[ActualRepayments] =
    Future {
      getDb(regime, mgdDb, gtrDb).underlying.withConnection { connection =>
        val cs =
          regime match
            case Regime.MGD => connection.prepareCall("{ call MGD_LNP_PK.getMGDActualRepayments(?, ?, ?, ?, ?, ?, ?, ?) }")
            case _          => connection.prepareCall("{ call GTR_LNP_PK.getGTRActualRepayments(?, ?, ?, ?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, regNumber) // IN P_REG_NUMBER
          cs.setInt(2, pageStart) // IN P_START
          cs.setInt(3, pageMaxRows) // IN P_MAX_ROWS
          cs.registerOutParameter(4, java.sql.Types.DATE) // OUT P_PERIOD_START_DATE
          cs.registerOutParameter(5, java.sql.Types.DATE) // OUT P_PERIOD_END_DATE
          cs.registerOutParameter(6, java.sql.Types.DECIMAL) // OUT P_TOTAL
          cs.registerOutParameter(7, java.sql.Types.NUMERIC) // OUT P_TOTAL_RECORDS
          cs.registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR) // OUT C_ACTUAL_REPAYMENTS

          cs.execute()

          val items: List[ActualRepaymentItem] = {
            val rs = cs.getObject(8).asInstanceOf[java.sql.ResultSet]
            if (rs == null) Nil
            else {
              try {
                val b = List.newBuilder[ActualRepaymentItem]
                while (rs.next()) {
                  val maybeItem =
                    for
                      transactionDate <- Option(rs.getDate("p_transaction_date")).map(_.toLocalDate)
                      amount          <- optDecimalFromLabel("p_amount", rs)
                    yield ActualRepaymentItem(transactionDate, amount)
                  b.addAll(maybeItem.toList)
                }
                b.result()
              } finally closeQuietly(rs)
            }
          }

          ActualRepayments(
            periodStartDate = optDate(4, cs),
            periodEndDate   = optDate(5, cs),
            total           = optDecimalFromIndex(6, cs).getOrElse(0),
            totalRecords    = optInt(7, cs).getOrElse(0),
            items           = items
          )

        } finally {
          closeQuietly(cs)
        }
      }
    }(ec)
}
