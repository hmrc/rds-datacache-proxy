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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{GroupMember, MgdCertificate, PartnerMember, ReturnPeriodEndDate, ReturnSummary}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait GamblingDataSource {
  def getReturnSummary(mgdRegNumber: String): Future[ReturnSummary]
  def getMgdCertificate(mgdRegNumber: String): Future[MgdCertificate]
}

@Singleton
class GamblingDataCacheRepository @Inject() (
  @NamedDatabase("gambling") db: Database
)(implicit ec: ExecutionContext)
    extends GamblingDataSource
    with Logging {

  override def getMgdCertificate(mgdRegNumber: String): Future[MgdCertificate] = {
    logger.info(s"getMgdCertificate - MGD Reg Number: $mgdRegNumber")

    Future {
      db.withConnection { connection =>

        val storedProcedure =
          connection.prepareCall(
            "{call MGD_DC_RTN_PCK.GET_MGD_CERTIFICATE(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}"
          )

        // Input
        storedProcedure.setString("P_MGD_REG_NUMBER", mgdRegNumber)

        // Scalars
        storedProcedure.registerOutParameter("MGD_REG_NUMBER", java.sql.Types.VARCHAR)
        storedProcedure.registerOutParameter("REGISTRATION_DATE", java.sql.Types.DATE)
        storedProcedure.registerOutParameter("INDIVIDUAL_NAME", java.sql.Types.VARCHAR)
        storedProcedure.registerOutParameter("BUSINESS_NAME", java.sql.Types.VARCHAR)
        storedProcedure.registerOutParameter("TRADING_NAME", java.sql.Types.VARCHAR)
        storedProcedure.registerOutParameter("REP_MEM_NAME", java.sql.Types.VARCHAR)

        storedProcedure.registerOutParameter("BUS_ADDR_LINE1", java.sql.Types.VARCHAR)
        storedProcedure.registerOutParameter("BUS_ADDR_LINE2", java.sql.Types.VARCHAR)
        storedProcedure.registerOutParameter("BUS_ADDR_LINE3", java.sql.Types.VARCHAR)
        storedProcedure.registerOutParameter("BUS_ADDR_LINE4", java.sql.Types.VARCHAR)
        storedProcedure.registerOutParameter("BUS_POSTCODE", java.sql.Types.VARCHAR)
        storedProcedure.registerOutParameter("BUS_COUNTRY", java.sql.Types.VARCHAR)
        storedProcedure.registerOutParameter("BUS_ADI", java.sql.Types.VARCHAR)

        storedProcedure.registerOutParameter("REP_MEM_LINE1", java.sql.Types.VARCHAR)
        storedProcedure.registerOutParameter("REP_MEM_LINE2", java.sql.Types.VARCHAR)
        storedProcedure.registerOutParameter("REP_MEM_LINE3", java.sql.Types.VARCHAR)
        storedProcedure.registerOutParameter("REP_MEM_LINE4", java.sql.Types.VARCHAR)
        storedProcedure.registerOutParameter("REP_MEM_POSTCODE", java.sql.Types.VARCHAR)
        storedProcedure.registerOutParameter("REP_MEM_ADI", java.sql.Types.VARCHAR)

        storedProcedure.registerOutParameter("TYPE_OF_BUSINESS", java.sql.Types.VARCHAR)
        storedProcedure.registerOutParameter("BUSINESS_TRADE_CLASS", java.sql.Types.NUMERIC)
        storedProcedure.registerOutParameter("NO_OF_PARTNERS", java.sql.Types.NUMERIC)

        storedProcedure.registerOutParameter("GROUP_REG", java.sql.Types.VARCHAR)
        storedProcedure.registerOutParameter("NO_OF_GROUP_MEMS", java.sql.Types.NUMERIC)

        storedProcedure.registerOutParameter("DATE_CERT_ISSUED", java.sql.Types.DATE)

        // CURSORS

        storedProcedure.registerOutParameter("P_PART_MEMBERS", oracle.jdbc.OracleTypes.CURSOR)
        storedProcedure.registerOutParameter("P_GROUP_MEMBERS", oracle.jdbc.OracleTypes.CURSOR)
        storedProcedure.registerOutParameter("RETURN_PERIOD_END_DATES", oracle.jdbc.OracleTypes.CURSOR)

        storedProcedure.execute()

        // Helper functions

        def optString(name: String) = Option(storedProcedure.getString(name))

        def optDate(name: String) = Option(storedProcedure.getDate(name)).map(_.toLocalDate)

        def optInt(name: String) = Option(storedProcedure.getObject(name)).map(_.toString.toInt)

        // PART MEMBERS CURSOR
        val partRs =
          storedProcedure.getObject("P_PART_MEMBERS", classOf[java.sql.ResultSet])

        val partMembers =
          Iterator
            .continually(partRs.next())
            .takeWhile(identity)
            .map { _ =>
              PartnerMember(
                namesOfPartMems = partRs.getString("names_of_part_mems"),
                solePropTitle = Option(partRs.getString("sole_prop_title")),
                solePropFirstName = Option(partRs.getString("sole_prop_first_name")),
                solePropMiddleName = Option(partRs.getString("sole_prop_middle_name")),
                solePropLastName = Option(partRs.getString("sole_prop_last_name")),
                typeOfBusiness = partRs.getInt("type_of_business")
              )
            }
            .toList

        partRs.close()

        // GROUP MEMBERS CURSOR
        val groupRs =
          storedProcedure.getObject("P_GROUP_MEMBERS", classOf[java.sql.ResultSet])

        val groupMembers =
          Iterator
            .continually(groupRs.next())
            .takeWhile(identity)
            .map(_ => GroupMember(groupRs.getString("names_of_group_mems")))
            .toList

        groupRs.close()


        // RETURN PERIOD CURSOR

        val returnRs =
          storedProcedure.getObject("RETURN_PERIOD_END_DATES", classOf[java.sql.ResultSet])

        val returnPeriods =
          Iterator
            .continually(returnRs.next())
            .takeWhile(identity)
            .map { _ =>
              ReturnPeriodEndDate(
                returnPeriodEndDate = returnRs.getDate("return_period_end_date").toLocalDate
              )
            }
            .toList

        returnRs.close()
        
        // Build final response
        val result = MgdCertificate(
          mgdRegNumber = storedProcedure.getString("MGD_REG_NUMBER"),
          registrationDate = optDate("REGISTRATION_DATE"),

          individualName = optString("INDIVIDUAL_NAME"),
          businessName = optString("BUSINESS_NAME"),
          tradingName = optString("TRADING_NAME"),
          repMemName = optString("REP_MEM_NAME"),

          busAddrLine1 = optString("BUS_ADDR_LINE1"),
          busAddrLine2 = optString("BUS_ADDR_LINE2"),
          busAddrLine3 = optString("BUS_ADDR_LINE3"),
          busAddrLine4 = optString("BUS_ADDR_LINE4"),
          busPostcode = optString("BUS_POSTCODE"),
          busCountry = optString("BUS_COUNTRY"),
          busAdi = optString("BUS_ADI"),

          repMemLine1 = optString("REP_MEM_LINE1"),
          repMemLine2 = optString("REP_MEM_LINE2"),
          repMemLine3 = optString("REP_MEM_LINE3"),
          repMemLine4 = optString("REP_MEM_LINE4"),
          repMemPostcode = optString("REP_MEM_POSTCODE"),
          repMemAdi = optString("REP_MEM_ADI"),

          typeOfBusiness = optString("TYPE_OF_BUSINESS"),
          businessTradeClass = optInt("BUSINESS_TRADE_CLASS"),

          noOfPartners = optInt("NO_OF_PARTNERS"),
          groupReg = storedProcedure.getString("GROUP_REG"),
          noOfGroupMems = optInt("NO_OF_GROUP_MEMS"),

          dateCertIssued = optDate("DATE_CERT_ISSUED"),

          partMembers = partMembers,
          groupMembers = groupMembers,
          returnPeriodEndDates = returnPeriods
        )

        storedProcedure.close()
        connection.close()

        result
      }
    }
  }

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
}
