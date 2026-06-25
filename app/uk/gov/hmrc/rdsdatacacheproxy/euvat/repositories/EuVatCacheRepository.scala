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

package uk.gov.hmrc.rdsdatacacheproxy.euvat.repositories

import oracle.jdbc.OracleTypes
import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.euvat.models.responses.TradersKnownFacts

import java.sql.ResultSet
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Using

class EuVatCacheRepository @Inject() (@NamedDatabase("euvat") db: Database)(implicit ec: ExecutionContext) extends Logging {

  def getTraderByVrn(vrn: String): Future[Option[TradersKnownFacts]] = {
    logger.info(s"************* calling stored procedure getTraderByVrn for VRN: $vrn")
    Future {
      db.withConnection { connection =>
        Using.resource(connection.prepareCall("{call EUVAT_FILING_DC_KF.getTraderByVRN(?, ?)}")) { stmt =>
          stmt.setInt("p_vrn", vrn.toInt)
          stmt.registerOutParameter("p_trader", OracleTypes.CURSOR)

          stmt.execute()

          val rs = stmt.getObject("p_trader", classOf[ResultSet])
          Using.resource(rs) { cursor =>
            if (!cursor.next()) {
              logger.warn(s"No trader known facts returned from stored procedure for vrn: $vrn")
              None
            } else {
              Some(
                TradersKnownFacts(
                  vatRegNumber           = cursor.getInt("vat_reg_number"),
                  traderName             = Option(cursor.getString("trader_name")),
                  addressLine1           = Option(cursor.getString("bus_address_1")),
                  addressLine2           = Option(cursor.getString("bus_address_2")),
                  addressLine3           = Option(cursor.getString("bus_address_3")),
                  addressLine4           = Option(cursor.getString("bus_address_4")),
                  addressLine5           = Option(cursor.getString("bus_address_5")),
                  postCode               = Option(cursor.getString("bus_postcode")),
                  tradeClass             = Option(cursor.getString("trade_class")),
                  dateOfRegistration     = Option(cursor.getTimestamp("date_of_reg")).map(_.toLocalDateTime),
                  dateOfDeregistration   = Option(cursor.getTimestamp("date_of_dereg")).map(_.toLocalDateTime),
                  missingTraderIndicator = Option(cursor.getString("missing_trader_ind"))
                )
              )
            }
          }
        }

      }

    }
  }
}
