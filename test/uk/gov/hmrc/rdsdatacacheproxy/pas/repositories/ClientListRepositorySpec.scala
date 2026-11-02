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

package uk.gov.hmrc.rdsdatacacheproxy.pas.repositories

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, verify, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.db.Database

import java.sql.{CallableStatement, Connection, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class ClientListRepositorySpec extends AnyWordSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: ClientListRepository = _
  var mockConnection: Connection = _
  var mockCallableStatement: CallableStatement = _
  var mockResultSet: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[Connection])
    mockCallableStatement = mock(classOf[CallableStatement])

    when(db.withConnection(any())).thenAnswer { inv =>
      val f = inv.getArgument(0, classOf[java.sql.Connection => Any])
      f(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repository = new ClientListRepository(db, db)
  }

  "getClientListDownloadStatus" should {
    "return 1 status code when stored procedure executes successfully when serviceName is CIS" in {
      when(mockCallableStatement.getInt(4)).thenReturn(1)

      val result = repository.getClientListDownloadStatus("cred123", "CIS").futureValue
      result mustBe 1

      verify(mockConnection).prepareCall("{ call CLIENT_LIST_STATUS.GETCLIENTLISTDOWNLOADSTATUS(?, ?, ?, ?) }")
      verify(mockCallableStatement).setString(1, "cred123")
      verify(mockCallableStatement).setString(2, "CIS")
      verify(mockCallableStatement).setInt(3, 14400)
      verify(mockCallableStatement).registerOutParameter(4, oracle.jdbc.OracleTypes.INTEGER)
      verify(mockCallableStatement).execute()
      verify(mockCallableStatement).close()
    }

    "return 1 status code when stored procedure executes successfully when serviceName is PAYE" in {
      when(mockCallableStatement.getInt(4)).thenReturn(1)

      val result = repository.getClientListDownloadStatus("cred123", "PAYE").futureValue
      result mustBe 1

      verify(mockConnection).prepareCall("{ call CLIENT_LIST_STATUS.GETCLIENTLISTDOWNLOADSTATUS(?, ?, ?, ?) }")
      verify(mockCallableStatement).setString(1, "cred123")
      verify(mockCallableStatement).setString(2, "PAYE")
      verify(mockCallableStatement).setInt(3, 14400)
      verify(mockCallableStatement).registerOutParameter(4, oracle.jdbc.OracleTypes.INTEGER)
      verify(mockCallableStatement).execute()
      verify(mockCallableStatement).close()
    }

    "return 0 status code when stored procedure executes successfully" in {
      when(mockCallableStatement.getInt(4)).thenReturn(0)

      val result = repository.getClientListDownloadStatus("cred456", "cis", 7200).futureValue
      result mustBe 0

      verify(mockConnection).prepareCall("{ call CLIENT_LIST_STATUS.GETCLIENTLISTDOWNLOADSTATUS(?, ?, ?, ?) }")
      verify(mockCallableStatement).setString(1, "cred456")
      verify(mockCallableStatement).setString(2, "cis")
      verify(mockCallableStatement).setInt(3, 7200)
      verify(mockCallableStatement).registerOutParameter(4, oracle.jdbc.OracleTypes.INTEGER)
      verify(mockCallableStatement).execute()
      verify(mockCallableStatement).close()
    }

    "use custom grace period when provided" in {
      when(mockCallableStatement.getInt(4)).thenReturn(2)

      val customGracePeriod = 3600
      val result = repository.getClientListDownloadStatus("cred789", "cis", customGracePeriod).futureValue
      result mustBe 2

      verify(mockCallableStatement).setInt(3, customGracePeriod)
      verify(mockCallableStatement).close()
    }

    "return IllegalArgumentException when serviceName is invalid" in {
      val exception = intercept[java.lang.IllegalArgumentException] {
        repository.getClientListDownloadStatus("cred123", "invalidDbName").futureValue
      }

      exception.getMessage must include("Invalid DB config name: invalidDbName")
    }
  }
}
