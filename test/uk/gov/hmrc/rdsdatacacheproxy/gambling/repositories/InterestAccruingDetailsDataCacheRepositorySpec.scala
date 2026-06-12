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
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseInterestAccruingDetailsSmall

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class InterestAccruingDetailsDataCacheRepositorySpec extends AnyWordSpec with Matchers with BeforeAndAfter {

  private val gtrDb: GTRDatabase = mock(classOf[Database]).asInstanceOf[GTRDatabase]
  private val mgdDb: MGDDatabase = mock(classOf[Database]).asInstanceOf[MGDDatabase]
  private val mgdMockConnection: Connection = mock(classOf[Connection])
  private val gtrMockConnection: Connection = mock(classOf[Connection])
  private val mockCsMgd: CallableStatement = mock(classOf[CallableStatement])
  private val mockCsGtr: CallableStatement = mock(classOf[CallableStatement])
  private val amountDeclaredRs: ResultSet = mock(classOf[ResultSet])
  private val interestAccruingRs: ResultSet = mock(classOf[ResultSet])

  private val repository: InterestAccruingDetailsDataCacheRepository = new InterestAccruingDetailsDataCacheRepository(
    mgdDb = mgdDb,
    gtrDb = gtrDb
  )

  before {
    Mockito.reset(mgdDb, gtrDb, mgdMockConnection, gtrMockConnection, mockCsMgd, mockCsGtr, amountDeclaredRs, interestAccruingRs)

    when(mgdDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mgdMockConnection)
    }

    when(gtrDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(gtrMockConnection)
    }

    when(mgdMockConnection.prepareCall("{ call MGD_LNP_PK.getMGDInterestAccruingDetails(?, ?, ?, ?, ?, ?, ?, ?) }"))
      .thenReturn(mockCsMgd)

    when(gtrMockConnection.prepareCall("{ call GTR_LNP_PK.getGTRInterestAccruingDetails(?, ?, ?, ?, ?, ?, ?, ?) }"))
      .thenReturn(mockCsGtr)
  }

  "getInterestAccruingDetails" should {

    "return InterestAccruingDetails for MGD/GTR regime when stored procedure returns data" in {

      val regNumber = "XWM12345678901"

      when(mockCsMgd.getDate(4)).thenReturn(Date.valueOf("2013-01-01"))
      when(mockCsMgd.getDate(5)).thenReturn(Date.valueOf("2014-11-03"))
      when(mockCsMgd.getBigDecimal(6)).thenReturn(java.math.BigDecimal.valueOf(3000.00))
      when(mockCsMgd.getObject(7)).thenReturn(java.math.BigDecimal.valueOf(1))
      when(mockCsMgd.getObject(8)).thenReturn(interestAccruingRs)

      when(interestAccruingRs.next()).thenReturn(true, false)

      when(interestAccruingRs.getInt("p_desc_code")).thenReturn(1)
      when(interestAccruingRs.getBigDecimal("p_amount")).thenReturn(java.math.BigDecimal.valueOf(3000.00))
      when(interestAccruingRs.getString("p_interest_id")).thenReturn("SAFE-CHG-00001")
      when(interestAccruingRs.getDate("p_period_start")).thenReturn(Date.valueOf("2014-10-01"))
      when(interestAccruingRs.getDate("p_period_end")).thenReturn(Date.valueOf("2014-10-31"))

      val result = repository.getInterestAccruingDetails(Regime.MGD, regNumber, 1, 10).futureValue

      result            shouldBe validResponseInterestAccruingDetailsSmall
      result.items.size shouldBe 1

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setInt(2, 1)
      verify(mockCsMgd).setInt(3, 10)

      verify(mockCsMgd).registerOutParameter(4, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
      verify(mockCsMgd).registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR)

      verify(mockCsMgd).execute()

      verify(mockCsMgd).getDate(4)
      verify(mockCsMgd).getDate(5)
      verify(mockCsMgd).getBigDecimal(6)
      verify(mockCsMgd).getObject(7)
      verify(mockCsMgd).getObject(8)

      verify(interestAccruingRs, times(2)).next()
      verify(interestAccruingRs).getInt("p_desc_code")
      verify(interestAccruingRs).getBigDecimal("p_amount")
      verify(interestAccruingRs).getString("p_interest_id")
      verify(interestAccruingRs).getDate("p_period_start")
      verify(interestAccruingRs).getDate("p_period_end")

      verify(interestAccruingRs).close()
      verify(mockCsMgd).close()
    }

    "return empty InterestAccruingDetails when regNumber is null" in {
      val regNumber: Null = null

      when(mockCsMgd.getBigDecimal(6)).thenReturn(java.math.BigDecimal.ZERO)
      when(mockCsMgd.getObject(7)).thenReturn(java.math.BigDecimal.valueOf(0))
      when(mockCsMgd.getObject(8)).thenReturn(interestAccruingRs)
      when(interestAccruingRs.next()).thenReturn(false)

      val result = repository.getInterestAccruingDetails(Regime.MGD, regNumber, 1, 10).futureValue

      result       shouldBe InterestAccruingDetails(None, None, BigDecimal(0), 0, List())
      result.items shouldBe empty

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setInt(2, 1)
      verify(mockCsMgd).setInt(3, 10)
      verify(mockCsMgd).execute()
      verify(mockCsMgd).close()
    }

    "return Empty List when InterestAccruingDetails result set is empty" in {
      val regNumber = "XWM00000001770"

      when(mockCsMgd.getDate(4)).thenReturn(Date.valueOf("2016-2-29"))
      when(mockCsMgd.getDate(5)).thenReturn(Date.valueOf("2017-6-15"))
      when(mockCsMgd.getBigDecimal(6)).thenReturn(java.math.BigDecimal.ZERO)
      when(mockCsMgd.getInt(7)).thenReturn(0)
      when(mockCsMgd.getObject(8)).thenReturn(interestAccruingRs)
      when(interestAccruingRs.next()).thenReturn(false)

      val result = repository.getInterestAccruingDetails(Regime.MGD, regNumber, 1, 10).futureValue

      result shouldBe InterestAccruingDetails(
        Some(LocalDate.of(2016, 2, 29)),
        Some(LocalDate.of(2017, 6, 15)),
        0,
        0,
        List()
      )

      verify(mockCsMgd).execute()
      verify(mockCsMgd).close()
    }

    Regime.values.toList.filterNot(_ == Regime.MGD).foreach { regime =>
      s"return InterestAccruingDetails for $regime regime when stored procedure returns data" in {

        val regNumber = "XWM12345678901"

        when(mockCsGtr.getDate(4)).thenReturn(Date.valueOf("2013-01-01"))
        when(mockCsGtr.getDate(5)).thenReturn(Date.valueOf("2014-11-03"))
        when(mockCsGtr.getBigDecimal(6)).thenReturn(java.math.BigDecimal.valueOf(3000.00))
        when(mockCsGtr.getObject(7)).thenReturn(java.math.BigDecimal.valueOf(1))
        when(mockCsGtr.getObject(8)).thenReturn(interestAccruingRs)

        when(interestAccruingRs.next()).thenReturn(true, false)
        when(interestAccruingRs.getInt("p_desc_code")).thenReturn(1)
        when(interestAccruingRs.getBigDecimal("p_amount")).thenReturn(java.math.BigDecimal.valueOf(3000.00))
        when(interestAccruingRs.getString("p_interest_id")).thenReturn("SAFE-CHG-00001")
        when(interestAccruingRs.getDate("p_period_start")).thenReturn(Date.valueOf("2014-10-01"))
        when(interestAccruingRs.getDate("p_period_end")).thenReturn(Date.valueOf("2014-10-31"))

        val result = repository.getInterestAccruingDetails(regime, regNumber, 1, 10).futureValue

        result shouldBe validResponseInterestAccruingDetailsSmall

        verify(mockCsGtr).execute()
        verify(mockCsGtr).close()
      }
    }
  }
}
