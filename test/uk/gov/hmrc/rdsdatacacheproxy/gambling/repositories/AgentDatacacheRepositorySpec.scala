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
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.Regime
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.Regime.MGD
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.agent.ClientListDownloadStatus.Succeeded
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.agent.{AgentClient, AgentClientListResponse}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}

import java.sql.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

class AgentDatacacheRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  private var repository: AgentDatacacheRepository = _
  private val gtrDb: GTRDatabase = mock(classOf[Database]).asInstanceOf[GTRDatabase]
  private val mgdDb: MGDDatabase = mock(classOf[Database]).asInstanceOf[MGDDatabase]
  private val mgdMockConnection: Connection = mock(classOf[Connection])
  private val gtrMockConnection: Connection = mock(classOf[Connection])
  private val mockCsMgd: CallableStatement = mock(classOf[CallableStatement])
  private val mockCsGtr: CallableStatement = mock(classOf[CallableStatement])

  private val testClientList = AgentClientListResponse(
    clients = List(
      AgentClient("111222333", "Jones Motors Ltd", "123"),
      AgentClient("444555666", "Smith Supplies", "456")
    ),
    totalCount                   = 2,
    clientNameStartingCharacters = List("J", "S")
  )

  private val emptyClientList = AgentClientListResponse(
    clients                      = List.empty,
    totalCount                   = 0,
    clientNameStartingCharacters = List.empty
  )

  before {

    repository = new AgentDatacacheRepository(mgdDb = mgdDb, gtrDb = gtrDb)

    Mockito.reset(mgdDb, gtrDb, mgdMockConnection, gtrMockConnection, mockCsMgd, mockCsGtr)
    when(mgdDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mgdMockConnection)
    }

    when(gtrDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(gtrMockConnection)
    }

    when(mgdMockConnection.prepareCall(any[String])).thenReturn(mockCsMgd)
    when(gtrMockConnection.prepareCall(any[String])).thenReturn(mockCsGtr)
  }

  "getClientListDownloadStatus" should "return the status integer from the stored procedure" in {
    when(mockCsMgd.getInt(4)).thenReturn(1)

    val result = repository.getAllClientsDownloadStatus("cred-123", "GAMBLING", 14400).futureValue

    result shouldBe Right(Succeeded)
    verify(mockCsMgd).setString(1, "cred-123")
    verify(mockCsMgd).setString(2, "GAMBLING")
    verify(mockCsMgd).setInt(3, 14400)
    verify(mockCsMgd).registerOutParameter(4, OracleTypes.INTEGER)
    verify(mockCsMgd).execute()
    verify(mockCsMgd).close()
  }

  Regime.values.toList.filter(_ != Regime.MGD).foreach { regime =>
    "getAllClients" should s"return a populated AgentClientListResponse when regime is $regime" in {
      val clientListRs = mock(classOf[ResultSet])
      val clientNameCharsRs = mock(classOf[ResultSet])

      when(mockCsGtr.getInt(6)).thenReturn(2)
      when(mockCsGtr.getObject(7, classOf[ResultSet])).thenReturn(clientListRs)
      when(mockCsGtr.getObject(8, classOf[ResultSet])).thenReturn(clientNameCharsRs)

      when(clientListRs.next()).thenReturn(true, true, false)
      when(clientListRs.getString("CLIENT_NAME")).thenReturn("Jones Motors Ltd", "Smith Supplies")
      when(clientListRs.getString("MGD_REG_NUMBER")).thenReturn("111222333", "444555666")
      when(clientListRs.getString("AGENT_OWN_REF")).thenReturn("123", "456")

      when(clientNameCharsRs.next()).thenReturn(true, true, false)
      when(clientNameCharsRs.getString("CLIENTNAMESTARTINGCHARACTER")).thenReturn("J", "S")

      val result = repository.getAllClients(regime, "cred-123", 0, -1, 0, "ASC").futureValue
      result shouldBe Right(testClientList)

      verify(mockCsGtr).setString(1, "cred-123")
      verify(mockCsGtr).setInt(2, 0)
      verify(mockCsGtr).setInt(3, -1)
      verify(mockCsGtr).setInt(4, 0)
      verify(mockCsGtr).setString(5, "ASC")
      verify(mockCsGtr).registerOutParameter(6, OracleTypes.INTEGER)
      verify(mockCsGtr).registerOutParameter(7, OracleTypes.CURSOR)
      verify(mockCsGtr).registerOutParameter(8, OracleTypes.CURSOR)
      verify(mockCsGtr).execute()
      verify(mockCsGtr).close()
    }

    "getAllClients" should s"return empty response when cursors are null when regime is $regime" in {
      when(mockCsGtr.getInt(6)).thenReturn(0)
      when(mockCsGtr.getObject(7, classOf[ResultSet])).thenReturn(null)
      when(mockCsGtr.getObject(8, classOf[ResultSet])).thenReturn(null)

      val result = repository.getAllClients(regime, "cred-123", 0, -1, 0, "ASC").futureValue
      result shouldBe Right(emptyClientList)
    }
  }

  Regime.values.toList.filter(_ != Regime.MGD).foreach { regime =>
    "hasClient" should s"return true when stored procedure returns 1 when regime is $regime" in {
      when(mockCsGtr.getInt(3)).thenReturn(1)

      val result = repository.hasClient(regime, "cred-123", "111222333").futureValue
      result shouldBe Right(true)

      verify(mockCsGtr).setString(1, "cred-123")
      verify(mockCsGtr).setString(2, "111222333")
      verify(mockCsGtr).registerOutParameter(3, OracleTypes.INTEGER)
      verify(mockCsGtr).execute()
      verify(mockCsGtr).close()
    }

    "hasClient" should s"return false when stored procedure returns 0 when regime is $regime" in {
      when(mockCsGtr.getInt(3)).thenReturn(0)

      val result = repository.hasClient(regime, "cred-123", "999999999").futureValue
      result shouldBe Right(false)
    }
  }

  // MGD ---------------------------------------------------------------------------------------------------
  "getAllClients" should s"return a populated AgentClientListResponse when regime is MGD" in {
    val clientListRs = mock(classOf[ResultSet])
    val clientNameCharsRs = mock(classOf[ResultSet])

    when(mockCsMgd.getInt(6)).thenReturn(2)
    when(mockCsMgd.getObject(7, classOf[ResultSet])).thenReturn(clientListRs)
    when(mockCsMgd.getObject(8, classOf[ResultSet])).thenReturn(clientNameCharsRs)

    when(clientListRs.next()).thenReturn(true, true, false)
    when(clientListRs.getString("CLIENT_NAME")).thenReturn("Jones Motors Ltd", "Smith Supplies")
    when(clientListRs.getString("MGD_REG_NUMBER")).thenReturn("111222333", "444555666")
    when(clientListRs.getString("AGENT_OWN_REF")).thenReturn("123", "456")

    when(clientNameCharsRs.next()).thenReturn(true, true, false)
    when(clientNameCharsRs.getString("CLIENTNAMESTARTINGCHARACTER")).thenReturn("J", "S")

    val result = repository.getAllClients(MGD, "cred-123", 0, -1, 0, "ASC").futureValue
    result shouldBe Right(testClientList)

    verify(mockCsMgd).setString(1, "cred-123")
    verify(mockCsMgd).setInt(2, 0)
    verify(mockCsMgd).setInt(3, -1)
    verify(mockCsMgd).setInt(4, 0)
    verify(mockCsMgd).setString(5, "ASC")
    verify(mockCsMgd).registerOutParameter(6, OracleTypes.INTEGER)
    verify(mockCsMgd).registerOutParameter(7, OracleTypes.CURSOR)
    verify(mockCsMgd).registerOutParameter(8, OracleTypes.CURSOR)
    verify(mockCsMgd).execute()
    verify(mockCsMgd).close()
  }

  "getAllClients" should s"return empty response when cursors are null when regime is MGD" in {
    when(mockCsMgd.getInt(6)).thenReturn(0)
    when(mockCsMgd.getObject(7, classOf[ResultSet])).thenReturn(null)
    when(mockCsMgd.getObject(8, classOf[ResultSet])).thenReturn(null)

    val result = repository.getAllClients(MGD, "cred-123", 0, -1, 0, "ASC").futureValue
    result shouldBe Right(emptyClientList)
  }

  "hasClient" should s"return true when stored procedure returns 1 when regime is MGD" in {
    when(mockCsMgd.getInt(3)).thenReturn(1)

    val result = repository.hasClient(MGD, "cred-123", "111222333").futureValue
    result shouldBe Right(true)

    verify(mockCsMgd).setString(1, "cred-123")
    verify(mockCsMgd).setString(2, "111222333")
    verify(mockCsMgd).registerOutParameter(3, OracleTypes.INTEGER)
    verify(mockCsMgd).execute()
    verify(mockCsMgd).close()
  }

  "hasClient" should s"return false when stored procedure returns 0 when regime is MGD" in {
    when(mockCsMgd.getInt(3)).thenReturn(0)

    val result = repository.hasClient(MGD, "cred-123", "999999999").futureValue
    result shouldBe Right(false)
  }
}
