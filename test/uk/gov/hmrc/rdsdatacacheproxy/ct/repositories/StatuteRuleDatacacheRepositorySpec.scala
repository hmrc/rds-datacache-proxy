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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, verify, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.StatuteRuleDataStub

import java.sql.{CallableStatement, Date, ResultSet, SQLException}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class StatuteRuleDatacacheRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter with StatuteRuleDataStub {

  var db: Database = _
  var repository: StatuteRuleRepositoryImpl = _
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

    repository = new StatuteRuleRepositoryImpl(db)
  }

  "getStatuteRule" should "return default record" in {
    when(mockCallableStatement.getDate(4)).thenReturn(Date.valueOf(LocalDate.parse("1999-01-18")))
    when(mockCallableStatement.getDate(5)).thenReturn(Date.valueOf(LocalDate.parse("1999-02-14")))
    when(mockCallableStatement.getInt(6)).thenReturn(27)
    when(mockCallableStatement.getBigDecimal(7)).thenReturn(java.math.BigDecimal.valueOf(100.011))
    when(mockCallableStatement.getBigDecimal(8)).thenReturn(java.math.BigDecimal.valueOf(5.75))

    val result = repository.getStatuteRule("C", LocalDate.parse("1991-01-01"), LocalDate.parse("1991-06-09")).futureValue
    result shouldBe Some(defaultRecord)

    verify(mockConnection).prepareCall("{call CT_LNP_PK.getStatuteRule(?, ?, ?, ?, ?, ?, ?, ?)}")

    verify(mockCallableStatement).setString(1, "C")
    verify(mockCallableStatement).setDate(2, Date.valueOf(LocalDate.parse("1991-01-01")))
    verify(mockCallableStatement).setDate(3, Date.valueOf(LocalDate.parse("1991-06-09")))

    verify(mockCallableStatement).registerOutParameter(4, java.sql.Types.DATE)
    verify(mockCallableStatement).registerOutParameter(5, java.sql.Types.DATE)
    verify(mockCallableStatement).registerOutParameter(6, java.sql.Types.NUMERIC)

    verify(mockCallableStatement).registerOutParameter(7, java.sql.Types.DECIMAL)
    verify(mockCallableStatement).registerOutParameter(8, java.sql.Types.DECIMAL)

    verify(mockCallableStatement).execute()
    verify(mockCallableStatement).close()
  }

  "getStatuteRule" should "return empty record" in {
    when(mockCallableStatement.getDate(4)).thenReturn(null)
    when(mockCallableStatement.getDate(5)).thenReturn(null)
    when(mockCallableStatement.getInt(6)).thenReturn(1) // looks like mandatory field
    when(mockCallableStatement.getBigDecimal(7)).thenReturn(null)
    when(mockCallableStatement.getBigDecimal(8)).thenReturn(null)

    val result = repository.getStatuteRule("C", LocalDate.parse("1991-01-01"), LocalDate.parse("1991-06-09")).futureValue
    result shouldBe Some(recordWithEmptyFields.copy(numberOfDays = Some(1)))

    verify(mockConnection).prepareCall("{call CT_LNP_PK.getStatuteRule(?, ?, ?, ?, ?, ?, ?, ?)}")

    verify(mockCallableStatement).setString(1, "C")
    verify(mockCallableStatement).setDate(2, Date.valueOf(LocalDate.parse("1991-01-01")))
    verify(mockCallableStatement).setDate(3, Date.valueOf(LocalDate.parse("1991-06-09")))

    verify(mockCallableStatement).registerOutParameter(4, java.sql.Types.DATE)
    verify(mockCallableStatement).registerOutParameter(5, java.sql.Types.DATE)
    verify(mockCallableStatement).registerOutParameter(6, java.sql.Types.NUMERIC)

    verify(mockCallableStatement).registerOutParameter(7, java.sql.Types.DECIMAL)
    verify(mockCallableStatement).registerOutParameter(8, java.sql.Types.DECIMAL)

    verify(mockCallableStatement).execute()
    verify(mockCallableStatement).close()
  }

  "getStatuteRule" should "return none record" in {
    when(mockCallableStatement.getDate(4)).thenThrow(new SQLException("no data found"))
    when(mockCallableStatement.getDate(5)).thenThrow(new SQLException("no data found"))
    when(mockCallableStatement.getInt(6)).thenThrow(new SQLException("no data found"))
    when(mockCallableStatement.getBigDecimal(7)).thenThrow(new SQLException("no data found"))
    when(mockCallableStatement.getBigDecimal(8)).thenThrow(new SQLException("no data found"))

    val result = repository.getStatuteRule("C", LocalDate.parse("1991-01-01"), LocalDate.parse("1991-06-09")).futureValue
    result shouldBe None

    verify(mockConnection).prepareCall("{call CT_LNP_PK.getStatuteRule(?, ?, ?, ?, ?, ?, ?, ?)}")

    verify(mockCallableStatement).setString(1, "C")
    verify(mockCallableStatement).setDate(2, Date.valueOf(LocalDate.parse("1991-01-01")))
    verify(mockCallableStatement).setDate(3, Date.valueOf(LocalDate.parse("1991-06-09")))

    verify(mockCallableStatement).registerOutParameter(4, java.sql.Types.DATE)
    verify(mockCallableStatement).registerOutParameter(5, java.sql.Types.DATE)
    verify(mockCallableStatement).registerOutParameter(6, java.sql.Types.NUMERIC)

    verify(mockCallableStatement).registerOutParameter(7, java.sql.Types.DECIMAL)
    verify(mockCallableStatement).registerOutParameter(8, java.sql.Types.DECIMAL)

    verify(mockCallableStatement).execute()
    verify(mockCallableStatement).close()
  }

}
