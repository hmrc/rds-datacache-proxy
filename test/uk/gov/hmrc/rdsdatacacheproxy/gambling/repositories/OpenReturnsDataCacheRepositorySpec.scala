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
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.*
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseOpenReturnPeriodsSmall

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class OpenReturnsDataCacheRepositorySpec extends AnyWordSpec with Matchers with BeforeAndAfter {

  private val mgdDb: MGDDatabase = mock(classOf[Database]).asInstanceOf[MGDDatabase]
  private val gtrDb: GTRDatabase = mock(classOf[Database]).asInstanceOf[GTRDatabase]
  private val mgdMockConnection: Connection = mock(classOf[Connection])
  private val mockCsMgd: CallableStatement = mock(classOf[CallableStatement])
  val openReturnPeriodsRs: ResultSet = mock(classOf[ResultSet])
  val repository: OpenReturnsDataCacheRepository = new OpenReturnsDataCacheRepository(
    mgdDb = mgdDb,
    gtrDb = gtrDb
  )

  before {
    Mockito.reset(mgdDb, gtrDb, mgdMockConnection, mockCsMgd, openReturnPeriodsRs)

    when(mgdDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mgdMockConnection)
    }

    when(mgdMockConnection.prepareCall("{ call MGD_DC_RTN_PCK.GET_OPEN_PERIODS(?, ?, ?, ?) }")).thenReturn(mockCsMgd)
  }

  "getOpenReturnPeriods" should {
    "return OpenReturnPeriods when stored procedure returns data" in {

      val regNumber = "XWM12345678901"

      when(mockCsMgd.getObject(4)).thenReturn(openReturnPeriodsRs)

      when(openReturnPeriodsRs.next()).thenReturn(true, false)

      when(openReturnPeriodsRs.getInt("consec_no")).thenReturn(12345)
      when(openReturnPeriodsRs.getString("mgd_period")).thenReturn("01/07/2025 - 30/09/2025")
      when(openReturnPeriodsRs.getDate("due_date")).thenReturn(Date.valueOf("2025-10-30"))
      when(openReturnPeriodsRs.getInt("status")).thenReturn(1)

      val result = repository.getOpenReturnPeriods(Regime.MGD, regNumber, 3, "ASC").futureValue

      result                  shouldBe validResponseOpenReturnPeriodsSmall
      result.openPeriods.size shouldBe 1

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setString(2, "ASC")
      verify(mockCsMgd).setInt(3, 3)
      verify(mockCsMgd).registerOutParameter(4, oracle.jdbc.OracleTypes.CURSOR)
      verify(mockCsMgd).execute()

      verify(mockCsMgd).getObject(4)

      verify(openReturnPeriodsRs, times(2)).next()
      verify(openReturnPeriodsRs).getInt("consec_no")
      verify(openReturnPeriodsRs).getString("mgd_period")
      verify(openReturnPeriodsRs).getDate("due_date")
      verify(openReturnPeriodsRs).getInt("status")

      verify(openReturnPeriodsRs).close()
      verify(mockCsMgd).close()
    }

    "return empty OpenReturnPeriods when regNumber is null" in {
      val regNumber: Null = null
      when(mockCsMgd.getObject(4)).thenReturn(null)
      val result = repository.getOpenReturnPeriods(Regime.MGD, regNumber, 3, "ASC").futureValue

      result             shouldBe OpenReturnPeriods(List())
      result.openPeriods shouldBe empty

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setString(2, "ASC")
      verify(mockCsMgd).setInt(3, 3)
      verify(mockCsMgd).registerOutParameter(4, oracle.jdbc.OracleTypes.CURSOR)
      verify(mockCsMgd).execute()

      verify(mockCsMgd).getObject(4)
      verify(openReturnPeriodsRs, times(0)).next()
      verify(openReturnPeriodsRs, times(0)).getDate("due_date")
      verify(openReturnPeriodsRs, times(0)).close()
      verify(mockCsMgd).close()
    }

    "return Empty List when OpenReturnPeriods result set is empty" in {
      val regNumber = "XWM00000001770"
      when(openReturnPeriodsRs.next()).thenReturn(false)

      val result = repository.getOpenReturnPeriods(Regime.MGD, regNumber, 3, "ASC").futureValue

      result shouldBe OpenReturnPeriods(List())

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setString(2, "ASC")
      verify(mockCsMgd).setInt(3, 3)
      verify(mockCsMgd).registerOutParameter(4, oracle.jdbc.OracleTypes.CURSOR)
      verify(mockCsMgd).execute()

      verify(mockCsMgd).getObject(4)
      verify(openReturnPeriodsRs, times(0)).next()
      verify(openReturnPeriodsRs, times(0)).getDate("due_date")
      verify(openReturnPeriodsRs, times(0)).close()
      verify(mockCsMgd).close()
    }

    "throw an error when regime is not MGD and do not execute the stored procedure" in {
      val regNumber = "XWM12345678901"

      val exception = repository.getOpenReturnPeriods(Regime.GBD, regNumber, 3, "ASC").failed.futureValue

      exception            shouldBe a[RuntimeException]
      exception.getMessage shouldBe "Regime GBD is not supported for getOpenReturnPeriods"

      verify(mgdMockConnection, never()).prepareCall(any())
      verifyNoInteractions(mockCsMgd)
      verifyNoInteractions(gtrDb)
    }
  }
}
