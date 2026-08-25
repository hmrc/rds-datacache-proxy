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

package uk.gov.hmrc.rdsdatacacheproxy.cis.repositories

import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.db.{Database, NamedDatabase}

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.mutable.ListBuffer
import scala.util.Using
import java.sql.{CallableStatement, Connection, ResultSet, Types}
import oracle.jdbc.OracleTypes
import uk.gov.hmrc.rdsdatacacheproxy.cis.models.{CisClientSearchResult, CisTaxpayer, CisTaxpayerSearchResult, EnqueueMessageRequest, SchemePrepop, SubcontractorPrepopRecord}
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.ResultSetUtils.*

trait CisMonthlyReturnSource {
  def getCisTaxpayerByTaxRef(taxOfficeNumber: String, taxOfficeReference: String): Future[Option[CisTaxpayer]]
  def getClientListDownloadStatus(credentialId: String, serviceName: String, gracePeriod: Int = 14400): Future[Int]
  def getAllClients(
    irAgentId: String,
    credentialId: String,
    start: Int = 0,
    count: Int = -1,
    sort: Int = 0,
    order: String = "ASC"
  ): Future[CisClientSearchResult]

  def hasClient(irAgentId: String, credentialId: String, taxOfficeNumber: String, taxOfficeReference: String): Future[Boolean]
  def getSchemePrepopByKnownFacts(taxOfficeNumber: String, taxOfficeReference: String, accountOfficeReference: String): Future[Option[SchemePrepop]]
  def getSubcontractorsPrepopByKnownFacts(taxOfficeNumber: String,
                                          taxOfficeReference: String,
                                          accountOfficeReference: String
                                         ): Future[Seq[SubcontractorPrepopRecord]]
  def enqueueMessage(request: EnqueueMessageRequest): Future[Long]
}

@Singleton
class CisDatacacheRepository @Inject() (
  @NamedDatabase("cis") db: Database
)(implicit ec: ExecutionContext)
    extends CisMonthlyReturnSource
    with Logging {

  private def withCall[A](conn: Connection, sql: String)(f: CallableStatement => A): A =
    Using.resource(conn.prepareCall(sql))(f)

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

  private def toCisTaxpayerSearchResult(rs: ResultSet): CisTaxpayerSearchResult =
    CisTaxpayerSearchResult(
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
      schemeName        = str(rs, "SCHEME_NAME")
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

  override def getClientListDownloadStatus(
    credentialId: String,
    serviceName: String,
    gracePeriod: Int
  ): Future[Int] = {
    logger.info(s"[CIS] getCisTaxpayerByTaxRef(CREDENTIAL_ID=$credentialId, SERVICE_NAME=$serviceName, GRACE_PERIOD=$gracePeriod)")

    Future {
      db.withConnection { conn =>
        val getDownloadStatusSql: CallableStatement =
          conn.prepareCall("{ call CLIENT_LIST_STATUS.GETCLIENTLISTDOWNLOADSTATUS(?, ?, ?, ?) }")

        try {
          getDownloadStatusSql.setString(1, credentialId)
          getDownloadStatusSql.setString(2, serviceName)
          getDownloadStatusSql.setInt(3, gracePeriod)
          getDownloadStatusSql.registerOutParameter(4, OracleTypes.INTEGER)
          getDownloadStatusSql.execute()

          getDownloadStatusSql.getInt(4)
        } finally getDownloadStatusSql.close()
      }
    }
  }

  private def readClientList(rs: ResultSet): List[CisTaxpayerSearchResult] = {
    val buffer = scala.collection.mutable.ListBuffer[CisTaxpayerSearchResult]()
    while (rs.next()) {
      buffer += toCisTaxpayerSearchResult(rs)
    }
    buffer.toList
  }

  private def readClientNameChars(rs: ResultSet): List[String] = {
    val buffer = scala.collection.mutable.ListBuffer[String]()
    while (rs.next()) {
      Option(rs.getString("CLIENTNAMESTARTINGCHARACTER"))
        .map(_.trim)
        .filter(_.nonEmpty)
        .foreach(buffer += _)
    }
    buffer.toList
  }

  override def getAllClients(
    irAgentId: String,
    credentialId: String,
    start: Int,
    count: Int,
    sort: Int,
    order: String
  ): Future[CisClientSearchResult] = {
    logger.info(
      s"[CIS] getAllClients(IR_AGENT_ID=$irAgentId, START=$start, COUNT=$count, SORT=$sort, ORDER=$order)"
    )

    Future {
      db.withConnection { conn =>
        val cs: CallableStatement =
          conn.prepareCall("{ call CIS_CLIENT_SEARCH.getAllClients(?, ?, ?, ?, ?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, irAgentId)
          cs.setString(2, credentialId)
          cs.setInt(3, start)
          cs.setInt(4, count)
          cs.setInt(5, sort)
          cs.setString(6, order)
          cs.registerOutParameter(7, OracleTypes.INTEGER)
          cs.registerOutParameter(8, OracleTypes.CURSOR)
          cs.registerOutParameter(9, OracleTypes.CURSOR)
          cs.execute()

          val clientCount = cs.getInt(7)
          val clientListRs = cs.getObject(8, classOf[ResultSet])
          val clientNameCharsRs = cs.getObject(9, classOf[ResultSet])

          try {
            val clients = Option(clientListRs).map(readClientList).getOrElse(List.empty)
            val nameChars = Option(clientNameCharsRs).map(readClientNameChars).getOrElse(List.empty)

            CisClientSearchResult(
              clients                      = clients,
              totalCount                   = clientCount,
              clientNameStartingCharacters = nameChars
            )
          } finally {
            if (clientListRs != null) clientListRs.close()
            if (clientNameCharsRs != null) clientNameCharsRs.close()
          }
        } finally cs.close()
      }
    }
  }

  override def hasClient(irAgentId: String, credentialId: String, taxOfficeNumber: String, taxOfficeReference: String): Future[Boolean] = {
    logger.info(s"[CIS] hasClient(TON=$taxOfficeNumber, TOR=$taxOfficeReference, agentId=$irAgentId)")

    Future {
      db.withConnection { conn =>
        val cs: CallableStatement =
          conn.prepareCall("{ call CIS_CLIENT_SEARCH.hasClient(?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, taxOfficeNumber)
          cs.setString(2, taxOfficeReference)
          cs.setString(3, irAgentId)
          cs.setString(4, credentialId)
          cs.registerOutParameter(5, OracleTypes.INTEGER)
          cs.execute()

          cs.getBoolean(5)
        } finally cs.close()
      }
    }
  }

  override def getSchemePrepopByKnownFacts(
    taxOfficeNumber: String,
    taxOfficeReference: String,
    accountOfficeReference: String
  ): Future[Option[SchemePrepop]] = {
    logger.info(
      s"[CIS] getSchemePrepopByKnownFacts(TON=$taxOfficeNumber, TOR=$taxOfficeReference, AO=$accountOfficeReference)"
    )

    Future {
      db.withConnection { conn =>
        val cs: CallableStatement =
          conn.prepareCall("{ call CISR_PREPOP_PORTAL_PK.getSchemePrepopByKnownFacts(?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, taxOfficeNumber)
          cs.setString(2, taxOfficeReference)
          cs.setString(3, accountOfficeReference)

          cs.registerOutParameter(4, OracleTypes.NUMBER)
          cs.registerOutParameter(5, OracleTypes.CURSOR)

          cs.execute()

          val responseCode = cs.getInt(4)

          if (responseCode != 0) {
            None
          } else {
            val rs = cs.getObject(5, classOf[ResultSet])
            try {
              if (rs != null && rs.next) {
                val first = SchemePrepop(
                  taxOfficeNumber        = rs.getTrimmedOrNull("TAX_OFFICE_NUMBER"),
                  taxOfficeReference     = rs.getTrimmedOrNull("TAX_OFFICE_REF"),
                  accountOfficeReference = rs.getTrimmedOrNull("AO_REF"),
                  utr                    = rs.getTrimmedOpt("UTR"),
                  schemeName             = rs.getTrimmedOrNull("SCHEME_NAME")
                )

                if (rs.next()) {
                  val msg =
                    s"[CIS] getSchemePrepopByKnownFacts: multiple rows returned for " +
                      s"TON=$taxOfficeNumber, TOR=$taxOfficeReference, AO=$accountOfficeReference; this should be unique"
                  logger.error(msg)
                  throw new IllegalStateException(msg)
                }

                Some(first)
              } else None
            } finally if (rs != null) rs.close()
          }
        } finally cs.close()
      }
    }
  }

  override def getSubcontractorsPrepopByKnownFacts(
    taxOfficeNumber: String,
    taxOfficeReference: String,
    accountOfficeReference: String
  ): Future[Seq[SubcontractorPrepopRecord]] = {
    logger.info(
      s"[CIS] getSubcontractorsPrepopByKnownFacts(TON=$taxOfficeNumber, TOR=$taxOfficeReference, AO=$accountOfficeReference)"
    )

    Future {
      db.withConnection { conn =>
        val cs: CallableStatement =
          conn.prepareCall("{ call CISR_PREPOP_PORTAL_PK.getSubcontrsPrepopByKnownFacts(?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, taxOfficeNumber)
          cs.setString(2, taxOfficeReference)
          cs.setString(3, accountOfficeReference)

          cs.registerOutParameter(4, OracleTypes.NUMBER)
          cs.registerOutParameter(5, OracleTypes.CURSOR)

          cs.execute()

          val responseCode = cs.getInt(4)

          if (responseCode != 0) {
            Seq.empty
          } else {
            val rs = cs.getObject(5, classOf[ResultSet])

            val buffer = ListBuffer.empty[SubcontractorPrepopRecord]

            try {
              while (rs != null && rs.next()) {
                buffer += SubcontractorPrepopRecord(
                  subcontractorType  = rs.getTrimmedOrNull("SUBCONTRACTOR_TYPE"),
                  subcontractorUtr   = rs.getTrimmedOrNull("SUBCONTRACTOR_UTR"),
                  verificationNumber = rs.getTrimmedOrNull("VERIFICATION_NUMBER"),
                  verificationSuffix = rs.getTrimmedOpt("VERIFICATION_SUFFIX"),
                  title              = rs.getTrimmedOpt("TITLE"),
                  firstName          = rs.getTrimmedOpt("FIRST_NAME"),
                  secondName         = rs.getTrimmedOpt("SECOND_NAME"),
                  surname            = rs.getTrimmedOpt("SURNAME"),
                  tradingName        = rs.getTrimmedOpt("TRADING_NAME")
                )
              }
            } finally if (rs != null) rs.close()

            buffer.toList
          }
        } finally cs.close()
      }
    }
  }

  override def enqueueMessage(request: EnqueueMessageRequest): Future[Long] = {
    logger.info(
      s"[CIS] enqueueMessage(sender=${request.sender}, queueName=${request.queueName}, filter=${request.filter})"
    )
    Future {
      db.withTransaction { conn =>
        val messageID = callEnqueueMessageHeader(conn          = conn,
                                                 sender        = request.sender,
                                                 queueName     = request.queueName,
                                                 replyQueue    = request.replyQueue,
                                                 correlationId = request.correlationID,
                                                 filter        = request.filter
                                                )

        request.payload.foreach { case (key, value) =>
          callEnqueueClob(
            conn          = conn,
            messageID     = messageID,
            sender        = request.sender,
            queueName     = request.queueName,
            replyQueue    = request.replyQueue,
            correlationId = request.correlationID,
            filter        = request.filter,
            key           = key,
            value         = value
          )
        }
        messageID
      }
    }
  }

  private def callEnqueueMessageHeader(conn: Connection,
                                       sender: String,
                                       queueName: String,
                                       replyQueue: String,
                                       correlationId: String,
                                       filter: String
                                      ): Long = {
    val messageID = withCall(conn, "{ call udas_queue.enqueue_message_header(?, ?, ?, ?, ?, ?) }") { cs =>
      cs.setString(1, sender)
      cs.setString(2, queueName)
      cs.setString(3, replyQueue)
      cs.setString(4, correlationId)
      cs.setString(5, filter)
      cs.registerOutParameter(6, Types.NUMERIC)

      cs.execute()

      val id = cs.getLong(6)
      if (cs.wasNull()) None else Some(id)
    }

    messageID match {
      case Some(id) if id >= 0 => id
      case _ =>
        val errorMessage =
          s"Failed to callEnqueueMessageHeader: sender=$sender, queueName=$queueName, filter=$filter"

        logger.error(errorMessage)
        throw new RuntimeException(errorMessage)
    }
  }

  private def callEnqueueClob(
    conn: Connection,
    messageID: Long,
    sender: String,
    queueName: String,
    replyQueue: String,
    correlationId: String,
    filter: String,
    key: String,
    value: String
  ): Long = {
    val messageIDOut = withCall(conn, "{ call udas_queue.enqueue_clob(?, ?, ?, ?, ?, ?, ?, ?, ?) }") { cs =>
      cs.setLong(1, messageID)
      cs.setString(2, sender)
      cs.setString(3, queueName)
      cs.setString(4, replyQueue)
      cs.setString(5, correlationId)
      cs.setString(6, filter)
      cs.setString(7, key)
      cs.setString(8, value)
      cs.registerOutParameter(9, Types.NUMERIC)

      cs.execute()

      val id = cs.getLong(9)
      if (cs.wasNull()) None else Some(id)
    }

    messageIDOut match {
      case Some(id) if id >= 0 && id == messageID => id
      case _ =>
        val errorMessage =
          s"Failed to enqueue CLOB: messageID=$messageID, messageIDOut=${messageIDOut.getOrElse("null")}, key=$key"

        logger.error(errorMessage)
        throw new RuntimeException(errorMessage)
    }
  }
}
