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
import java.sql.{CallableStatement, ResultSet}
import oracle.jdbc.OracleTypes
import uk.gov.hmrc.rdsdatacacheproxy.models.CisTaxpayer

trait CisMonthlyReturnSource {
  def getCisTaxpayerByTaxRef(taxOfficeNumber: String, taxOfficeReference: String): Future[Option[CisTaxpayer]]
}

@Singleton
class CisDatacacheRepository @Inject() (
  @NamedDatabase("cis") db: Database
)(implicit ec: ExecutionContext)
    extends CisMonthlyReturnSource
    with Logging {

  private def str(rs: ResultSet, col: String): Option[String] =
    Option(rs.getString(col)).map(_.trim).filter(_.nonEmpty)

  private def toCisTaxpayer(rs: ResultSet): CisTaxpayer =
    CisTaxpayer(
      uniqueId          = str(rs, "UNIQUE_ID").getOrElse(""),
      taxOfficeNumber   = str(rs, "TAX_OFFICE_NUMBER").getOrElse(""),
      taxOfficeRef      = str(rs, "TAX_OFFICE_REF").getOrElse(""),
      aoDistrict        = str(rs, "AO_DISTRICT"),
      aoPayType         = str(rs, "AO_PAY_TYPE"),
      aoCheckCode       = str(rs, "AO_CHECK_CODE"),
      aoReference       = str(rs, "AO_REFERENCE"),
      validBusinessAddr = str(rs, "VALID_BUSINESS_ADDR"),
      correlation       = str(rs, "CORRELATION"),
      ggAgentId         = str(rs, "GG_AGENT_ID"),
      employerName1     = str(rs, "EMPLOYER_NAME1"),
      employerName2     = str(rs, "EMPLOYER_NAME2"),
      agentOwnRef       = str(rs, "AGENT_OWN_REF"),
      schemeName        = str(rs, "SCHEME_NAME"),
      utr               = str(rs, "UTR"),
      enrolledSig       = str(rs, "ENROLLED_SIG")
    )

  override def getCisTaxpayerByTaxRef(
    taxOfficeNumber: String,
    taxOfficeReference: String
  ): Future[Option[CisTaxpayer]] = {
    logger.info(s"[CIS] getCisTaxpayerByTaxRef(TON=$taxOfficeNumber, TOR=$taxOfficeReference)")
    Future {
      db.withConnection { conn =>
        val cs: CallableStatement =
          conn.prepareCall("{ call ECISR_SEARCH_PK.getCISTaxpayerByTaxReference(?, ?, ?) }")

        try {
          cs.setString(1, taxOfficeNumber)
          cs.setString(2, taxOfficeReference)
          cs.registerOutParameter(3, OracleTypes.CURSOR)
          cs.execute()

          val rs = cs.getObject(3, classOf[ResultSet])
          try {
            if (rs != null && rs.next()) {
              val first = toCisTaxpayer(rs)

              if (rs.next()) {
                logger.warn(
                  s"[CIS] getCisTaxpayerByTaxRef: multiple rows for TON=$taxOfficeNumber, TOR=$taxOfficeReference; using first row (UNIQUE_ID=${first.uniqueId})"
                )
              }
              Some(first)
            } else {
              None
            }
          } finally if (rs != null) rs.close()
        } finally cs.close()
      }
    }
  }
}
