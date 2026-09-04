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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{LicenceDetails, Regime}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait LicenceDataSource {
  def getLicenceDetails(regime: Regime, regNumber: String): Future[LicenceDetails]
}

@Singleton
class LicenceCacheRepository @Inject() (@NamedDatabase("gambling") mgdDb: MGDDatabase, @NamedDatabase("gambling.gtr") gtrDb: GTRDatabase)(implicit
  ec: ExecutionContext
) extends LicenceDataSource
    with RepositorySupport
    with Logging {

  override def getLicenceDetails(regime: Regime, regNumber: String): Future[LicenceDetails] =
    Future {
      getDb(regime, mgdDb, gtrDb).underlying.withConnection { connection =>
        val cs = {
          regime match
            case Regime.MGD => connection.prepareCall("{ call MGD_DC_VARIATION_PK.GET_LICENCE_DETAILS(?, ?) }")
            case _          => throw new RuntimeException(s"Regime $regime is not supported for getLicenceDetails")
        }
        try {
          cs.setString(1, regNumber) // IN P_MGD_REG_NUMBER
          cs.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR) // OUT P_LICENCES
          cs.execute()

          val rs = cs.getObject(2).asInstanceOf[java.sql.ResultSet]
          try {
            if (rs == null || !rs.next())
              throw new RuntimeException(s"No licence details found for regNumber=$regNumber")

            LicenceDetails(
              mgdRegNumber          = rs.getString("mgd_reg_number"),
              haveGamblingLicenceNo = Option(rs.getString("HAVE_GAMBLING_LICENCE_NO")),
              gamblingLicenceNo     = Option(rs.getString("GAMBLING_LICENCE_NO")).getOrElse(""),
              heldByLandlord        = Option(rs.getString("HELD_BY_LANDLORD")),
              localAuthority        = Option(rs.getString("LOCAL_AUTHORITY")),
              familyEntertainment   = Option(rs.getString("FAMILY_ENTERTAINMENT")),
              clubGaming            = Option(rs.getString("CLUB_GAMING")),
              clubLicence           = Option(rs.getString("CLUB_LICENCE")),
              prizeGaming           = Option(rs.getString("PRIZE_GAMING")),
              onPremises            = Option(rs.getString("ON_PREMISES")),
              clubPremises          = Option(rs.getString("CLUB_PREMISES")),
              regCert               = Option(rs.getString("REG_CERT")),
              bookmaking            = Option(rs.getString("BOOKMAKING")),
              bingo                 = Option(rs.getString("BINGO")),
              amusement             = Option(rs.getString("AMUSEMENT")),
              serveAlcohol          = Option(rs.getString("SERVE_ALCOHOL")),
              premisesNotCovered    = Option(rs.getString("PREMISES_NOT_COVERED")),
              systemDate            = optSystemDate("system_date", rs)
            )
          } finally closeQuietly(rs)
        } finally {
          closeQuietly(cs)
        }
      }
    }(ec)
}
