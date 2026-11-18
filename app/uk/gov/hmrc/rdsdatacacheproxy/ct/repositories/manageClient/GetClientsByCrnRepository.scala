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

import java.sql.ResultSet
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future, blocking}

@ImplementedBy(classOf[GetClientsByCrnRepositoryImpl])
trait GetClientsByCrnRepository {
  def getClientByCrn(irAgentReference: String, credentialId: String, companyRegNo: String): Future[AllClients]
}

class GetClientsByCrnRepositoryImpl @Inject() (@NamedDatabase("ct-core") db: Database)(implicit ec: ExecutionContext)
    extends GetClientsByCrnRepository
    with RepositoryDataSupport
    with Logging {

  override def getClientByCrn(irAgentReference: String, credentialId: String, companyRegNo: String): Future[AllClients] = {
    val context = s"Invoking repository"
    logger.info(s"Retrieving Clients Details by CRN")

    Future(blocking {
      db.withConnection { connection =>

        val cs = connection.prepareCall("{call CT_CLIENT_SEARCH.get_client_by_crn(? ,? , ? , ?, ? , ?)}")

        try {
          cs.setString(1, irAgentReference)
          cs.setString(2, credentialId)
          cs.setString(3, companyRegNo)

          cs.registerOutParameter(4, OracleTypes.NUMERIC)
          cs.registerOutParameter(5, OracleTypes.CURSOR)
          cs.registerOutParameter(6, OracleTypes.CURSOR)

          cs.execute()

          val clientListResponse: List[ClientList] = processResultSetList(cs, 5, processClientList, context)

          val clientNameStartChars: List[String] = processResultSetList(cs, 6, processNameStartChars, context)

          AllClients(
            clientCount          = cs.getInt(4),
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
      utr          = rs.getString("UTR"),
      companyRegNo = Option(rs.getString("COMPANY_REG_NO")),
      companyName  = rs.getString("COMPANY_NAME"),
      agentOwnRef  = Option(rs.getString("AGENT_OWN_REF"))
    )
  }
  private def processNameStartChars(rs: ResultSet): String = {
    rs.getString("CLIENTNAMESTARTINGCHARACTER")
  }

}
