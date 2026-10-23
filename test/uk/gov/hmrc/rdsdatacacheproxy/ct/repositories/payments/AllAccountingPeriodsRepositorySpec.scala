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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.payments

import oracle.jdbc.OracleTypes
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.payments.AllAccountingPeriodsHelper

import java.sql.{CallableStatement, Date, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class AllAccountingPeriodsRepositorySpec extends AnyFreeSpec with Matchers with BeforeAndAfter with AllAccountingPeriodsHelper {

  var db: Database = _
  var repo: AllAccountingPeriodsRepositoryImpl = _
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

    repo = new AllAccountingPeriodsRepositoryImpl(db)
  }

  "getAccountingPeriodList" - {
    "return all accounting periods" in {
      when(mockCS.getObject(eqTo(2), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenReturn(true, false)

      when(rs.getBigDecimal("accounting_period")).thenReturn(scala.math.BigDecimal(10).bigDecimal)
      when(rs.getDate("ap_end_date")).thenReturn(Date.valueOf("2025-07-24"))
      when(rs.getString("tax_charge_present")).thenReturn("true")
      when(rs.getBigDecimal("penalty_total")).thenReturn(scala.math.BigDecimal(10).bigDecimal)

      val result = repo.getAccountingPeriodList(2L).futureValue
      result shouldBe allAccountingPeriods

      verify(mockConnection).prepareCall("{call CT_DC_PK.getAccountingPeriodList(?, ?)}")

      verify(mockCS).setLong(1, 2L)
      verify(mockCS).registerOutParameter(2, OracleTypes.CURSOR)

      verify(mockCS).execute()

      verify(rs, times(2)).next()

      verify(mockCS).close()
    }

    "return multiple items of all accounting periods" in {
      when(mockCS.getObject(eqTo(2), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenReturn(true, true, false)

      when(rs.getBigDecimal("accounting_period")).thenReturn(scala.math.BigDecimal(20).bigDecimal, scala.math.BigDecimal(30).bigDecimal)
      when(rs.getDate("ap_end_date")).thenReturn(Date.valueOf("2025-08-24"), Date.valueOf("2025-08-24"))
      when(rs.getString("tax_charge_present")).thenReturn("true", "false")
      when(rs.getBigDecimal("penalty_total")).thenReturn(scala.math.BigDecimal(20).bigDecimal, scala.math.BigDecimal(30).bigDecimal)

      val result = repo.getAccountingPeriodList(2L).futureValue
      result shouldBe multipleAllAccountingPeriods

      verify(mockConnection).prepareCall("{call CT_DC_PK.getAccountingPeriodList(?, ?)}")

      verify(mockCS).setLong(1, 2L)
      verify(mockCS).registerOutParameter(2, OracleTypes.CURSOR)

      verify(mockCS).execute()

      verify(rs, times(3)).next()

      verify(mockCS).close()
    }

    "return empty list of all accounting periods" in {
      when(mockCS.getObject(eqTo(2), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenReturn(false)

      val result = repo.getAccountingPeriodList(2L).futureValue
      result shouldBe emptyAllAccountingPeriods

      verify(mockConnection).prepareCall("{call CT_DC_PK.getAccountingPeriodList(?, ?)}")

      verify(mockCS).setLong(1, 2L)
      verify(mockCS).registerOutParameter(2, OracleTypes.CURSOR)

      verify(mockCS).execute()

      verify(rs, times(1)).next()

      verify(mockCS).close()
    }

    "return an exception and close the connection" in {
      when(mockCS.execute()).thenThrow(new RuntimeException("DB error"))

      val ex = repo.getAccountingPeriodList(2L).failed.futureValue
      ex.getMessage should include("DB error")

      verify(mockCS).close()
    }
  }
}
