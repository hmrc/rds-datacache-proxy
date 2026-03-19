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

package uk.gov.hmrc.rdsdatacacheproxy.nova.repositories

import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import oracle.jdbc.OracleTypes
import uk.gov.hmrc.rdsdatacacheproxy.nova.models.{EuMemberState, EuMemberStatesResponse, NovaClient, NovaClientListResponse, NvraKnownFacts, TraderDetailsResponse, TraderInformation, TraderResponse, VehicleCalculationData, VehicleStatusDetails}

import java.sql.{CallableStatement, Date, ResultSet, Types}
import java.time.LocalDate
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

trait NovaDataSource {
  def getTraderDetails(userVrn: String, clientVrn: Option[String]): Future[Option[TraderDetailsResponse]]
  def getClientListDownloadStatus(credentialId: String, serviceName: String, gracePeriod: Int = 14400): Future[Int]
  def getAllClients(credentialId: String, start: Int = 0, count: Int = -1, sort: Int = 0, order: String = "ASC"): Future[NovaClientListResponse]
  def getClientsByVrn(credentialId: String, vrn: String): Future[NovaClientListResponse]
  def getClientsByName(credentialId: String,
                       name: String,
                       start: Int = 0,
                       count: Int = -1,
                       sort: Int = 0,
                       order: String = "ASC"
                      ): Future[NovaClientListResponse]
  def getClientsByNameStart(credentialId: String,
                            nameStart: String,
                            start: Int = 0,
                            count: Int = -1,
                            sort: Int = 0,
                            order: String = "ASC"
                           ): Future[NovaClientListResponse]
  def hasClient(credentialId: String, vrn: String): Future[Boolean]
  def getTraderInformation(vrn: String, gracePeriod: Option[Int]): Future[Option[TraderInformation]]
  def getVehicleStatusDetails(vin: String): Future[Option[VehicleStatusDetails]]
  def getVehicleCalculationData(fromCurrency: String, invoiceDate: LocalDate, arrivalDate: LocalDate): Future[VehicleCalculationData]
  def getEuMemberStates(): Future[EuMemberStatesResponse]
  def getNvraKnownFacts(nvraRefNumber: String): Future[NvraKnownFacts]
}

@Singleton
class NovaDatacacheRepository @Inject() (
  @NamedDatabase("nova") db: Database
)(implicit ec: ExecutionContext)
    extends NovaDataSource
    with Logging {

  private def str(rs: ResultSet, col: String): Option[String] =
    Option(rs.getString(col)).map(_.trim).filter(_.nonEmpty)

  private def dateStr(rs: ResultSet, col: String): Option[String] =
    Option(rs.getDate(col)).map(_.toLocalDate.toString)

  private def isYes(rs: ResultSet, col: String): Boolean =
    str(rs, col).exists(v => v.equalsIgnoreCase("Y") || v == "1")

  private def toTraderResponse(
    vrn: String,
    infoRs: ResultSet,
    addrRs: ResultSet,
    detailsRs: ResultSet
  ): TraderResponse = {
    val status = str(infoRs, "STATUS")
    val traderName = str(infoRs, "TRADER_NAME")
    val tradingName = str(infoRs, "TRADING_NAME")
    val addressLine1 = str(infoRs, "BUS_ADDRESS_1")
    val addressLine2 = str(infoRs, "BUS_ADDRESS_2")
    val addressLine3 = str(infoRs, "BUS_ADDRESS_3")
    val addressLine4 = str(infoRs, "BUS_ADDRESS_4")
    val postcode = str(infoRs, "BUS_POSTCODE")
    val email = str(infoRs, "EMAIL")
    val tradeClass = str(infoRs, "TRADE_CLASS")
    val tradeClassDesc = str(infoRs, "TRADE_CLASS_DESC")
    val organisationType = str(infoRs, "ORGANISATION_TYPE")
    val effectiveRegDate = dateStr(infoRs, "EFFECTIVE_REG_DATE")
    val ceasedDate = dateStr(infoRs, "CEASED_DATE")
    val certIssuedDate = dateStr(infoRs, "CERT_ISSUED_DATE")
    val nextReturnPeDate = dateStr(infoRs, "NEXT_RETURN_PE_DATE")
    val returnStagger = str(infoRs, "RETURN_STAGGER")

    val redundant = isYes(addrRs, "REDUNDANT_TRADER")
    val insolvent = Option(addrRs.getInt("INSOLVENCY_STATUS")).exists(_ != 0)
    val phoneNumber = str(addrRs, "DAYTIME_PHONE")
    val mobileNumber = str(addrRs, "MOBILE_PHONE")

    val missingTrader = isYes(detailsRs, "MISSING_TRADER_IND")

    TraderResponse(
      vrn                   = vrn,
      status                = status,
      traderName            = traderName,
      tradingName           = tradingName,
      addressLine1          = addressLine1,
      addressLine2          = addressLine2,
      addressLine3          = addressLine3,
      addressLine4          = addressLine4,
      postcode              = postcode,
      email                 = email,
      phoneNumber           = phoneNumber,
      mobileNumber          = mobileNumber,
      tradeClass            = tradeClass,
      tradeClassDescription = tradeClassDesc,
      organisationType      = organisationType,
      effectiveRegDate      = effectiveRegDate,
      ceasedDate            = ceasedDate,
      certIssuedDate        = certIssuedDate,
      nextReturnPeDate      = nextReturnPeDate,
      returnStagger         = returnStagger,
      redundant             = redundant,
      insolvent             = insolvent,
      missingTrader         = missingTrader
    )
  }

  override def getTraderDetails(userVrn: String, clientVrn: Option[String]): Future[Option[TraderDetailsResponse]] = {
    logger.info(s"[NOVA] getTraderDetails(userVrn=$userVrn, clientVrn=$clientVrn)")

    Future {
      db.withConnection { conn =>
        val cs: CallableStatement =
          conn.prepareCall("{ call VAT_DC_PK.getAllTraderClientDetails(?, ?, ?, ?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, userVrn)
          clientVrn match {
            case Some(vrn) => cs.setString(2, vrn)
            case None      => cs.setNull(2, Types.VARCHAR)
          }
          cs.registerOutParameter(3, OracleTypes.CURSOR) // USER_TRADER_INFO
          cs.registerOutParameter(4, OracleTypes.CURSOR) // USER_ADDR_CONTACT
          cs.registerOutParameter(5, OracleTypes.CURSOR) // CLIENT_TRADER_INFO
          cs.registerOutParameter(6, OracleTypes.CURSOR) // CLIENT_ADDR_CONTACT
          cs.registerOutParameter(7, OracleTypes.CURSOR) // USER_TRADER_DETAILS
          cs.registerOutParameter(8, OracleTypes.CURSOR) // CLIENT_TRADER_DETAILS
          cs.execute()

          val userInfoRs = cs.getObject(3, classOf[ResultSet])
          val userAddrRs = cs.getObject(4, classOf[ResultSet])
          val clientInfoRs = cs.getObject(5, classOf[ResultSet])
          val clientAddrRs = cs.getObject(6, classOf[ResultSet])
          val userDetailRs = cs.getObject(7, classOf[ResultSet])
          val clientDetailRs = cs.getObject(8, classOf[ResultSet])

          try {
            if (userInfoRs == null || !userInfoRs.next()) {
              None
            } else {
              userAddrRs.next()
              userDetailRs.next()

              val userTrader = toTraderResponse(userVrn, userInfoRs, userAddrRs, userDetailRs)

              val clientTrader = clientVrn.flatMap { vrn =>
                if (clientInfoRs != null && clientInfoRs.next()) {
                  clientAddrRs.next()
                  clientDetailRs.next()
                  Some(toTraderResponse(vrn, clientInfoRs, clientAddrRs, clientDetailRs))
                } else {
                  None
                }
              }

              Some(TraderDetailsResponse(userTrader, clientTrader))
            }
          } finally {
            if (userInfoRs != null) userInfoRs.close()
            if (userAddrRs != null) userAddrRs.close()
            if (clientInfoRs != null) clientInfoRs.close()
            if (clientAddrRs != null) clientAddrRs.close()
            if (userDetailRs != null) userDetailRs.close()
            if (clientDetailRs != null) clientDetailRs.close()
          }
        } finally cs.close()
      }
    }
  }

  override def getClientListDownloadStatus(credentialId: String, serviceName: String, gracePeriod: Int): Future[Int] = {
    logger.info(s"[NOVA] getClientListDownloadStatus(credentialId=$credentialId, serviceName=$serviceName, gracePeriod=$gracePeriod)")

    Future {
      db.withConnection { conn =>
        val cs: CallableStatement =
          conn.prepareCall("{ call CLIENT_LIST_STATUS.GETCLIENTLISTDOWNLOADSTATUS(?, ?, ?, ?) }")

        try {
          cs.setString(1, credentialId)
          cs.setString(2, serviceName)
          cs.setInt(3, gracePeriod)
          cs.registerOutParameter(4, OracleTypes.INTEGER)
          cs.execute()

          cs.getInt(4)
        } finally cs.close()
      }
    }
  }

  private def readClientList(rs: ResultSet): List[NovaClient] = {
    val buffer = ListBuffer[NovaClient]()
    while (rs.next()) {
      buffer += NovaClient(
        name                  = Option(rs.getString("CLIENT_NAME")).map(_.trim).getOrElse(""),
        vatRegistrationNumber = Option(rs.getString("VAT_REG_NUMBER")).map(_.trim).getOrElse("")
      )
    }
    buffer.toList
  }

  private def readClientNameChars(rs: ResultSet): List[String] = {
    val buffer = ListBuffer[String]()
    while (rs.next()) {
      Option(rs.getString("CLIENTNAMESTARTINGCHARACTER"))
        .map(_.trim)
        .filter(_.nonEmpty)
        .foreach(buffer += _)
    }
    buffer.toList
  }

  override def getAllClients(credentialId: String, start: Int, count: Int, sort: Int, order: String): Future[NovaClientListResponse] = {
    logger.info(s"[NOVA] getAllClients(credentialId=$credentialId, start=$start, count=$count, sort=$sort, order=$order)")

    Future {
      db.withConnection { conn =>
        val cs: CallableStatement =
          conn.prepareCall("{ call NOVA_CLIENT_SEARCH.getAllClients(?, ?, ?, ?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, credentialId)
          cs.setInt(2, start)
          cs.setInt(3, count)
          cs.setInt(4, sort)
          cs.setString(5, order)
          cs.registerOutParameter(6, OracleTypes.INTEGER) // P_CLIENT_COUNT
          cs.registerOutParameter(7, OracleTypes.CURSOR) // CP_CLIENT_LIST
          cs.registerOutParameter(8, OracleTypes.CURSOR) // CP_CLIENT_NAME_CHARS
          cs.execute()

          val clientCount = cs.getInt(6)
          val clientListRs = cs.getObject(7, classOf[ResultSet])
          val clientNameCharsRs = cs.getObject(8, classOf[ResultSet])

          try {
            val clients = Option(clientListRs).map(readClientList).getOrElse(List.empty)
            val nameChars = Option(clientNameCharsRs).map(readClientNameChars).getOrElse(List.empty)
            NovaClientListResponse(clients, clientCount, nameChars)
          } finally {
            if (clientListRs != null) clientListRs.close()
            if (clientNameCharsRs != null) clientNameCharsRs.close()
          }
        } finally cs.close()
      }
    }
  }

  override def getClientsByVrn(credentialId: String, vrn: String): Future[NovaClientListResponse] = {
    logger.info(s"[NOVA] getClientsByVrn(credentialId=$credentialId, vrn=$vrn)")

    Future {
      db.withConnection { conn =>
        val cs: CallableStatement =
          conn.prepareCall("{ call NOVA_CLIENT_SEARCH.getClientByVrn(?, ?, ?, ?) }")

        try {
          cs.setString(1, credentialId)
          cs.setString(2, vrn)
          cs.registerOutParameter(3, OracleTypes.CURSOR) // CP_CLIENT_LIST
          cs.registerOutParameter(4, OracleTypes.CURSOR) // CP_CLIENT_NAME_CHARS
          cs.execute()

          val clientListRs = cs.getObject(3, classOf[ResultSet])
          val clientNameCharsRs = cs.getObject(4, classOf[ResultSet])

          try {
            val clients = Option(clientListRs).map(readClientList).getOrElse(List.empty)
            val nameChars = Option(clientNameCharsRs).map(readClientNameChars).getOrElse(List.empty)
            NovaClientListResponse(clients, clients.size, nameChars)
          } finally {
            if (clientListRs != null) clientListRs.close()
            if (clientNameCharsRs != null) clientNameCharsRs.close()
          }
        } finally cs.close()
      }
    }
  }

  override def getClientsByName(credentialId: String,
                                name: String,
                                start: Int,
                                count: Int,
                                sort: Int,
                                order: String
                               ): Future[NovaClientListResponse] = {
    logger.info(s"[NOVA] getClientsByName(credentialId=$credentialId, name=$name, start=$start, count=$count, sort=$sort, order=$order)")

    Future {
      db.withConnection { conn =>
        val cs: CallableStatement =
          conn.prepareCall("{ call NOVA_CLIENT_SEARCH.getClientsByName(?, ?, ?, ?, ?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, credentialId)
          cs.setString(2, name)
          cs.setInt(3, start)
          cs.setInt(4, count)
          cs.setInt(5, sort)
          cs.setString(6, order)
          cs.registerOutParameter(7, OracleTypes.INTEGER) // P_CLIENT_COUNT
          cs.registerOutParameter(8, OracleTypes.CURSOR) // CP_CLIENT_LIST
          cs.registerOutParameter(9, OracleTypes.CURSOR) // CP_CLIENT_NAME_CHARS
          cs.execute()

          val clientCount = cs.getInt(7)
          val clientListRs = cs.getObject(8, classOf[ResultSet])
          val clientNameCharsRs = cs.getObject(9, classOf[ResultSet])

          try {
            val clients = Option(clientListRs).map(readClientList).getOrElse(List.empty)
            val nameChars = Option(clientNameCharsRs).map(readClientNameChars).getOrElse(List.empty)
            NovaClientListResponse(clients, clientCount, nameChars)
          } finally {
            if (clientListRs != null) clientListRs.close()
            if (clientNameCharsRs != null) clientNameCharsRs.close()
          }
        } finally cs.close()
      }
    }
  }

  override def getClientsByNameStart(credentialId: String,
                                     nameStart: String,
                                     start: Int,
                                     count: Int,
                                     sort: Int,
                                     order: String
                                    ): Future[NovaClientListResponse] = {
    logger.info(
      s"[NOVA] getClientsByNameStart(credentialId=$credentialId, nameStart=$nameStart, start=$start, count=$count, sort=$sort, order=$order)"
    )

    Future {
      db.withConnection { conn =>
        val cs: CallableStatement =
          conn.prepareCall("{ call NOVA_CLIENT_SEARCH.getClientsByNameStart(?, ?, ?, ?, ?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, credentialId)
          cs.setString(2, nameStart)
          cs.setInt(3, start)
          cs.setInt(4, count)
          cs.setInt(5, sort)
          cs.setString(6, order)
          cs.registerOutParameter(7, OracleTypes.INTEGER) // P_CLIENT_COUNT
          cs.registerOutParameter(8, OracleTypes.CURSOR) // CP_CLIENT_LIST
          cs.registerOutParameter(9, OracleTypes.CURSOR) // CP_CLIENT_NAME_CHARS
          cs.execute()

          val clientCount = cs.getInt(7)
          val clientListRs = cs.getObject(8, classOf[ResultSet])
          val clientNameCharsRs = cs.getObject(9, classOf[ResultSet])

          try {
            val clients = Option(clientListRs).map(readClientList).getOrElse(List.empty)
            val nameChars = Option(clientNameCharsRs).map(readClientNameChars).getOrElse(List.empty)
            NovaClientListResponse(clients, clientCount, nameChars)
          } finally {
            if (clientListRs != null) clientListRs.close()
            if (clientNameCharsRs != null) clientNameCharsRs.close()
          }
        } finally cs.close()
      }
    }
  }

  override def hasClient(credentialId: String, vrn: String): Future[Boolean] = {
    logger.info(s"[NOVA] hasClient(credentialId=$credentialId, vrn=$vrn)")

    Future {
      db.withConnection { conn =>
        val cs: CallableStatement =
          conn.prepareCall("{ call NOVA_CLIENT_SEARCH.hasClient(?, ?, ?) }")

        try {
          cs.setString(1, credentialId)
          cs.setString(2, vrn)
          cs.registerOutParameter(3, OracleTypes.INTEGER) // P_EXISTS_O: 1=exists, 0=not
          cs.execute()

          cs.getInt(3) == 1
        } finally cs.close()
      }
    }
  }

  override def getTraderInformation(vrn: String, gracePeriod: Option[Int]): Future[Option[TraderInformation]] = {
    logger.info(s"[NOVA] getTraderInformation(vrn=$vrn, gracePeriod=$gracePeriod)")

    Future {
      db.withConnection { conn =>
        val cs: CallableStatement =
          conn.prepareCall("{ call VAT_DC_PK.getTraderInformation(?, ?, ?) }")

        try {
          cs.setString(1, vrn)
          gracePeriod match {
            case Some(gp) => cs.setInt(2, gp)
            case None     => cs.setNull(2, Types.NUMERIC)
          }
          cs.registerOutParameter(3, OracleTypes.CURSOR) // p_trader
          cs.execute()

          val traderRs = cs.getObject(3, classOf[ResultSet])

          try {
            if (traderRs == null || !traderRs.next()) {
              None
            } else {
              Some(
                TraderInformation(
                  vrn                   = vrn,
                  status                = str(traderRs, "status"),
                  traderName            = str(traderRs, "trader_name"),
                  tradingName           = str(traderRs, "trading_name"),
                  addressLine1          = str(traderRs, "bus_address_1"),
                  addressLine2          = str(traderRs, "bus_address_2"),
                  addressLine3          = str(traderRs, "bus_address_3"),
                  addressLine4          = str(traderRs, "bus_address_4"),
                  postcode              = str(traderRs, "bus_postcode"),
                  email                 = str(traderRs, "email"),
                  organisationType      = str(traderRs, "organisation_type"),
                  tradeClass            = str(traderRs, "trade_class"),
                  tradeClassDescription = str(traderRs, "trade_class_desc"),
                  effectiveRegDate      = dateStr(traderRs, "effective_reg_date"),
                  ceasedDate            = dateStr(traderRs, "ceased_date"),
                  certIssuedDate        = dateStr(traderRs, "cert_issued_date"),
                  nextReturnPeDate      = dateStr(traderRs, "next_return_pe_date"),
                  returnStagger         = str(traderRs, "return_stagger")
                )
              )
            }
          } finally {
            if (traderRs != null) traderRs.close()
          }
        } finally cs.close()
      }
    }
  }

  override def getVehicleStatusDetails(vin: String): Future[Option[VehicleStatusDetails]] = {
    logger.info(s"[NOVA] getVehicleStatusDetails(vin=$vin)")

    Future {
      db.withConnection { conn =>
        val cs: CallableStatement =
          conn.prepareCall("{ call NOVA_FILING_APP.getVehicleStatusDetails(?, ?) }")

        try {
          cs.setString(1, vin)
          cs.registerOutParameter(2, OracleTypes.CURSOR) // c_VehicleDetails
          cs.execute()

          val vehicleRs = cs.getObject(2, classOf[ResultSet])

          try {
            if (vehicleRs == null || !vehicleRs.next()) {
              None
            } else {
              Some(
                VehicleStatusDetails(
                  vin             = Option(vehicleRs.getString("p_vin")).map(_.trim).getOrElse(vin),
                  novaRef         = str(vehicleRs, "p_nova_ref"),
                  make            = str(vehicleRs, "p_make"),
                  model           = str(vehicleRs, "p_model"),
                  mileage         = Option(vehicleRs.getObject("p_mileage")).map(_ => vehicleRs.getInt("p_mileage")),
                  firstRegDate    = dateStr(vehicleRs, "p_first_reg_date"),
                  secured         = str(vehicleRs, "p_status").exists(_.equalsIgnoreCase("secured")),
                  restrictionDate = dateStr(vehicleRs, "p_restriction_date"),
                  imported        = str(vehicleRs, "p_origin").exists(_.equalsIgnoreCase("IMPORT"))
                )
              )
            }
          } finally {
            if (vehicleRs != null) vehicleRs.close()
          }
        } finally cs.close()
      }
    }
  }

  override def getVehicleCalculationData(
    fromCurrency: String,
    invoiceDate: LocalDate,
    arrivalDate: LocalDate
  ): Future[VehicleCalculationData] = {
    logger.info(
      s"[NOVA] getVehicleCalculationData(fromCurrency=$fromCurrency, invoiceDate=$invoiceDate, arrivalDate=$arrivalDate)"
    )

    Future {
      db.withConnection { conn =>
        val cs: CallableStatement =
          conn.prepareCall("{ call NOVA_FILING_APP.getVehicleCalculationData2(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, fromCurrency)
          cs.setString(2, "GBP")
          cs.setDate(3, Date.valueOf(invoiceDate))
          cs.setDate(4, Date.valueOf(arrivalDate))
          cs.registerOutParameter(5, Types.NUMERIC) // p_exchange_rate
          cs.registerOutParameter(6, Types.DATE) // p_effective_date
          cs.registerOutParameter(7, Types.NUMERIC) // p_vat_rate
          cs.registerOutParameter(8, Types.DATE) // p_min_limit_eff_date
          cs.registerOutParameter(9, Types.NUMERIC) // p_min_limit_amount
          cs.registerOutParameter(10, Types.DATE) // p_threshold_days_eff_date
          cs.registerOutParameter(11, Types.INTEGER) // p_threshold_days
          cs.registerOutParameter(12, Types.DATE) // p_rate_eff_date
          cs.registerOutParameter(13, Types.NUMERIC) // p_rate_amount
          cs.registerOutParameter(14, Types.DATE) // p_max_no_of_days_eff_date
          cs.registerOutParameter(15, Types.INTEGER) // p_max_no_of_days
          cs.registerOutParameter(16, Types.DATE) // p_alt_amt_eff_date
          cs.registerOutParameter(17, Types.NUMERIC) // p_alt_amt
          cs.execute()

          def optDate(pos: Int): Option[String] =
            Option(cs.getDate(pos)).map(_.toLocalDate.toString)

          def optDecimal(pos: Int): Option[BigDecimal] =
            Option(cs.getBigDecimal(pos))

          def optInt(pos: Int): Option[Int] = {
            val v = cs.getInt(pos)
            if (cs.wasNull()) None else Some(v)
          }

          VehicleCalculationData(
            exchangeRate         = optDecimal(5),
            vatRateEffectiveDate = optDate(6),
            vatRate              = optDecimal(7),
            minLimitEffDate      = optDate(8),
            minLimitAmount       = optDecimal(9),
            thresholdDaysEffDate = optDate(10),
            thresholdDays        = optInt(11),
            rateEffDate          = optDate(12),
            rateAmount           = optDecimal(13),
            maxNoOfDaysEffDate   = optDate(14),
            maxNoOfDays          = optInt(15),
            altAmtEffDate        = optDate(16),
            altAmt               = optDecimal(17)
          )
        } finally cs.close()
      }
    }
  }

  override def getEuMemberStates(): Future[EuMemberStatesResponse] = {
    logger.info("[NOVA] getEuMemberStates()")

    Future {
      db.withConnection { conn =>
        val cs: CallableStatement =
          conn.prepareCall("{ call NOVA_FILING_APP.getEuMemberStates(?) }")

        try {
          cs.registerOutParameter(1, OracleTypes.CURSOR) // c_EuMemberStates
          cs.execute()

          val statesRs = cs.getObject(1, classOf[ResultSet])

          try {
            val buffer = scala.collection.mutable.ListBuffer[EuMemberState]()
            if (statesRs != null) {
              while (statesRs.next()) {
                buffer += EuMemberState(
                  countryCode        = Option(statesRs.getString("p_country_code")).map(_.trim).getOrElse(""),
                  countryDescription = Option(statesRs.getString("p_coutry_desc")).map(_.trim).getOrElse(""),
                  euJoiningDate      = Option(statesRs.getDate("p_eu_joining_date")).map(_.toLocalDate.toString),
                  euLeavingDate      = Option(statesRs.getDate("p_eu_leaving_date")).map(_.toLocalDate.toString),
                  euAccessionaryDate = Option(statesRs.getDate("p_eu_accessionary_date")).map(_.toLocalDate.toString)
                )
              }
            }
            val filtered = buffer.toList
              .filter(s => (s.euJoiningDate.isDefined && s.euLeavingDate.isEmpty) || s.countryCode == "HR")
              .sortBy(_.countryCode)
            EuMemberStatesResponse(filtered)
          } finally {
            if (statesRs != null) statesRs.close()
          }
        } finally cs.close()
      }
    }
  }

  override def getNvraKnownFacts(nvraRefNumber: String): Future[NvraKnownFacts] = {
    logger.info(s"[NOVA] getNvraKnownFacts(nvraRefNumber=$nvraRefNumber)")

    Future {
      db.withConnection { conn =>
        val cs: CallableStatement =
          conn.prepareCall("{ call NVRA_CHRIS_PK.retrieveNVRA_KnownFacts(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, nvraRefNumber)
          cs.registerOutParameter(2, Types.VARCHAR) // out_nvra_ref_number
          cs.registerOutParameter(3, Types.VARCHAR) // out_agent_name
          cs.registerOutParameter(4, Types.VARCHAR) // out_address_line_1
          cs.registerOutParameter(5, Types.VARCHAR) // out_address_line_2
          cs.registerOutParameter(6, Types.VARCHAR) // out_address_line_3
          cs.registerOutParameter(7, Types.VARCHAR) // out_address_line_4
          cs.registerOutParameter(8, Types.VARCHAR) // out_address_line_5
          cs.registerOutParameter(9, Types.VARCHAR) // out_postcode
          cs.registerOutParameter(10, Types.VARCHAR) // out_abroad_flag
          cs.registerOutParameter(11, Types.VARCHAR) // out_result_code
          cs.execute()

          def optStr(pos: Int): Option[String] =
            Option(cs.getString(pos)).map(_.trim).filter(_.nonEmpty)

          NvraKnownFacts(
            nvraRefNumber = Option(cs.getString(2)).map(_.trim).getOrElse(nvraRefNumber),
            agentName     = optStr(3),
            addressLine1  = optStr(4),
            addressLine2  = optStr(5),
            addressLine3  = optStr(6),
            addressLine4  = optStr(7),
            addressLine5  = optStr(8),
            postcode      = optStr(9),
            abroadFlag    = optStr(10),
            resultCode    = Option(cs.getString(11)).map(_.trim).getOrElse("001")
          )
        } finally cs.close()
      }
    }
  }
}
