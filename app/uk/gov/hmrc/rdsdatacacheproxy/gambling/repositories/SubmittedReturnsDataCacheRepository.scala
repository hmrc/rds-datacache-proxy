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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{Regime, SubmittedReturns, SubmittedReturnsItem}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait SubmittedReturnsDataSource {
  def getSubmittedReturns(regNumber: String, sortBy: Int, orderBy: String): Future[SubmittedReturns]
}

@Singleton
class SubmittedReturnsDataCacheRepository @Inject() (@NamedDatabase("gambling") mgdDb: MGDDatabase,
                                                     @NamedDatabase("gambling.gtr") gtrDb: GTRDatabase
                                                    )(implicit
  ec: ExecutionContext
) extends SubmittedReturnsDataSource
    with RepositorySupport
    with Logging {

  override def getSubmittedReturns(regNumber: String, sortBy: Int, orderBy: String): Future[SubmittedReturns] =
    Future {
      getDb(Regime.MGD, mgdDb, gtrDb).underlying.withConnection { connection =>
        val cs = connection.prepareCall("{ call MGD_DC_RTN_PCK.GET_SUBMITTED_RETURNS(?, ?, ?, ?) }")

        try {
          cs.setString(1, regNumber) // IN  P_REG_NUMBER
          cs.setString(2, orderBy) // IN  P_ORDER
          cs.setInt(3, sortBy) // IN  P_SORT_COLUMN
          cs.registerOutParameter(4, oracle.jdbc.OracleTypes.CURSOR) // OUT P_SUBMITTED_RETURNS (REF CURSOR)
          cs.execute()

          val submittedReturns: List[SubmittedReturnsItem] = {
            val rs = cs.getObject(4).asInstanceOf[java.sql.ResultSet]
            if (rs == null) Nil
            else {
              try {
                val b = List.newBuilder[SubmittedReturnsItem]

                while (rs.next()) {
                  val maybeItem =
                    for
                      consec_no      <- Option(rs.getInt("consec_no"))
                      mgd_period     <- Option(rs.getString("mgd_period"))
                      submitted_date <- Option(rs.getDate("submitted_date").toLocalDate)
                      ack_ref        <- Option(rs.getString("ack_ref"))
                    yield SubmittedReturnsItem(consec_no, mgd_period, submitted_date, ack_ref)
                  b.addAll(maybeItem.toList)
                }
                b.result()
              } finally closeQuietly(rs)
            }
          }

          SubmittedReturns(
            items = submittedReturns
          )

        } finally {
          closeQuietly(cs)
        }
      }
    }(ec)
}
