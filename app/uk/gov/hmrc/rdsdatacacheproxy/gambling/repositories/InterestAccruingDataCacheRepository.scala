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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{InterestAccruing, InterestAccruingItem, Regime}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait InterestAccruingDataSource {
  def getInterestAccruing(regime: Regime,
                          regNumber: String,
                          interestId: String,
                          paginationStart: Int,
                          paginationMaxRows: Int
                         ): Future[InterestAccruing]
}

@Singleton
class InterestAccruingDataCacheRepository @Inject() (
  @NamedDatabase("gambling") mgdDb: MGDDatabase,
  @NamedDatabase("gambling.gtr") gtrDb: GTRDatabase
)(implicit ec: ExecutionContext)
    extends InterestAccruingDataSource
    with RepositorySupport
    with Logging {

  override def getInterestAccruing(regime: Regime,
                                   regNumber: String,
                                   interestId: String,
                                   paginationStart: Int,
                                   paginationMaxRows: Int
                                  ): Future[InterestAccruing] =
    Future {
      getDb(regime, mgdDb, gtrDb).underlying.withConnection { connection =>
        val cs =
          regime match
            case Regime.MGD => connection.prepareCall("{ call MGD_LNP_PK.getMGDAccruingDrilldown(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }")
            case _          => connection.prepareCall("{ call GTR_LNP_PK.getGTRAccruingDrilldown(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, regNumber) // IN  P_REGISTRATION_NUMBER
          cs.setString(2, interestId) // IN  P_INTEREST_ID
          cs.setInt(3, paginationStart) // IN  P_START
          cs.setInt(4, paginationMaxRows) // IN  P_MAX_ROWS
          cs.registerOutParameter(5, java.sql.Types.DATE) // OUT P_PERIOD_START_DATE
          cs.registerOutParameter(6, java.sql.Types.DATE) // OUT P_PERIOD_END_DATE
          cs.registerOutParameter(7, java.sql.Types.DECIMAL) // OUT P_TOTAL (NUMBER)
          cs.registerOutParameter(8, java.sql.Types.NUMERIC) // OUT P_TOTAL_RECORDS (NUMBER)
          cs.registerOutParameter(9, java.sql.Types.NUMERIC) // OUT P_DESC_CODE (NUMBER)
          cs.registerOutParameter(10, oracle.jdbc.OracleTypes.CURSOR) // OUT C_INTEREST_ACCRUING_DRILLDOWN (REF CURSOR)
          cs.execute()

          val items: List[InterestAccruingItem] = {
            val rs = cs.getObject(10).asInstanceOf[java.sql.ResultSet]
            if (rs == null) Nil
            else {
              try {
                val b = List.newBuilder[InterestAccruingItem]

                while (rs.next()) {
                  val maybeItem =
                    for
                      interestOn <- optDecimalFromLabel("p_interest_on", rs)
                      dateFrom   <- Option(rs.getDate("p_date_from").toLocalDate)
                      dateTo     <- Option(rs.getDate("p_date_to").toLocalDate)
                      noOfDays   <- optDecimalFromLabel("p_no_of_days", rs)
                      rate       <- optDecimalFromLabel("p_rate", rs)
                      amount     <- optDecimalFromLabel("p_amount", rs)
                    yield InterestAccruingItem(interestOn, dateFrom, dateTo, noOfDays, rate, amount)
                  b.addAll(maybeItem.toList)
                }
                b.result()
              } finally closeQuietly(rs)
            }
          }

          InterestAccruing(
            periodStartDate = optDate(5, cs),
            periodEndDate   = optDate(6, cs),
            total           = optDecimalFromIndex(7, cs).getOrElse(0),
            totalRecords    = optInt(8, cs).getOrElse(0),
            descriptionCode = optInt(9, cs),
            items           = items
          )

        } finally {
          closeQuietly(cs)
        }
      }
    }(ec)
}
