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
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.manageClient.AllClientsHelper

import java.sql.{CallableStatement, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class ClientsByAgentOwnRefRepositorySpec extends AnyFreeSpec with Matchers with BeforeAndAfter with AllClientsHelper {

  var db: Database = _
  var repo: ClientsByAgentOwnRefRepositoryImpl = _
  var mockConnection: java.sql.Connection = _
  var mockCS: CallableStatement = _
  var rs1: ResultSet = _
  var rs2: ResultSet = _

  before {
    db             = mock(classOf[Database])
    mockConnection = mock(classOf[java.sql.Connection])
    mockCS         = mock(classOf[CallableStatement])
    rs1            = mock(classOf[ResultSet])
    rs2            = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCS)

    repo = new ClientsByAgentOwnRefRepositoryImpl(db)
  }

  "getClientsByAgentOwnRef" - {
    "return clients by agent own ref" in {
      when(mockCS.getObject(eqTo(9), eqTo(classOf[ResultSet]))).thenReturn(rs1)
      when(mockCS.getObject(eqTo(10), eqTo(classOf[ResultSet]))).thenReturn(rs2)
      when(rs1.next()).thenReturn(true, false)
      when(rs2.next()).thenReturn(true, false)

      when(mockCS.getInt(8)).thenReturn(100)

      when(rs1.getString("utr")).thenReturn("12345")
      when(rs1.getString("company_reg_no")).thenReturn("01234A")
      when(rs1.getString("company_name")).thenReturn("ABC Limited")
      when(rs1.getString("agent_own_ref")).thenReturn("ABC10")

      when(rs2.getString("clientnamestartingcharacter")).thenReturn("A")

      val result = repo.getClientsByAgentOwnRef("irAgentId1", "credentialId1", "clientId1", 0, -1, 0, "ASC").futureValue
      result shouldBe allClients

      verify(mockConnection).prepareCall("{call CT_CLIENT_SEARCH.get_clients_by_agents_own_ref(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")

      verify(mockCS).setString(1, "irAgentId1")
      verify(mockCS).setString(2, "credentialId1")
      verify(mockCS).setString(3, "clientId1")
      verify(mockCS).setInt(4, 0) // start starts with 0
      verify(mockCS).setInt(5, -1)
      verify(mockCS).setInt(6, 0)
      verify(mockCS).setString(7, "ASC")

      verify(mockCS).registerOutParameter(8, OracleTypes.NUMERIC)
      verify(mockCS).registerOutParameter(9, OracleTypes.CURSOR)
      verify(mockCS).registerOutParameter(10, OracleTypes.CURSOR)

      verify(mockCS).execute()

      verify(rs1, times(2)).next()
      verify(rs2, times(2)).next()

      verify(mockCS).close()
    }

    "return multiple items of client list in clients by agent own ref" in {
      when(mockCS.getObject(eqTo(9), eqTo(classOf[ResultSet]))).thenReturn(rs1)
      when(mockCS.getObject(eqTo(10), eqTo(classOf[ResultSet]))).thenReturn(rs2)
      when(rs1.next()).thenReturn(true, true, false)
      when(rs2.next()).thenReturn(true, false)

      when(mockCS.getInt(8)).thenReturn(100)

      when(rs1.getString("utr")).thenReturn("12345", "67890")
      when(rs1.getString("company_reg_no")).thenReturn("01234A", "6789B")
      when(rs1.getString("company_name")).thenReturn("ABC Limited", "DEF Limited")
      when(rs1.getString("agent_own_ref")).thenReturn("ABC10", "DEF10")

      when(rs2.getString("clientnamestartingcharacter")).thenReturn("D")

      val result = repo.getClientsByAgentOwnRef("irAgentId2", "credentialId2", "clientId2", 0, -1, 0, "ASC").futureValue
      result shouldBe multipleClientListAllClients

      verify(mockConnection).prepareCall("{call CT_CLIENT_SEARCH.get_clients_by_agents_own_ref(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")

      verify(mockCS).setString(1, "irAgentId2")
      verify(mockCS).setString(2, "credentialId2")
      verify(mockCS).setString(3, "clientId2")
      verify(mockCS).setInt(4, 0) // start starts with 0
      verify(mockCS).setInt(5, -1)
      verify(mockCS).setInt(6, 0)
      verify(mockCS).setString(7, "ASC")

      verify(mockCS).registerOutParameter(8, OracleTypes.NUMERIC)
      verify(mockCS).registerOutParameter(9, OracleTypes.CURSOR)
      verify(mockCS).registerOutParameter(10, OracleTypes.CURSOR)

      verify(mockCS).execute()

      verify(rs1, times(3)).next()
      verify(rs2, times(2)).next()

      verify(mockCS).close()
    }

    "return multiple items of client agent own ref chars in all clients" in {
      when(mockCS.getObject(eqTo(9), eqTo(classOf[ResultSet]))).thenReturn(rs1)
      when(mockCS.getObject(eqTo(10), eqTo(classOf[ResultSet]))).thenReturn(rs2)
      when(rs1.next()).thenReturn(true, false)
      when(rs2.next()).thenReturn(true, true, true, false)

      when(mockCS.getInt(8)).thenReturn(100)

      when(rs1.getString("utr")).thenReturn("111213")
      when(rs1.getString("company_reg_no")).thenReturn("111213C")
      when(rs1.getString("company_name")).thenReturn("GHI Limited")
      when(rs1.getString("agent_own_ref")).thenReturn("GHI10")

      when(rs2.getString("clientnamestartingcharacter")).thenReturn("G", "H", "I")

      val result = repo.getClientsByAgentOwnRef("irAgentId3", "credentialId3", "clientId3", 0, -1, 0, "ASC").futureValue
      result shouldBe multipleItemsClientNameAllClients

      verify(mockConnection).prepareCall("{call CT_CLIENT_SEARCH.get_clients_by_agents_own_ref(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")

      verify(mockCS).setString(1, "irAgentId3")
      verify(mockCS).setString(2, "credentialId3")
      verify(mockCS).setString(3, "clientId3")
      verify(mockCS).setInt(4, 0) // start starts with 0
      verify(mockCS).setInt(5, -1)
      verify(mockCS).setInt(6, 0)
      verify(mockCS).setString(7, "ASC")

      verify(mockCS).registerOutParameter(8, OracleTypes.NUMERIC)
      verify(mockCS).registerOutParameter(9, OracleTypes.CURSOR)
      verify(mockCS).registerOutParameter(10, OracleTypes.CURSOR)

      verify(mockCS).execute()

      verify(rs1, times(2)).next()
      verify(rs2, times(4)).next()

      verify(mockCS).close()
    }

    "return an exception and close the connection" in {
      when(mockCS.execute()).thenThrow(new RuntimeException("DB error"))

      val ex = repo.getClientsByAgentOwnRef("irAgentIdNA", "credentialIdNA", "clientIdNA", 0, -1, 0, "ASC").failed.futureValue
      ex.getMessage should include("DB error")

      verify(mockCS).close()
    }
  }
}
