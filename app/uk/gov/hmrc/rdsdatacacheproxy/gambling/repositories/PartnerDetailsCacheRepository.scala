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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{Partner, PartnerDetails, Regime}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait PartnerDetailsDataSource {
  def getPartnerDetails(regime: Regime, regNumber: String): Future[PartnerDetails]
}

@Singleton
class PartnerDetailsCacheRepository @Inject() (@NamedDatabase("gambling") mgdDb: MGDDatabase, @NamedDatabase("gambling.gtr") gtrDb: GTRDatabase)(
  implicit ec: ExecutionContext
) extends PartnerDetailsDataSource
    with RepositorySupport
    with Logging {

  override def getPartnerDetails(regime: Regime, regNumber: String): Future[PartnerDetails] =
    Future {
      getDb(regime, mgdDb, gtrDb).underlying.withConnection { connection =>
        val cs = {
          regime match
            case Regime.MGD => connection.prepareCall("{ call MGD_DC_VARIATION_PK.GET_PARTNERS(?, ?, ?) }")
            case _          => throw new RuntimeException(s"Regime $regime is not supported for getPartnerDetails")
        }
        try {
          cs.setString(1, regNumber) // IN P_MGD_REG_NUMBER
          cs.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR) // OUT P_PARTNERS
          cs.registerOutParameter(3, oracle.jdbc.OracleTypes.DATE) // OUT P_SYSDATE
          cs.execute()

          val partnerDetails: List[Partner] = {
            val rs = cs.getObject(2).asInstanceOf[java.sql.ResultSet]
            if (rs == null) Nil
            else {
              try {
                val b = List.newBuilder[Partner]

                while (rs.next()) {
                  val maybeItem = Option(rs.getString("mgd_reg_number")).map(mgdRegNumber =>
                    Partner(
                      mgdRegNumber           = mgdRegNumber,
                      dateOfJoining          = optDate("DATE_OF_JOINING", rs),
                      dateOfLeaving          = optDate("DATE_OF_LEAVING", rs),
                      solePropTitle          = Option(rs.getString("SOLE_PROP_TITLE")),
                      solePropFirstName      = Option(rs.getString("SOLE_PROP_FIRST_NAME")),
                      solePropMiddleName     = Option(rs.getString("SOLE_PROP_MIDDLE_NAME")),
                      solePropLastName       = Option(rs.getString("SOLE_PROP_LAST_NAME")),
                      businessName           = Option(rs.getString("BUSINESS_NAME")),
                      tradingName            = Option(rs.getString("TRADING_NAME")),
                      dateOfBirth            = optDate("DATE_OF_BIRTH", rs),
                      nino                   = Option(rs.getString("NINO")),
                      utr                    = Option(rs.getString("UTR")),
                      vrn                    = Option(rs.getString("VRN")),
                      crn                    = Option(rs.getString("CRN")),
                      dateOfIncorporation    = optDate("DATE_OF_INCORPORATION", rs),
                      countryOfIncorporation = Option(rs.getString("COUNTRY_OF_INCORPORATION")),
                      foreignCorporateRef    = Option(rs.getString("FOREIGN_CORPORATE_REF")),
                      address1               = Option(rs.getString("ADDRESS_1")),
                      address2               = Option(rs.getString("ADDRESS_2")),
                      address3               = Option(rs.getString("ADDRESS_3")),
                      address4               = Option(rs.getString("ADDRESS_4")),
                      postcode               = Option(rs.getString("POSTCODE")),
                      country                = Option(rs.getString("COUNTRY")),
                      adi                    = Option(rs.getString("ADI")),
                      iomOrCiFlag            = Option(rs.getString("IOM_OR_CI_FLAG")),
                      phoneNumber            = Option(rs.getString("PHONE_NUMBER")),
                      mobilePhoneNumber      = Option(rs.getString("MOBILE_PHONE_NUMBER")),
                      faxNumber              = Option(rs.getString("FAX_NUMBER")),
                      emailAddress           = Option(rs.getString("EMAIL_ADDR")),
                      isFutureLeaveDate      = Option(rs.getInt("IS_FUTURE_LEAVE_DATE")),
                      isFutureJoinDate       = Option(rs.getInt("IS_FUTURE_JOIN_DATE")),
                      businessType           = Option(rs.getInt("BUSINESS_TYPE"))
                    )
                  )
                  b.addAll(maybeItem.toList)
                }
                b.result()
              } finally closeQuietly(rs)
            }
          }

          PartnerDetails(
            partners   = partnerDetails,
            systemDate = Option(cs.getDate(3)).map(_.toLocalDate)
          )
        } finally {
          closeQuietly(cs)
        }
      }
    }(ec)
}
