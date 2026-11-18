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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.manageClient

import com.google.inject.ImplementedBy
import oracle.jdbc.OracleTypes
import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.manageClient.{AllClients, ClientList}

import java.sql.*
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[ClientsByAgentOwnRefRepositoryImpl])
trait ClientsByAgentOwnRefRepository {
  def getClientsByAgentOwnRef(irAgentId: String, credentialId: String, clientId: String, start: Int, count: Int, sort: Int, order: String): Future[AllClients]
}

class ClientsByAgentOwnRefRepositoryImpl @Inject() (
                                              @NamedDatabase("ct-core") db: Database
                                            )(implicit ec: ExecutionContext)
  extends ClientsByAgentOwnRefRepository
    with Logging {

  def getClientsByAgentOwnRef(irAgentId: String, credentialId: String, clientId: String, start: Int, count: Int, sort: Int, order: String): Future[AllClients] = {
    Future {
      db.withConnection { connect =>
        val sp = connect.prepareCall("{call CT_CLIENT_SEARCH.get_clients_by_agents_own_ref(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")

        try {
          sp.setString(1, irAgentId)
          sp.setString(2, credentialId)
          sp.setString(3, clientId)
          sp.setInt(4, start)
          sp.setInt(5, count)
          sp.setInt(6, sort)
          sp.setString(7, order)

          sp.registerOutParameter(8, OracleTypes.NUMERIC)
          sp.registerOutParameter(9, OracleTypes.CURSOR)
          sp.registerOutParameter(10, OracleTypes.CURSOR)

          sp.execute()

          val clientListDetails = sp.getObject(9, classOf[ResultSet])
          val clientNameStartCharsDetails = sp.getObject(10, classOf[ResultSet])

          val clientList = readClientList(clientListDetails)
          val clientNameStartChars = readClientNameStartChars(clientNameStartCharsDetails)

          AllClients(
            clientCount          = sp.getInt(8),
            clientList           = clientList,
            clientNameStartChars = clientNameStartChars
          )

        } finally {
          sp.close()
        }
      }
    }
  }

  private def readClientList(rs: ResultSet): List[ClientList] = {
    val buffer = ListBuffer[ClientList]()
    while (rs.next()) {
      buffer += ClientList(
        utr          = rs.getString("utr"),
        companyRegNo = Some(rs.getString("company_reg_no")),
        companyName  = rs.getString("company_name"),
        agentOwnRef  = Some(rs.getString("agent_own_ref"))
      )
    }
    buffer.toList
  }

  private def readClientNameStartChars(rs: ResultSet): List[String] = {
    val buffer = ListBuffer[String]()
    while (rs.next()) {
      buffer += rs.getString("clientnamestartingcharacter")
    }
    buffer.toList
  }
}