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

package uk.gov.hmrc.rdsdatacacheproxy.mgd.repositories

import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.mgd.models.ReturnSummary
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait MgdDataSource {
  def getReturnSummary(mgdRegNumber: String): Future[ReturnSummary]
}

@Singleton
class MgdDatacacheRepository @Inject() (@NamedDatabase("Mgd") db: Database)(implicit ec: ExecutionContext) extends MgdDataSource with Logging {

  override def getReturnSummary(mgdRegNumber: String): Future[ReturnSummary] = {

    logger.info(s"[Mgd] getReturnSummary(mgd_reg_number=$mgdRegNumber")
    Future {
      db.withConnection { conn =>

        val cs = conn.prepareCall("{ call MGD_DC_RTN_PCK.GET_RETURN_SUMMARY(?, ?) }")

        try {
          cs.setString(1, mgdRegNumber)
          cs.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
          cs.execute()

          val rs = cs.getObject(2, classOf[java.sql.ResultSet])
          try {
            rs.next()
            ReturnSummary(
              mgdRegNumber   = rs.getString("MGD_REG_NUMBER"),
              returnsDue     = rs.getInt("RETURNS_DUE"),
              returnsOverdue = rs.getInt("RETURNS_OVERDUE")
            )
          } finally {
            if (rs != null) rs.close()
          }
        } finally {
          cs.close()
        }
      }
    }
  }
}
