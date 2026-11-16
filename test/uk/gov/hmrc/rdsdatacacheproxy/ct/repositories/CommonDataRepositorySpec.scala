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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.CommonData

import java.sql.{CallableStatement, Connection}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class CommonDataRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var repository: CommonDataRepositoryImpl = _
  var db: Database = _
  var mockConnection: Connection = _
  var mockCallableStatement: CallableStatement = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[Connection])
    mockCallableStatement = mock(classOf[CallableStatement])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repository = new CommonDataRepositoryImpl(db)
  }

  "getCommonData" should "return the date from the stored procedure" in {

    val expectedDate = LocalDate.of(2026, 9, 15)

    when(mockCallableStatement.getDate(1))
      .thenReturn(java.sql.Date.valueOf(expectedDate))

    val result = repository.getCommonData().futureValue

    result shouldBe CommonData(expectedDate)

    verify(mockCallableStatement).registerOutParameter(1, OracleTypes.DATE)
    verify(mockCallableStatement).execute()
    verify(mockCallableStatement).getDate(1)
    verify(mockCallableStatement).close()
    verify(mockConnection).prepareCall("{call CT_DC_PK.getCommonData(?)}")
  }

  "getCommonData" should "throw exception and close the connection when exception is returned from DB" in {

    when(mockCallableStatement.execute()).thenThrow(new RuntimeException("Boom"))

    val ex = repository.getCommonData().failed.futureValue

    ex.getMessage should include("Boom")

    verify(mockCallableStatement).close()
  }
}
