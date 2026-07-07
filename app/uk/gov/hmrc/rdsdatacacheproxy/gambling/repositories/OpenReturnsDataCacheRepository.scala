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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{OpenReturnPeriodItem, OpenReturnPeriods, Regime}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait OpenReturnsDataSource {
  def getOpenReturnPeriods(regime: Regime, regNumber: String, sortBy: Int, orderBy: String): Future[OpenReturnPeriods]
}

@Singleton
class OpenReturnsDataCacheRepository @Inject() (
  @NamedDatabase("gambling") mgdDb: MGDDatabase,
  @NamedDatabase("gambling.gtr") gtrDb: GTRDatabase
)(implicit ec: ExecutionContext)
    extends OpenReturnsDataSource
    with RepositorySupport
    with Logging {

  override def getOpenReturnPeriods(regime: Regime, regNumber: String, sortBy: Int, orderBy: String): Future[OpenReturnPeriods] =
    Future {
      getDb(Regime.MGD, mgdDb, gtrDb).underlying.withConnection { connection =>
        val cs = {
          regime match
            case Regime.MGD => connection.prepareCall("{ call MGD_DC_RTN_PCK.GET_OPEN_PERIODS(?, ?, ?, ?) }")
            case _          => throw new RuntimeException(s"Regime $regime is not supported for getOpenReturnPeriods")
        }

        try {
          cs.setString(1, regNumber) // IN  P_MGD_REG_NUMBER
          cs.setString(2, orderBy) // IN  P_ORDER
          cs.setInt(3, sortBy) // IN  P_SORT_COLUMN
          cs.registerOutParameter(4, oracle.jdbc.OracleTypes.CURSOR) // OUT P_OPEN_PERIODS (REF CURSOR)
          cs.execute()

          val openPeriods: List[OpenReturnPeriodItem] = {
            val rs = cs.getObject(4).asInstanceOf[java.sql.ResultSet]
            if (rs == null) Nil
            else {
              try {
                val b = List.newBuilder[OpenReturnPeriodItem]

                while (rs.next()) {
                  val maybeItem =
                    for
                      consecNo  <- Option(rs.getInt("consec_no"))
                      mgdPeriod <- Option(rs.getString("mgd_period"))
                      dueDate   <- Option(rs.getDate("due_date").toLocalDate)
                      status    <- Option(rs.getInt("status"))
                    yield OpenReturnPeriodItem(consecNo, mgdPeriod, dueDate, status)
                  b.addAll(maybeItem.toList)
                }
                b.result()
              } finally closeQuietly(rs)
            }
          }

          OpenReturnPeriods(openPeriods = openPeriods)

        } finally {
          closeQuietly(cs)
        }
      }
    }(ec)
}
