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

import oracle.jdbc.OracleTypes
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.manageClient.{AllClients, ClientList}

import java.sql.{CallableStatement, Connection, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class GetClientsByCrnRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var repository: GetClientsByCrnRepositoryImpl = _
  var db: Database = _
  var mockConnection: Connection = _
  var mockCallableStatement: CallableStatement = _
  var mockResultSet1: ResultSet = _
  var mockResultSet2: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[Connection])
    mockCallableStatement = mock(classOf[CallableStatement])
    mockResultSet1        = mock(classOf[ResultSet])
    mockResultSet2        = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repository = new GetClientsByCrnRepositoryImpl(db)

  }

  "getClientByCrn" should "return AllClients with single item in ClientList from DB when stored procedure is invoked" in {
    val irAgentReference = "STN006"
    val credentialId = "78366363"
    val companyRegNo = "REG-67-63"

    when(mockCallableStatement.getObject(eqTo(4), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet1)
    when(mockCallableStatement.getObject(eqTo(5), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet2)
    when(mockResultSet1.next()).thenReturn(true, false)
    when(mockResultSet2.next()).thenReturn(true, false)

    when(mockResultSet1.getString("UTR")).thenReturn("792732")
    when(mockResultSet1.getString("COMPANY_REG_NO")).thenReturn("M67532")
    when(mockResultSet1.getString("COMPANY_NAME")).thenReturn("Rubix Solutions")
    when(mockResultSet1.getString("AGENT_OWN_REF")).thenReturn("8754000057")

    when(mockResultSet2.getString("CLIENTNAMESTARTINGCHARACTER")).thenReturn("A")

    val clientListResult = List(
      ClientList(
        utr          = "792732",
        companyRegNo = Some("M67532"),
        companyName  = "Rubix Solutions",
        agentOwnRef  = Some("8754000057")
      )
    )

    val clientNameChars = List("A")

    val result = repository.getClientByCrn(irAgentReference, credentialId, companyRegNo).futureValue

    result shouldBe AllClients(
      clientCount          = 1,
      clientList           = clientListResult,
      clientNameStartChars = clientNameChars
    )

    verify(mockConnection).prepareCall("{call CT_CLIENT_SEARCH.get_client_by_crn(? ,? , ? , ?, ?)}")

    verify(mockCallableStatement).setString(1, irAgentReference)
    verify(mockCallableStatement).setString(2, credentialId)
    verify(mockCallableStatement).setString(3, companyRegNo)

    verify(mockCallableStatement).registerOutParameter(4, OracleTypes.CURSOR)
    verify(mockCallableStatement).registerOutParameter(5, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockResultSet1, times(2)).next()
    verify(mockResultSet2, times(2)).next()

    verify(mockCallableStatement).close()

  }

  "getClientByCrn" should "return an empty list of clientList from DB when stored procedure is invoked and resultSet is null" in {

    val irAgentReference = "STN006"
    val credentialId = "78366363"
    val companyRegNo = "REG-67-63"

    when(mockCallableStatement.getObject(eqTo(4), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet1)
    when(mockCallableStatement.getObject(eqTo(5), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet2)
    when(mockResultSet1.next()).thenReturn(false)
    when(mockResultSet2.next()).thenReturn(false)

    val result = repository.getClientByCrn(irAgentReference, credentialId, companyRegNo).futureValue

    result shouldBe AllClients(
      clientCount          = 0,
      clientList           = List.empty,
      clientNameStartChars = List.empty
    )

    verify(mockConnection).prepareCall("{call CT_CLIENT_SEARCH.get_client_by_crn(? ,? , ? , ?, ?)}")

    verify(mockCallableStatement).setString(1, irAgentReference)
    verify(mockCallableStatement).setString(2, credentialId)
    verify(mockCallableStatement).setString(3, companyRegNo)

    verify(mockCallableStatement).registerOutParameter(4, OracleTypes.CURSOR)
    verify(mockCallableStatement).registerOutParameter(5, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockResultSet1, times(1)).next()
    verify(mockResultSet2, times(1)).next()

    verify(mockCallableStatement).close()

  }

  "getClientByCrn" should "return clientList containing multiple elements of clientList from DB when stored procedure is invoked" in {
    val irAgentReference = "STN006"
    val credentialId = "78366363"
    val companyRegNo = "REG-67-63"

    when(mockCallableStatement.getObject(eqTo(4), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet1)
    when(mockCallableStatement.getObject(eqTo(5), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet2)
    when(mockResultSet1.next()).thenReturn(true, true, false)
    when(mockResultSet2.next()).thenReturn(true, true, false)

    when(mockResultSet1.getString("UTR")).thenReturn("792732", "12345")
    when(mockResultSet1.getString("COMPANY_REG_NO")).thenReturn("M67532", "HJUT12")
    when(mockResultSet1.getString("COMPANY_NAME")).thenReturn("Rubix Solutions", "Rowley Tyres")
    when(mockResultSet1.getString("AGENT_OWN_REF")).thenReturn("8754000057", "79734973")

    when(mockResultSet2.getString("CLIENTNAMESTARTINGCHARACTER")).thenReturn("R", "R")

    val clientListResult = List(
      ClientList(
        utr          = "792732",
        companyRegNo = Some("M67532"),
        companyName  = "Rubix Solutions",
        agentOwnRef  = Some("8754000057")
      ),
      ClientList(
        utr          = "12345",
        companyRegNo = Some("HJUT12"),
        companyName  = "Rowley Tyres",
        agentOwnRef  = Some("79734973")
      )
    )

    val clientNameChars = List("R", "R")

    val result = repository.getClientByCrn(irAgentReference, credentialId, companyRegNo).futureValue

    result shouldBe AllClients(
      clientCount          = clientListResult.size,
      clientList           = clientListResult,
      clientNameStartChars = clientNameChars
    )

    verify(mockConnection).prepareCall("{call CT_CLIENT_SEARCH.get_client_by_crn(? ,? , ? , ?, ?)}")

    verify(mockCallableStatement).setString(1, irAgentReference)
    verify(mockCallableStatement).setString(2, credentialId)
    verify(mockCallableStatement).setString(3, companyRegNo)

    verify(mockCallableStatement).registerOutParameter(4, OracleTypes.CURSOR)
    verify(mockCallableStatement).registerOutParameter(5, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockResultSet1, times(3)).next()
    verify(mockResultSet2, times(3)).next()

    verify(mockCallableStatement).close()

  }

  "getReallocationFromAccPeriod" should "return an exception and close the connection when an exception occurs in Downstream services" in {
    val irAgentReference = "STN006"
    val credentialId = "78366363"
    val companyRegNo = "REG-67-63"

    when(mockCallableStatement.execute()).thenThrow(new RuntimeException("DB error"))

    val ex = repository.getClientByCrn(irAgentReference, credentialId, companyRegNo).failed.futureValue
    ex.getMessage should include("DB error")

    verify(mockCallableStatement).close()
  }

}
