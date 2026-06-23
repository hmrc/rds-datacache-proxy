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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.*
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseSubmittedReturnSingle

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class SubmittedReturnSingleDataCacheRepositorySpec extends AnyWordSpec with Matchers with BeforeAndAfter {

  private var mgdDb: MGDDatabase = _
  private var gtrDb: GTRDatabase = _
  var repository: SubmittedReturnSingleDataCacheRepository = _
  private var mgdMockConnection: Connection = _
  private var gtrMockConnection: Connection = _
  private var mockCsMgd: CallableStatement = _
  private var mockCsGtr: CallableStatement = _
  var SubmittedReturnSingleRs: ResultSet = _

  before {
    gtrDb                   = mock(classOf[Database]).asInstanceOf[GTRDatabase]
    mgdDb                   = mock(classOf[Database]).asInstanceOf[MGDDatabase]
    mgdMockConnection       = mock(classOf[Connection])
    gtrMockConnection       = mock(classOf[Connection])
    mockCsMgd               = mock(classOf[CallableStatement])
    mockCsGtr               = mock(classOf[CallableStatement])
    SubmittedReturnSingleRs = mock(classOf[ResultSet])

    when(mgdDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mgdMockConnection)
    }

    when(mgdMockConnection.prepareCall("{ call MGD_DC_RTN_PCK.GET_SINGLE_RETURN_V2(?, ?, ?) }")).thenReturn(mockCsMgd)

    repository = new SubmittedReturnSingleDataCacheRepository(
      mgdDb = mgdDb,
      gtrDb = gtrDb
    )
  }

  "getSubmittedReturnSingle" should {
    "return SubmittedReturnSingle when stored procedure returns data" in {

      val regNumber = "XWM12345678901"

      when(mockCsMgd.getObject(3)).thenReturn(SubmittedReturnSingleRs)

      when(SubmittedReturnSingleRs.next()).thenReturn(true, false)

      when(SubmittedReturnSingleRs.getInt("consec_no")).thenReturn(23)
      when(SubmittedReturnSingleRs.getString("mgd_period")).thenReturn("01/01/2025 - 30/03/2025")
      when(SubmittedReturnSingleRs.getDate("submitted_date")).thenReturn(Date.valueOf("2025-05-01"))
      when(SubmittedReturnSingleRs.getString("ack_ref")).thenReturn("123456789012345")
      when(SubmittedReturnSingleRs.getInt("no_of_machines_avail")).thenReturn(5)
      when(SubmittedReturnSingleRs.getObject("net_takings_higher_rate")).thenReturn(BigDecimal.valueOf(100.10))
      when(SubmittedReturnSingleRs.getObject("net_takings_std_rate")).thenReturn(BigDecimal.valueOf(20.00))
      when(SubmittedReturnSingleRs.getObject("net_takings_lower_rate")).thenReturn(BigDecimal.valueOf(200.20))
      when(SubmittedReturnSingleRs.getObject("total_due_higher_rate")).thenReturn(BigDecimal.valueOf(10.00))
      when(SubmittedReturnSingleRs.getObject("total_due_std_rate")).thenReturn(BigDecimal.valueOf(300.30))
      when(SubmittedReturnSingleRs.getObject("total_due_lower_rate")).thenReturn(BigDecimal.valueOf(5.00))
      when(SubmittedReturnSingleRs.getObject("duty_payable")).thenReturn(BigDecimal.valueOf(35.00))
      when(SubmittedReturnSingleRs.getObject("under_declared_duty")).thenReturn(BigDecimal.valueOf(40.00))
      when(SubmittedReturnSingleRs.getObject("previous_return_amount")).thenReturn(BigDecimal.valueOf(100.00))
      when(SubmittedReturnSingleRs.getObject("neg_amt_carry_forward")).thenReturn(BigDecimal.valueOf(99.99))
      when(SubmittedReturnSingleRs.getObject("total_net_duty_payable")).thenReturn(BigDecimal.valueOf(75.49))

      val result = repository.getSubmittedReturnSingle(regNumber, 23).futureValue

      result shouldBe validResponseSubmittedReturnSingle

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setInt(2, 23)
      verify(mockCsMgd).registerOutParameter(3, oracle.jdbc.OracleTypes.CURSOR)
      verify(mockCsMgd).execute()

      verify(mockCsMgd).getObject(3)

      verify(SubmittedReturnSingleRs, times(1)).next()
      verify(SubmittedReturnSingleRs).getInt("consec_no")
      verify(SubmittedReturnSingleRs).getString("mgd_period")
      verify(SubmittedReturnSingleRs).getDate("submitted_date")
      verify(SubmittedReturnSingleRs).getString("ack_ref")
      verify(SubmittedReturnSingleRs).getInt("no_of_machines_avail")
      verify(SubmittedReturnSingleRs).getBigDecimal("net_takings_higher_rate")
      verify(SubmittedReturnSingleRs).getBigDecimal("net_takings_std_rate")
      verify(SubmittedReturnSingleRs).getBigDecimal("net_takings_lower_rate")
      verify(SubmittedReturnSingleRs).getBigDecimal("total_due_higher_rate")
      verify(SubmittedReturnSingleRs).getBigDecimal("total_due_std_rate")
      verify(SubmittedReturnSingleRs).getBigDecimal("total_due_lower_rate")
      verify(SubmittedReturnSingleRs).getBigDecimal("duty_payable")
      verify(SubmittedReturnSingleRs).getBigDecimal("under_declared_duty")
      verify(SubmittedReturnSingleRs).getBigDecimal("previous_return_amount")
      verify(SubmittedReturnSingleRs).getBigDecimal("neg_amt_carry_forward")
      verify(SubmittedReturnSingleRs).getBigDecimal("total_net_duty_payable")

      verify(SubmittedReturnSingleRs).close()
      verify(mockCsMgd).close()
    }

    "return empty SubmittedReturnSingle when regNumber is null" in {
      val regNumber: Null = null
      when(mockCsMgd.getObject(3)).thenReturn(null)

      val ex = intercept[RuntimeException] {
        repository.getSubmittedReturnSingle(regNumber, 3).futureValue
      }
      ex.getMessage should include("Null cursor returned for regNumber=")

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setInt(2, 3)
      verify(mockCsMgd).registerOutParameter(3, oracle.jdbc.OracleTypes.CURSOR)
      verify(mockCsMgd).execute()

      verify(mockCsMgd).getObject(3)
      verify(SubmittedReturnSingleRs, times(0)).next()
      verify(SubmittedReturnSingleRs, times(0)).getDate("submitted_date")
      verify(SubmittedReturnSingleRs, times(0)).close()
      verify(mockCsMgd).close()
    }

    "return Empty List when SubmittedReturnSingle result set is empty" in {
      val regNumber = "XWM00000001770"
      when(SubmittedReturnSingleRs.next()).thenReturn(false)

      val ex = intercept[RuntimeException] {
        repository.getSubmittedReturnSingle(regNumber, 3).futureValue
      }
      ex.getMessage should include("Null cursor returned for regNumber=")

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setInt(2, 3)
      verify(mockCsMgd).registerOutParameter(3, oracle.jdbc.OracleTypes.CURSOR)
      verify(mockCsMgd).execute()

      verify(mockCsMgd).getObject(3)
      verify(SubmittedReturnSingleRs, times(0)).next()
      verify(SubmittedReturnSingleRs, times(0)).getDate("submitted_date")
      verify(SubmittedReturnSingleRs, times(0)).close()
      verify(mockCsMgd).close()
    }

    "return 'Unable to create SubmittedReturnSingle' when stored procedure returns bad data" in {

      val regNumber = "XWM12345678901"

      when(mockCsMgd.getObject(3)).thenReturn(SubmittedReturnSingleRs)

      when(SubmittedReturnSingleRs.next()).thenReturn(true, false)

      val ex = intercept[RuntimeException] {
        repository.getSubmittedReturnSingle(regNumber, 3).futureValue
      }
      ex.getMessage should include("Unable to create SubmittedReturnSingle for regNumber=")

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setInt(2, 3)
      verify(mockCsMgd).registerOutParameter(3, oracle.jdbc.OracleTypes.CURSOR)
      verify(mockCsMgd).execute()

      verify(mockCsMgd).getObject(3)
      verify(SubmittedReturnSingleRs, times(1)).next()
      verify(SubmittedReturnSingleRs, times(1)).getInt("consec_no")
      verify(SubmittedReturnSingleRs, times(0)).getDate("submitted_date")
      verify(SubmittedReturnSingleRs, times(1)).close()
      verify(mockCsMgd).close()
    }
  }
}
