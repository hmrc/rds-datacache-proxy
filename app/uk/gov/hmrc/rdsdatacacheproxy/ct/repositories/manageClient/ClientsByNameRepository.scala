package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.manageClient

import com.google.inject.ImplementedBy
import oracle.jdbc.OracleTypes
import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.manageClient.{ClientsByName, ClientList}

import java.sql.*
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[ClientsByNameRepositoryImpl])
trait ClientsByNameRepository {
  def getClientsByName(irAgentId: String, credentialId: String, name: String, start: Int, count: Int, sort: Int, order: String): Future[AllClients]
}

class ClientsByNameRepositoryImpl @Inject() (
                                           @NamedDatabase("ct-core") db: Database
                                         )(implicit ec: ExecutionContext)
  extends ClientsByNameRepository
    with Logging {

  def getClientsByName(irAgentId: String, credentialId: String, name: String, start: Int, count: Int, sort: Int, order: String): Future[AllClients] = {
    Future {
      db.withConnection { connect =>
        val sp = connect.prepareCall("{call CT_CLIENT_SEARCH.get_clients_by_name(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")

        try {
          sp.setString(1, irAgentId)
          sp.setString(2, credentialId)
          sp.setString(3, name)
          sp.setInt(4, start) // start starts with 0
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

          ClientsByName(
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
        companyRegNo = rs.getString("company_reg_no"),
        companyName  = rs.getString("company_name"),
        agentOwnRef  = rs.getString("agent_own_ref")
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