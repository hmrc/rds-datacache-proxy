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
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.*
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait GamblingDataSource {
  def getReturnSummary(mgdRegNumber: String): Future[ReturnSummary]
  def getBusinessName(mgdRegNumber: String): Future[BusinessName]
}

@Singleton
class GamblingDataCacheRepository @Inject() (
  @NamedDatabase("gambling") db: Database
)(implicit ec: ExecutionContext)
    extends GamblingDataSource
    with Logging {

  override def getReturnSummary(mgdRegNumber: String): Future[ReturnSummary] = {

    logger.info(s"[GamblingDataCacheRepository][getReturnSummary] mgdRegNumber=$mgdRegNumber")

    Future {
      db.withConnection { conn =>

        val cs = conn.prepareCall("{ call MGD_DC_RTN_PCK.GET_RETURN_SUMMARY(?, ?) }")

        try {
          cs.setString(1, mgdRegNumber)
          cs.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
          cs.execute()

          val rs = cs.getObject(2).asInstanceOf[java.sql.ResultSet]

          if (rs == null) {
            val msg = s"Null cursor returned for mgdRegNumber=$mgdRegNumber"
            logger.error(s"[GamblingDataCacheRepository] $msg")
            throw new RuntimeException(msg)
          }

          try {
            if (rs.next()) {
              ReturnSummary(
                mgdRegNumber   = rs.getString("MGD_REG_NUMBER"),
                returnsDue     = rs.getInt("RETURNS_DUE"),
                returnsOverdue = rs.getInt("RETURNS_OVERDUE")
              )
            } else {
              val msg = s"Empty result set for mgdRegNumber=$mgdRegNumber"
              logger.error(s"[GamblingDataCacheRepository] $msg")
              throw new RuntimeException(msg)
            }
          } finally {
            rs.close()
          }
        } finally {
          cs.close()
        }
      }
    }(ec)
  }
  override def getBusinessName(mgdRegNumber: String): Future[BusinessName] = {

    logger.info(s"[GamblingDataCacheRepository][getBusinessName] mgdRegNumber=$mgdRegNumber")

    Future {
      db.withConnection { conn =>

        val cs = conn.prepareCall("{ call MGD_DC_VARIATION_PK.GET_BUSINESS_NAME(?, ?) }")

        try {
          cs.setString(1, mgdRegNumber)
          cs.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
          cs.execute()

          val rs = cs.getObject(2).asInstanceOf[java.sql.ResultSet]

          if (rs == null) {
            val msg = s"Null cursor returned for mgdRegNumber=$mgdRegNumber"
            logger.error(s"[GamblingDataCacheRepository] $msg")
            throw new RuntimeException(msg)
          }

          try {
            if (rs.next()) {
              BusinessName(
                mgdRegNumber      = rs.getString("MGD_REG_NUMBER"),
                solePropTitle     = rs.getString("SOLE_PROP_TITLE"),
                solePropFirstName = rs.getString("SOLE_PROP_FIRST_NAME"),
                solePropMidName   = rs.getString("SOLE_PROP_MIDDLE_NAME"),
                solePropLastName  = rs.getString("SOLE_PROP_LAST_NAME"),
                businessName      = rs.getString("BUSINESS_NAME"),
                businessType      = rs.getString("BUSINESS_TYPE"),
                tradingName       = rs.getString("TRADING_NAME"),
                systemDate        = rs.getDate("SYSTEM_DATE")
              )
            } else {
              val msg = s"Empty result set for mgdRegNumber=$mgdRegNumber"
              logger.error(s"[GamblingDataCacheRepository] $msg")
              throw new RuntimeException(msg)
            }
          } finally {
            rs.close()
          }
        } finally {
          cs.close()
        }
      }
    }(ec)
  }
}
