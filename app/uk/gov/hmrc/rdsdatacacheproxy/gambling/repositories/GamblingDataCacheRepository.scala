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
import scala.concurrent.{ExecutionContext, Future, blocking}

trait GamblingDataSource {
  def getReturnSummary(mgdRegNumber: String): Future[ReturnSummary]
  def getBusinessDetails(mgdRegNumber: String): Future[BusinessDetails]
  def getBusinessName(mgdRegNumber: String): Future[BusinessName]
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

    Future(blocking {
      db.withConnection { connection =>

        val cs =
          connection.prepareCall(
            "{call MGD_DC_RTN_PCK.GET_MGD_CERTIFICATE(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}"
          )

        // close helper
        def closeQuietly(c: AutoCloseable): Unit =
          if (c != null)
            try c.close()
            catch {
              case _: Throwable => ()
            }

        try {
          cs.setString(1, mgdRegNumber)

          cs.registerOutParameter(2, java.sql.Types.VARCHAR) // MGD_REG_NUMBER
          cs.registerOutParameter(3, java.sql.Types.DATE) // REGISTRATION_DATE
          cs.registerOutParameter(4, java.sql.Types.VARCHAR) // INDIVIDUAL_NAME
          cs.registerOutParameter(5, java.sql.Types.VARCHAR) // BUSINESS_NAME
          cs.registerOutParameter(6, java.sql.Types.VARCHAR) // TRADING_NAME
          cs.registerOutParameter(7, java.sql.Types.VARCHAR) // REP_MEM_NAME

          cs.registerOutParameter(8, java.sql.Types.VARCHAR) // BUS_ADDR_LINE1
          cs.registerOutParameter(9, java.sql.Types.VARCHAR) // BUS_ADDR_LINE2
          cs.registerOutParameter(10, java.sql.Types.VARCHAR) // BUS_ADDR_LINE3
          cs.registerOutParameter(11, java.sql.Types.VARCHAR) // BUS_ADDR_LINE4
          cs.registerOutParameter(12, java.sql.Types.VARCHAR) // BUS_POSTCODE
          cs.registerOutParameter(13, java.sql.Types.VARCHAR) // BUS_COUNTRY
          cs.registerOutParameter(14, java.sql.Types.VARCHAR) // BUS_ADI (VARCHAR2)

          cs.registerOutParameter(15, java.sql.Types.VARCHAR) // REP_MEM_LINE1
          cs.registerOutParameter(16, java.sql.Types.VARCHAR) // REP_MEM_LINE2
          cs.registerOutParameter(17, java.sql.Types.VARCHAR) // REP_MEM_LINE3
          cs.registerOutParameter(18, java.sql.Types.VARCHAR) // REP_MEM_LINE4
          cs.registerOutParameter(19, java.sql.Types.VARCHAR) // REP_MEM_POSTCODE
          cs.registerOutParameter(20, java.sql.Types.VARCHAR) // REP_MEM_ADI (VARCHAR2)

          cs.registerOutParameter(21, java.sql.Types.VARCHAR) // TYPE_OF_BUSINESS (VARCHAR2)
          cs.registerOutParameter(22, java.sql.Types.NUMERIC) // BUSINESS_TRADE_CLASS (NUMBER)
          cs.registerOutParameter(23, java.sql.Types.NUMERIC) // NO_OF_PARTNERS (NUMBER)

          cs.registerOutParameter(24, oracle.jdbc.OracleTypes.CURSOR) // P_PART_MEMBERS (REF CURSOR)
          cs.registerOutParameter(25, java.sql.Types.VARCHAR) // GROUP_REG (VARCHAR2)
          cs.registerOutParameter(26, java.sql.Types.NUMERIC) // NO_OF_GROUP_MEMS (NUMBER)
          cs.registerOutParameter(27, oracle.jdbc.OracleTypes.CURSOR) // P_GROUP_MEMBERS (REF CURSOR)
          cs.registerOutParameter(28, java.sql.Types.DATE) // DATE_CERT_ISSUED (DATE)
          cs.registerOutParameter(29, oracle.jdbc.OracleTypes.CURSOR) // RETURN_PERIOD_END_DATES (REF CURSOR)

          cs.execute()

          def optString(i: Int): Option[String] =
            Option(cs.getString(i)).map(_.trim).filter(_.nonEmpty)

          def optDate(i: Int): Option[java.time.LocalDate] =
            Option(cs.getDate(i)).map(_.toLocalDate)

          def optInt(i: Int): Option[Int] =
            Option(cs.getObject(i)).map {
              case bd: java.math.BigDecimal => bd.intValue()
              case n: java.lang.Number      => n.intValue()
              case other                    => other.toString.toInt
            }

          val partMembers: List[PartnerMember] = {
            val rs = cs.getObject(24).asInstanceOf[java.sql.ResultSet]
            if (rs == null) Nil
            else {
              try {
                val b = List.newBuilder[PartnerMember]
                while (rs.next()) {
                  b += PartnerMember(
                    namesOfPartMems    = rs.getString("names_of_part_mems"),
                    solePropTitle      = Option(rs.getString("sole_prop_title")),
                    solePropFirstName  = Option(rs.getString("sole_prop_first_name")),
                    solePropMiddleName = Option(rs.getString("sole_prop_middle_name")),
                    solePropLastName   = Option(rs.getString("sole_prop_last_name")),
                    typeOfBusiness     = rs.getInt("type_of_business")
                  )
                }
                b.result()
              } finally closeQuietly(rs)
            }
          }

          val groupMembers: List[GroupMember] = {
            val rs = cs.getObject(27).asInstanceOf[java.sql.ResultSet]
            if (rs == null) Nil
            else {
              try {
                val b = List.newBuilder[GroupMember]
                while (rs.next()) {
                  b += GroupMember(rs.getString("names_of_group_mems"))
                }
                b.result()
              } finally closeQuietly(rs)
            }
          }

          val returnPeriods: List[ReturnPeriodEndDate] = {
            val rs = cs.getObject(29).asInstanceOf[java.sql.ResultSet]
            if (rs == null) Nil
            else {
              try {
                val b = List.newBuilder[ReturnPeriodEndDate]
                while (rs.next()) {
                  val d = rs.getDate("return_period_end_date")
                  if (d != null) b += ReturnPeriodEndDate(d.toLocalDate)
                }
                b.result()
              } finally closeQuietly(rs)
            }
          }

          MgdCertificate(
            mgdRegNumber         = cs.getString(2),
            registrationDate     = optDate(3),
            individualName       = optString(4),
            businessName         = optString(5),
            tradingName          = optString(6),
            repMemName           = optString(7),
            busAddrLine1         = optString(8),
            busAddrLine2         = optString(9),
            busAddrLine3         = optString(10),
            busAddrLine4         = optString(11),
            busPostcode          = optString(12),
            busCountry           = optString(13),
            busAdi               = optString(14),
            repMemLine1          = optString(15),
            repMemLine2          = optString(16),
            repMemLine3          = optString(17),
            repMemLine4          = optString(18),
            repMemPostcode       = optString(19),
            repMemAdi            = optString(20),
            typeOfBusiness       = optString(21),
            businessTradeClass   = optInt(22),
            noOfPartners         = optInt(23),
            groupReg             = cs.getString(25),
            noOfGroupMems        = optInt(26),
            dateCertIssued       = optDate(28),
            partMembers          = partMembers,
            groupMembers         = groupMembers,
            returnPeriodEndDates = returnPeriods
          )

        } finally {
          closeQuietly(cs)
        }
      }
    })(ec)
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
                solePropTitle     = Option(rs.getString("SOLE_PROP_TITLE")),
                solePropFirstName = Option(rs.getString("SOLE_PROP_FIRST_NAME")),
                solePropMidName   = Option(rs.getString("SOLE_PROP_MIDDLE_NAME")),
                solePropLastName  = Option(rs.getString("SOLE_PROP_LAST_NAME")),
                businessName      = Option(rs.getString("BUSINESS_NAME")),
                businessType      = Option(rs.getInt("BUSINESS_TYPE")),
                tradingName       = Option(rs.getString("TRADING_NAME")),
                systemDate        = Option(rs.getDate("SYSTEM_DATE")).map(_.toLocalDate)
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

  override def getBusinessDetails(mgdRegNumber: String): Future[BusinessDetails] = {

    logger.info(s"[GamblingDataCacheRepository][getBusinessDetails] mgdRegNumber=$mgdRegNumber")

    Future {
      db.withConnection { conn =>

        val cs = conn.prepareCall("{ call MGD_DC_VARIATION_PK.GET_BUSINESS_DETAILS(?, ?) }")

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
              BusinessDetails(
                mgdRegNumber          = rs.getString("MGD_REG_NUMBER"),
                businessType          = Option(rs.getInt("BUSINESS_TYPE")),
                currentlyRegistered   = Option(rs.getInt("CURRENTLY_REGISTERED")),
                groupReg              = Option(rs.getString("GROUP_REG")),
                dateOfRegistration    = Option(rs.getDate("DATE_OF_REGISTRATION")).map(_.toLocalDate),
                businessPartnerNumber = Option(rs.getString("BUSINESS_PARTNER_NUMBER")),
                systemDate            = Option(rs.getDate("SYSTEM_DATE")).map(_.toLocalDate)
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
