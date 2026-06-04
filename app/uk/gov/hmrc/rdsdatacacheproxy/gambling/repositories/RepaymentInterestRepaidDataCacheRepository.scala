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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{Regime, RepaymentInterestRepaid, RepaymentInterestRepaidItem}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait RepaymentInterestRepaidDataSource {
  def getRepaymentInterestRepaid(regime: Regime, regNumber: String, paginationStart: Int, paginationMaxRows: Int): Future[RepaymentInterestRepaid]
}

@Singleton
class RepaymentInterestRepaidDataCacheRepository @Inject() (@NamedDatabase("gambling") mgdDb: MGDDatabase,
                                                            @NamedDatabase("gambling.gtr") gtrDb: GTRDatabase
                                                           )(implicit
  ec: ExecutionContext
) extends RepaymentInterestRepaidDataSource
    with RepositorySupport
    with Logging {

  override def getRepaymentInterestRepaid(regime: Regime,
                                          regNumber: String,
                                          paginationStart: Int,
                                          paginationMaxRows: Int
                                         ): Future[RepaymentInterestRepaid] =
    Future {
      getDb(regime, mgdDb, gtrDb).underlying.withConnection { connection =>
        val cs =
          regime match
            case Regime.MGD => connection.prepareCall("{ call MGD_LNP_PK.getMGDRepaymentInterestRepaid(?, ?, ?, ?, ?, ?, ?, ?) }")
            case _          => connection.prepareCall("{ call GTR_LNP_PK.getGTRRepaymentInterestRepaid(?, ?, ?, ?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, regNumber) // IN  P_REG_NUMBER
          cs.setInt(2, paginationStart) // IN  P_PAGINATION_START
          cs.setInt(3, paginationMaxRows) // IN  P_PAGINATION_MAX_ROWS
          cs.registerOutParameter(4, java.sql.Types.DATE) // OUT P_GTR_PERIOD_START_DATE
          cs.registerOutParameter(5, java.sql.Types.DATE) // OUT P_GTR_PERIOD_END_DATE
          cs.registerOutParameter(6, java.sql.Types.DECIMAL) // OUT P_TOTAL (NUMBER)
          cs.registerOutParameter(7, java.sql.Types.NUMERIC) // OUT P_TOTAL_RECORDS (NUMBER)
          cs.registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR) // OUT C_REPAYMENT_INTEREST_REPAID(REF CURSOR)
          cs.execute()

          val repaymentInterestRepaid: List[RepaymentInterestRepaidItem] = {
            val rs = cs.getObject(8).asInstanceOf[java.sql.ResultSet]
            if (rs == null) Nil
            else {
              try {
                val b = List.newBuilder[RepaymentInterestRepaidItem]

                while (rs.next()) {
                  val maybeItem =
                    for
                      transactionDate <- Option(rs.getDate("p_transaction_date").toLocalDate)
                      amount          <- optDecimalFromLabel("p_amount", rs)
                    yield RepaymentInterestRepaidItem(transactionDate, amount)
                  b.addAll(maybeItem.toList)
                }
                b.result()
              } finally closeQuietly(rs)
            }
          }

          RepaymentInterestRepaid(
            periodStartDate = optDate(4, cs),
            periodEndDate   = optDate(5, cs),
            total           = optDecimalFromIndex(6, cs).getOrElse(0),
            totalRecords    = optInt(7, cs).getOrElse(0),
            items           = repaymentInterestRepaid
          )

        } finally {
          closeQuietly(cs)
        }
      }
    }(ec)
}
