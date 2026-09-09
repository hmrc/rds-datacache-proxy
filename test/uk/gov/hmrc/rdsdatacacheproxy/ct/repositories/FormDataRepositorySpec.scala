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

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, verify, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.FormDataStub

import java.sql.{CallableStatement, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class FormDataRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter with FormDataStub {

  var db: Database = _
  var repository: FormDataRepositoryImpl = _
  var mockConnection: java.sql.Connection = _
  var mockCallableStatement: CallableStatement = _
  var rs: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[java.sql.Connection])
    mockCallableStatement = mock(classOf[CallableStatement])

    rs = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repository = new FormDataRepositoryImpl(db)
  }

  "getData" should "return some record" in {
    when(mockCallableStatement.getObject(eqTo(6), eqTo(classOf[ResultSet]))).thenReturn(rs)
    when(rs.next()).thenReturn(true, false)

    when(rs.getString("form_type")).thenReturn("A")
    when(rs.getString("xml_data")).thenReturn("xml_data")

    val result = repository.getData(taxRef = 1L, accPeriod = 1L, LocalDate.parse("2006-01-01"), LocalDate.parse("2006-12-31")).futureValue

    result shouldBe firstDataItem

    verify(mockConnection).prepareCall("{call UDAS_CT_DC.getLatestCTReturn(?, ?, ?, ?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, 1L)
    verify(mockCallableStatement).setLong(2, 1L)

    verify(mockCallableStatement).registerOutParameter(5, oracle.jdbc.OracleTypes.CLOB)
    verify(mockCallableStatement).registerOutParameter(6, oracle.jdbc.OracleTypes.CURSOR)

    verify(mockCallableStatement).execute()

    verify(mockCallableStatement).close()
  }

  "getData" should "return empty record" in {
    when(mockCallableStatement.getObject(eqTo(6), eqTo(classOf[ResultSet]))).thenReturn(rs)
    when(rs.next()).thenReturn(false)

    val result = repository.getData(taxRef = 9L, accPeriod = 1L, LocalDate.parse("2006-01-01"), LocalDate.parse("2006-12-31")).futureValue

    result shouldBe fullyEmptyDataItem

    verify(mockConnection).prepareCall("{call UDAS_CT_DC.getLatestCTReturn(?, ?, ?, ?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, 9L)
    verify(mockCallableStatement).setLong(2, 1L)

    verify(mockCallableStatement).registerOutParameter(5, oracle.jdbc.OracleTypes.CLOB)
    verify(mockCallableStatement).registerOutParameter(6, oracle.jdbc.OracleTypes.CURSOR)

    verify(mockCallableStatement).execute()

    verify(mockCallableStatement).close()
  }

}
