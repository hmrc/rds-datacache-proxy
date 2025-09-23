/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy.repositories


import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import scala.concurrent.{ExecutionContext, Future}
import java.sql.ResultSet
import oracle.jdbc.OracleTypes

trait CisMonthlyReturnSource {
  def getInstanceIdByTaxRef(taxOfficeNumber: String, taxOfficeReference: String): Future[Option[String]]
}

@Singleton
class CisDatacacheRepository @Inject()(
                                        @NamedDatabase("cis") db: Database        
                                      )(implicit ec: ExecutionContext)
  extends CisMonthlyReturnSource with Logging {
  
  override def getInstanceIdByTaxRef(taxOfficeNumber: String, taxOfficeReference: String): Future[Option[String]] = {
    logger.info(s"[CIS] findInstanceId(TaxOfficeNumber=$taxOfficeNumber, TaxOfficeReference=$taxOfficeReference)")
    
    Future {
      db.withConnection { conn =>
        val cs = conn.prepareCall("{ call ECISR_SEARCH_PK.getCISTaxpayerByTaxReference(?, ?, ?) }")
        try {
          cs.setString(1, taxOfficeNumber)
          cs.setString(2, taxOfficeReference)
          cs.registerOutParameter(3, OracleTypes.CURSOR)
          cs.execute()

          val rs = cs.getObject(3, classOf[ResultSet]) 
          try {
            if (rs.next()) {
              val idOpt = Option(rs.getString("UNIQUE_ID")).map(_.trim).filter(_.nonEmpty)

              if (rs.next()) {
                logger.warn(s"[CIS] findInstanceId: multiple rows for TON=$taxOfficeNumber, TOR=$taxOfficeReference; using first UNIQUE_ID")
              }

              idOpt
            } else {
              None
            }
          } finally if (rs != null) rs.close()
        } finally cs.close()
      }
    }
  }
}