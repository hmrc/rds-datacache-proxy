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
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.RepositoryDataSupport

import java.sql.*
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future, blocking}

@ImplementedBy(classOf[GetClientsByUtrRepositoryImpl])
trait GetClientsByUtrRepository {
  def getClientsByUtr(irAgentId: String, credentialId: String, utr: String): Future[AllClients]
}

class GetClientsByUtrRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends GetClientsByUtrRepository
    with RepositoryDataSupport
    with Logging {

  def getClientsByUtr(irAgentReference: String, credentialId: String, utr: String): Future[AllClients] = {
    val context = s"Invoking repository for utr:$utr"
    logger.info(s"Retrieving Clients Details by CRN")
    Future(blocking {
      db.withConnection { connection =>

        val cs = connection.prepareCall("{call CT_CLIENT_SEARCH.get_client_by_utr(? ,? , ? , ?, ?)}")

        try {
          cs.setString(1, irAgentReference)
          cs.setString(2, credentialId)
          cs.setString(3, utr)

          cs.registerOutParameter(4, OracleTypes.CURSOR)
          cs.registerOutParameter(5, OracleTypes.CURSOR)

          cs.execute()

          val clientListResponse: List[ClientList] = processResultSetList(cs, 4, processClientList, context)

          val clientNameStartChars: List[String] = processResultSetList(cs, 5, processNameStartChars, context)

          AllClients(
            clientCount          = clientListResponse.size,
            clientList           = clientListResponse,
            clientNameStartChars = clientNameStartChars
          )

        } finally {
          cs.close()
        }

      }
    })
  }

  private def processClientList(rs: ResultSet): ClientList = {
    ClientList(
      utr          = rs.getString("utr"),
      companyRegNo = Option(rs.getString("company_reg_no")),
      companyName  = rs.getString("company_name"),
      agentOwnRef  = Option(rs.getString("agent_own_ref"))
    )
  }

  private def processNameStartChars(rs: ResultSet): String = {
    rs.getString("clientnamestartingcharacter")
  }
}
