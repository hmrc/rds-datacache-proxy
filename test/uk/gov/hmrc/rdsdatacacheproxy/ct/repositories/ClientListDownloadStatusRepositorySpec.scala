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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories

import oracle.jdbc.OracleTypes
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, verify, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.ClientDownloadStatus

import java.sql.{CallableStatement, Connection, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class ClientListDownloadStatusRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var repository: ClientListDownloadStatusRepositoryImpl = _
  var db: Database = _
  var mockConnection: Connection = _
  var mockCallableStatement: CallableStatement = _
  var mockResultSet: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[Connection])
    mockCallableStatement = mock(classOf[CallableStatement])
    mockResultSet         = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repository = new ClientListDownloadStatusRepositoryImpl(db)

  }

  "getClientListDownloadStatus" should "return the status integer from the stored procedure" in {
    when(mockCallableStatement.getInt(4)).thenReturn(1)

    val result = repository.getClientListDownloadStatus("1", "CT", 14400).futureValue

    result shouldBe ClientDownloadStatus(1)
    verify(mockCallableStatement).setString(1, "1")
    verify(mockCallableStatement).setString(2, "CT")
    verify(mockCallableStatement).setInt(3, 14400)
    verify(mockCallableStatement).registerOutParameter(4, OracleTypes.INTEGER)
    verify(mockCallableStatement).execute()
    verify(mockCallableStatement).close()
  }

  "getClientListDownloadStatus" should "throw exception and close the connection when exception is returned from DB" in {

    when(mockCallableStatement.execute()).thenThrow(new RuntimeException("Boom"))

    val ex = repository.getClientListDownloadStatus("1", "CT", 14400).failed.futureValue

    ex.getMessage should include("Boom")

    verify(mockCallableStatement).close()
  }

}
