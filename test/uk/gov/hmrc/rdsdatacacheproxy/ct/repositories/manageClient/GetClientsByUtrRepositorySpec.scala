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
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.manageClient.GetClientsByUtrHelper

import java.sql.{CallableStatement, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class GetClientsByUtrRepositorySpec extends AnyFreeSpec with Matchers with BeforeAndAfter with GetClientsByUtrHelper {

  var db: Database = _
  var repo: GetClientsByUtrRepositoryImpl = _
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

    repo = new GetClientsByUtrRepositoryImpl(db)
  }

  "getClientsByUtr" - {

    "return a single client" in {
      when(mockCS.getObject(eqTo(4), eqTo(classOf[ResultSet]))).thenReturn(rs1)
      when(mockCS.getObject(eqTo(5), eqTo(classOf[ResultSet]))).thenReturn(rs2)

      when(rs1.next()).thenReturn(true, false)
      when(rs2.next()).thenReturn(true, false)

      when(rs1.getString("utr")).thenReturn("1234567890")
      when(rs1.getString("company_reg_no")).thenReturn("01234A")
      when(rs1.getString("company_name")).thenReturn("ABC Limited")
      when(rs1.getString("agent_own_ref")).thenReturn("ABC10")

      when(rs2.getString("clientnamestartingcharacter")).thenReturn("A")

      val result =
        repo.getClientsByUtr("IR2002", "CR2002", "1234567890").futureValue

      result shouldBe singleClient

      verify(mockConnection).prepareCall(
        "{call CT_CLIENT_SEARCH.get_client_by_utr(?, ?, ?, ?, ?)}"
      )

      verify(mockCS).setString(1, "IR2002")
      verify(mockCS).setString(2, "CR2002")
      verify(mockCS).setString(3, "1234567890")

      verify(mockCS).registerOutParameter(4, OracleTypes.CURSOR)
      verify(mockCS).registerOutParameter(5, OracleTypes.CURSOR)

      verify(mockCS).execute()

      verify(rs1, times(2)).next()
      verify(rs2, times(2)).next()

      verify(mockCS).close()
    }

    "return multiple clients" in {
      when(mockCS.getObject(eqTo(4), eqTo(classOf[ResultSet]))).thenReturn(rs1)
      when(mockCS.getObject(eqTo(5), eqTo(classOf[ResultSet]))).thenReturn(rs2)

      when(rs1.next()).thenReturn(true, true, false)
      when(rs2.next()).thenReturn(true, false)

      when(rs1.getString("utr")).thenReturn("2345678901", "356789012")
      when(rs1.getString("company_reg_no")).thenReturn("01234A", "6789B")
      when(rs1.getString("company_name")).thenReturn("ABC Limited", "DEF Limited")
      when(rs1.getString("agent_own_ref")).thenReturn("ABC10", "DEF10")

      when(rs2.getString("clientnamestartingcharacter")).thenReturn("A")

      val result =
        repo.getClientsByUtr("IR3003", "CR3003", "2345678901").futureValue

      result shouldBe multipleClients

      verify(mockCS).setString(1, "IR3003")
      verify(mockCS).setString(2, "CR3003")
      verify(mockCS).setString(3, "2345678901")

      verify(mockCS).registerOutParameter(4, OracleTypes.CURSOR)
      verify(mockCS).registerOutParameter(5, OracleTypes.CURSOR)

      verify(mockCS).execute()

      verify(rs1, times(3)).next()
      verify(rs2, times(2)).next()

      verify(mockCS).close()
    }

    "return multiple client name starting characters" in {
      when(mockCS.getObject(eqTo(4), eqTo(classOf[ResultSet]))).thenReturn(rs1)
      when(mockCS.getObject(eqTo(5), eqTo(classOf[ResultSet]))).thenReturn(rs2)

      when(rs1.next()).thenReturn(true, false)
      when(rs2.next()).thenReturn(true, true, true, false)

      when(rs1.getString("utr")).thenReturn("4567890123")
      when(rs1.getString("company_reg_no")).thenReturn("111213C")
      when(rs1.getString("company_name")).thenReturn("GHI Limited")
      when(rs1.getString("agent_own_ref")).thenReturn("GHI10")

      when(rs2.getString("clientnamestartingcharacter"))
        .thenReturn("G", "H", "I")

      val result =
        repo.getClientsByUtr("IR4004", "CR4004", "4567890123").futureValue

      result shouldBe multipleNameStartChars

      verify(mockCS).setString(1, "IR4004")
      verify(mockCS).setString(2, "CR4004")
      verify(mockCS).setString(3, "4567890123")

      verify(mockCS).registerOutParameter(4, OracleTypes.CURSOR)
      verify(mockCS).registerOutParameter(5, OracleTypes.CURSOR)

      verify(mockCS).execute()

      verify(rs1, times(2)).next()
      verify(rs2, times(4)).next()

      verify(mockCS).close()
    }

    "return an empty client list" in {
      when(mockCS.getObject(eqTo(4), eqTo(classOf[ResultSet]))).thenReturn(rs1)
      when(mockCS.getObject(eqTo(5), eqTo(classOf[ResultSet]))).thenReturn(rs2)

      when(rs1.next()).thenReturn(false)
      when(rs2.next()).thenReturn(false)

      val result =
        repo.getClientsByUtr("IR1001", "CR1001", "1234567890").futureValue

      result shouldBe emptyClients

      verify(mockCS).setString(1, "IR1001")
      verify(mockCS).setString(2, "CR1001")
      verify(mockCS).setString(3, "1234567890")

      verify(mockCS).registerOutParameter(4, OracleTypes.CURSOR)
      verify(mockCS).registerOutParameter(5, OracleTypes.CURSOR)

      verify(mockCS).execute()

      verify(rs1, times(1)).next()
      verify(rs2, times(1)).next()

      verify(mockCS).close()
    }

    "return None for nullable company registration number and agent own reference" in {
      when(mockCS.getObject(eqTo(4), eqTo(classOf[ResultSet]))).thenReturn(rs1)
      when(mockCS.getObject(eqTo(5), eqTo(classOf[ResultSet]))).thenReturn(rs2)

      when(rs1.next()).thenReturn(true, false)
      when(rs2.next()).thenReturn(false)

      when(rs1.getString("utr")).thenReturn("1234567890")
      when(rs1.getString("company_reg_no")).thenReturn(null)
      when(rs1.getString("company_name")).thenReturn("ABC Limited")
      when(rs1.getString("agent_own_ref")).thenReturn(null)

      val result =
        repo.getClientsByUtr("IR2002", "CR2002", "1234567890").futureValue

      result.clientList.head.companyRegNo shouldBe None
      result.clientList.head.agentOwnRef  shouldBe None

      verify(mockCS).close()
    }

    "return an exception and close the callable statement" in {
      when(mockCS.execute()).thenThrow(new RuntimeException("DB error"))

      val ex =
        repo.getClientsByUtr("IR9999", "CR9999", "9999999999").failed.futureValue

      ex.getMessage should include("DB error")

      verify(mockCS).close()
    }
  }
}
