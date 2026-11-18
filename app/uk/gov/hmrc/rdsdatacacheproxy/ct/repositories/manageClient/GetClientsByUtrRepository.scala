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

@ImplementedBy(classOf[GetClientsByUtrRepositoryImpl])
trait GetClientsByUtrRepository {
  def getClientsByUtr(irAgentId: String, credentialId: String, utr: String): Future[AllClients]
}

class GetClientsByUtrRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends GetClientsByUtrRepository
    with Logging {

  def getClientsByUtr(irAgentId: String, credentialId: String, utr: String): Future[AllClients] = {
    Future {
      db.withConnection { connect =>
        val sp = connect.prepareCall(
          "{call CT_CLIENT_SEARCH.get_client_by_utr(?, ?, ?, ?, ?)}"
        )

        try {
          sp.setString(1, irAgentId)
          sp.setString(2, credentialId)
          sp.setString(3, utr)

          sp.registerOutParameter(4, OracleTypes.CURSOR)
          sp.registerOutParameter(5, OracleTypes.CURSOR)

          sp.execute()

          val clientListDetails =
            sp.getObject(4, classOf[ResultSet])

          val clientNameStartCharsDetails =
            sp.getObject(5, classOf[ResultSet])

          val clientList = readClientList(clientListDetails)

          val clientNameStartChars =
            readClientNameStartChars(clientNameStartCharsDetails)

          AllClients(
            clientCount          = clientList.size,
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
        companyRegNo = Option(rs.getString("company_reg_no")),
        companyName  = rs.getString("company_name"),
        agentOwnRef  = Option(rs.getString("agent_own_ref"))
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
