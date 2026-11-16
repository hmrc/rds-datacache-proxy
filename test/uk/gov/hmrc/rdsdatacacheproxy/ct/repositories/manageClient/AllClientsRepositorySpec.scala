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

import java.sql.{CallableStatement, Date, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class AllClientsRepositorySpec
    extends AnyFreeSpec
    with Matchers
    with BeforeAndAfter
    with AllClientsHelper {

  var db: Database = _
  var repo: AllClientsRepositoryImpl = _
  var mockConnection: java.sql.Connection = _
  var mockCS: CallableStatement = _
  var rs: ResultSet = _

  before {
    db             = mock(classOf[Database])
    mockConnection = mock(classOf[java.sql.Connection])
    mockCS         = mock(classOf[CallableStatement])
    rs             = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCS)

    repo = new AllClientsRepositoryImpl(db)
  }

  "getAllClients" - {
    "return all clients" in {
      when(mockCS.getObject(eqTo(8), eqTo(classOf[ResultSet]))).thenReturn(rs1)
      when(mockCS.getObject(eqTo(9), eqTo(classOf[ResultSet]))).thenReturn(rs2)
      when(rs1.next()).thenReturn(true, false)
      when(rs2.next()).thenReturn(true, false)

      when(rs1.getString("utr")).thenReturn("12345")
      when(rs1.getString("company_reg_no")).thenReturn("01234A")
      when(rs1.getString("company_name")).thenReturn("ABC Limited")
      when(rs1.getString("agent_own_ref")).thenReturn("ABC10")

      when(rs2.getString("client_name_start_chars")).thenReturn("A")

      val result = repo.getAllClients("irAgentId1", "credentialId1", 0, -1, 0, "ASC").futureValue
      result shouldBe allClients

      verify(mockConnection).prepareCall("{call CT_CLIENT_SEARCH.Get_All_Clients(?, ?, ?, ?, ?, ?, ?, ?, ?)}")

      verify(mockCS).setString(1, irAgentId)
      verify(mockCS).setString(2, credentialId)
      verify(mockCS).setInt(3, start) // start starts with 0
      verify(mockCS).setInt(4, count)
      verify(mockCS).setInt(5, sort)
      verify(mockCS).setString(6, order)

      verify(mockCS).registerOutParameter(7, OracleTypes.NUMERIC)
      verify(mockCS).registerOutParameter(8, OracleTypes.CURSOR)
      verify(mockCS).registerOutParameter(9, OracleTypes.CURSOR)

      verify(mockCS).execute()

      verify(rs1, times(2)).next()
      verify(rs2, times(2)).next()

      verify(mockCS).close()
    }

    "return multiple items of client list in all clients" in {
      when(mockCS.getObject(eqTo(8), eqTo(classOf[ResultSet]))).thenReturn(rs1)
      when(mockCS.getObject(eqTo(9), eqTo(classOf[ResultSet]))).thenReturn(rs2)
      when(rs1.next()).thenReturn(true, false)
      when(rs2.next()).thenReturn(true, false)

      when(rs1.getString("utr")).thenReturn("12345", "67890")
      when(rs1.getString("company_reg_no")).thenReturn("01234A", "6789B")
      when(rs1.getString("company_name")).thenReturn("ABC Limited", "DEF Limited")
      when(rs1.getString("agent_own_ref")).thenReturn("ABC10", "DEF10")

      when(rs2.getString("client_name_start_chars")).thenReturn("A")

      val result = repo.getAllClients("irAgentId2", "credentialId2", 0, -1, 0, "ASC").futureValue
      result shouldBe multipleClientListAllClients

      verify(mockConnection).prepareCall("{call CT_CLIENT_SEARCH.Get_All_Clients(?, ?, ?, ?, ?, ?, ?, ?, ?)}")

      verify(mockCS).setString(1, irAgentId)
      verify(mockCS).setString(2, credentialId)
      verify(mockCS).setInt(3, start) // start starts with 0
      verify(mockCS).setInt(4, count)
      verify(mockCS).setInt(5, sort)
      verify(mockCS).setString(6, order)

      verify(mockCS).registerOutParameter(7, OracleTypes.NUMERIC)
      verify(mockCS).registerOutParameter(8, OracleTypes.CURSOR)
      verify(mockCS).registerOutParameter(9, OracleTypes.CURSOR)

      verify(mockCS).execute()

      verify(rs1, times(2)).next()
      verify(rs2, times(2)).next()

      verify(mockCS).close()
    }

    "return multiple items of client name chars in all clients" in {
      when(mockCS.getObject(eqTo(8), eqTo(classOf[ResultSet]))).thenReturn(rs1)
      when(mockCS.getObject(eqTo(9), eqTo(classOf[ResultSet]))).thenReturn(rs2)
      when(rs1.next()).thenReturn(true, false)
      when(rs2.next()).thenReturn(true, false)

      when(rs1.getString("utr")).thenReturn("111213")
      when(rs1.getString("company_reg_no")).thenReturn("111213C")
      when(rs1.getString("company_name")).thenReturn("GHI Limited")
      when(rs1.getString("agent_own_ref")).thenReturn("GHI10")

      when(rs2.getString("client_name_start_chars")).thenReturn("G", "H", "I")

      val result = repo.getAllClients("irAgentId3", "credentialId3", 0, -1, 0, "ASC").futureValue
      result shouldBe multipleItemsClientNameAllClients

      verify(mockConnection).prepareCall("{call CT_CLIENT_SEARCH.Get_All_Clients(?, ?, ?, ?, ?, ?, ?, ?, ?)}")

      verify(mockCS).setString(1, irAgentId)
      verify(mockCS).setString(2, credentialId)
      verify(mockCS).setInt(3, start) // start starts with 0
      verify(mockCS).setInt(4, count)
      verify(mockCS).setInt(5, sort)
      verify(mockCS).setString(6, order)

      verify(mockCS).registerOutParameter(7, OracleTypes.NUMERIC)
      verify(mockCS).registerOutParameter(8, OracleTypes.CURSOR)
      verify(mockCS).registerOutParameter(9, OracleTypes.CURSOR)

      verify(mockCS).execute()

      verify(rs1, times(2)).next()
      verify(rs2, times(2)).next()

      verify(mockCS).close()
    }

    "return an exception and close the connection" in {
      when(mockCS.execute()).thenThrow(new RuntimeException("DB error"))

      val ex = repo.getAllClients("irAgentIdNA", "credentialIdNA", 0, -1, 0, "ASC").failed.futureValue
      ex.getMessage should include("DB error")

      verify(mockCS).close()
    }
  }
}
