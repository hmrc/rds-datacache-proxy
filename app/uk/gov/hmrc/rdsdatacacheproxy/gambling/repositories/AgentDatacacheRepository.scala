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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories

import oracle.jdbc.OracleTypes
import play.api.Logging
import play.api.db.NamedDatabase
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.Regime
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.agent.{AgentClient, AgentClientListResponse}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}

import java.sql.*
import javax.inject.{Inject, Singleton}
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

trait AgentDataSource {
  def getAllClientsDownloadStatus(credentialId: String, regime: String, gracePeriod: Int = 14400): Future[Int]
  def getAllClients(regime: Regime,
                    credentialId: String,
                    start: Int = 0,
                    count: Int = -1,
                    sort: Int = 0,
                    order: String = "ASC"
                   ): Future[Either[StatementError, AgentClientListResponse]]
  def hasClient(regime: Regime, credentialId: String, regNumber: String): Future[Either[StatementError, Boolean]]
}

@Singleton
class AgentDatacacheRepository @Inject() (
  @NamedDatabase("gambling") mgdDb: MGDDatabase,
  @NamedDatabase("gambling.gtr") gtrDb: GTRDatabase
)(implicit ec: ExecutionContext)
    extends AgentDataSource
    with RepositorySupport
    with Logging {

  override def getAllClientsDownloadStatus(credentialId: String, regime: String, gracePeriod: Int): Future[Int] = {
    // NAME: getClientListDownloadStatus
    // DESCRIPTION: Return the status of the client list download process. If no record found, or outside grace period return -1, else return the status.
    // -1 - Update should proceed
    //  0 - Update is in progress
    //  1 - No update in progress, last update succeeded
    //  2 - No update in progress, last update failed

    logger.info(s"getClientListDownloadStatus(credentialId=$credentialId, regime=$regime, gracePeriod=$gracePeriod)")

    Future {
      mgdDb.underlying.withConnection { conn =>
        val cs: CallableStatement =
          conn.prepareCall("{ call CLIENT_LIST_STATUS.GETCLIENTLISTDOWNLOADSTATUS(?, ?, ?, ?) }")

        try {
          cs.setString(1, credentialId)
          cs.setString(2, regime)
          cs.setInt(3, gracePeriod)
          cs.registerOutParameter(4, OracleTypes.INTEGER)
          cs.execute()

          cs.getInt(4)
        } finally cs.close()
      }
    }
  }

  override def getAllClients(regime: Regime,
                             credentialId: String,
                             start: Int,
                             count: Int,
                             sort: Int,
                             order: String
                            ): Future[Either[StatementError, AgentClientListResponse]] = {
    logger.info(s"getAllClients(credentialId=$credentialId, start=$start, count=$count, sort=$sort, order=$order)")

    Future {
      getDb(regime, mgdDb, gtrDb).underlying.withConnection { connection =>
        val cs =
          regime match
            case Regime.MGD => connection.prepareCall("{ call MGD_CLIENT_SEARCH.getAllClients(?, ?, ?, ?, ?, ?, ?, ?) }")
            case Regime.GBD => connection.prepareCall("{ call GTR_GBD_CLIENT_SEARCH.getAllClients(?, ?, ?, ?, ?, ?, ?, ?) }")
            case Regime.PBD => connection.prepareCall("{ call GTR_PBD_CLIENT_SEARCH.getAllClients(?, ?, ?, ?, ?, ?, ?, ?) }")
            case Regime.RGD => connection.prepareCall("{ call GTR_RGD_CLIENT_SEARCH.getAllClients(?, ?, ?, ?, ?, ?, ?, ?) }")

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
            Right(AgentClientListResponse(clients, clientCount, nameChars))
          } finally {
            if (clientListRs != null) clientListRs.close()
            if (clientNameCharsRs != null) clientNameCharsRs.close()
          }
        } finally cs.close()
      }
    }
  }

  override def hasClient(regime: Regime, credentialId: String, regNumber: String): Future[Either[StatementError, Boolean]] = {
    logger.info(s"hasClient(credentialId=$credentialId, regNumber=$regNumber)")

    Future {
      getDb(regime, mgdDb, gtrDb).underlying.withConnection { connection =>
        val cs: CallableStatement =
          regime match
            case Regime.MGD => connection.prepareCall("{ call MGD_CLIENT_SEARCH.hasClient(?, ?, ?) }")
            case Regime.GBD => connection.prepareCall("{ call GTR_GBD_CLIENT_SEARCH.hasClient(?, ?, ?) }")
            case Regime.PBD => connection.prepareCall("{ call GTR_PBD_CLIENT_SEARCH.hasClient(?, ?, ?) }")
            case Regime.RGD => connection.prepareCall("{ call GTR_RGD_CLIENT_SEARCH.hasClient(?, ?, ?) }")

        try {
          cs.setString(1, credentialId)
          cs.setString(2, regNumber)
          cs.registerOutParameter(3, OracleTypes.INTEGER) // P_EXISTS_O: 1=exists, 0=not
          cs.execute()

          Right(cs.getInt(3) == 1)
        } finally cs.close()
      }
    }
  }

  private def readClientList(rs: ResultSet): List[AgentClient] = {
    val buffer = ListBuffer[AgentClient]()
    while (rs.next()) {
      buffer += AgentClient(
        clientName  = Option(rs.getString("CLIENT_NAME")).map(_.trim).getOrElse(""),
        regNumber   = Option(rs.getString("MGD_REG_NUMBER")).map(_.trim).getOrElse(""), // TODO : add other regimes
        agentOwnRef = Option(rs.getString("AGENT_OWN_REF")).map(_.trim).getOrElse("")
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
}
