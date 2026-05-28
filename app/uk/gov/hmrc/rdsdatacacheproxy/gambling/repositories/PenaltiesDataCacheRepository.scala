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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{Penalties, PenaltyItem, Regime}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait PenaltiesDataSource {
  def getPenalties(regime: Regime, regNumber: String, paginationStart: Int, paginationMaxRows: Int): Future[Penalties]
}

@Singleton
class PenaltiesDataCacheRepository @Inject() (@NamedDatabase("gambling") mgdDb: Database, @NamedDatabase("gambling.gtr") gtrDb: Database)(implicit
  ec: ExecutionContext
) extends PenaltiesDataSource
    with RepositorySupport
    with Logging {

  override def getPenalties(regime: Regime, regNumber: String, paginationStart: Int, paginationMaxRows: Int): Future[Penalties] =
    Future {
      getDb(regime, mgdDb, gtrDb).withConnection { connection =>
        val cs =
          regime match
            case Regime.MGD => connection.prepareCall("{ call MGD_LNP_PK.getMGDPenalties(?, ?, ?, ?, ?, ?, ?, ?) }")
            case _          => connection.prepareCall("{ call GTR_LNP_PK.getGTRPenalties(?, ?, ?, ?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, regNumber) // IN  P_REG_NUMBER
          cs.setInt(2, paginationStart) // IN  P_PAGINATION_START
          cs.setInt(3, paginationMaxRows) // IN  P_PAGINATION_MAX_ROWS
          cs.registerOutParameter(4, java.sql.Types.DATE) // OUT P_GTR_PERIOD_START_DATE
          cs.registerOutParameter(5, java.sql.Types.DATE) // OUT P_GTR_PERIOD_END_DATE
          cs.registerOutParameter(6, java.sql.Types.DECIMAL) // OUT P_TOTAL (NUMBER)
          cs.registerOutParameter(7, java.sql.Types.NUMERIC) // OUT P_TOTAL_RECORDS (NUMBER)
          cs.registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR) // OUT C_PENALTIES (REF CURSOR)
          cs.execute()

          val penalties: List[PenaltyItem] = {
            val rs = cs.getObject(8).asInstanceOf[java.sql.ResultSet]
            if (rs == null) Nil
            else {
              try {
                val b = List.newBuilder[PenaltyItem]

                while (rs.next()) {
                  val maybeItem =
                    for
                      dateRaised      <- Option(rs.getDate("p_date_raised").toLocalDate)
                      descriptionCode <- Option(rs.getInt("p_desc_code"))
                      amount          <- optDecimalFromLabel("p_amount", rs)
                      periodStartDate <- Option(rs.getDate("p_period_start").toLocalDate)
                      periodEndDate   <- Option(rs.getDate("p_period_end").toLocalDate)
                    yield PenaltyItem(dateRaised, descriptionCode, amount, periodStartDate, periodEndDate)
                  b.addAll(maybeItem.toList)
                }
                b.result()
              } finally closeQuietly(rs)
            }
          }

          Penalties(
            periodStartDate = optDate(4, cs),
            periodEndDate   = optDate(5, cs),
            total           = optDecimalFromIndex(6, cs).getOrElse(0),
            totalRecords    = optInt(7, cs).getOrElse(0),
            items           = penalties
          )

        } finally {
          closeQuietly(cs)
        }
      }
    }(ec)
}
