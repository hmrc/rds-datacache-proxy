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
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseInterestDetailsSmall

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class InterestDetailsDataCacheRepositorySpec extends AnyWordSpec with Matchers with BeforeAndAfter {

  private var mgdDb: MGDDatabase = _
  private var gtrDb: GTRDatabase = _
  var repository: InterestDetailsDataCacheRepository = _
  private var mgdMockConnection: Connection = _
  private var gtrMockConnection: Connection = _
  private var mockCsMgd: CallableStatement = _
  private var mockCsGtr: CallableStatement = _
  var itemsRs: ResultSet = _
  var interestDetailsRs: ResultSet = _

  before {
    gtrDb             = mock(classOf[Database]).asInstanceOf[GTRDatabase]
    mgdDb             = mock(classOf[Database]).asInstanceOf[MGDDatabase]
    mgdMockConnection = mock(classOf[Connection])
    gtrMockConnection = mock(classOf[Connection])
    mockCsMgd         = mock(classOf[CallableStatement])
    mockCsGtr         = mock(classOf[CallableStatement])
    itemsRs           = mock(classOf[ResultSet])
    interestDetailsRs = mock(classOf[ResultSet])

    when(mgdDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mgdMockConnection)
    }

    when(gtrDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(gtrMockConnection)
    }

    when(mgdMockConnection.prepareCall("{ call MGD_LNP_PK.getMGDInterestDetails(?, ?, ?, ?, ?, ?, ?, ?) }")).thenReturn(mockCsMgd)
    when(gtrMockConnection.prepareCall("{ call GTR_LNP_PK.getGTRInterestDetails(?, ?, ?, ?, ?, ?, ?, ?) }")).thenReturn(mockCsGtr)

    repository = new InterestDetailsDataCacheRepository(
      mgdDb = mgdDb,
      gtrDb = gtrDb
    )
  }

  "getInterestDetails" should {
    "return InterestDetails for MGD regime when stored procedure returns data" in {

      val regNumber = "XWM12345678901"

      when(mockCsMgd.getDate(4)).thenReturn(Date.valueOf("2013-03-01"))
      when(mockCsMgd.getDate(5)).thenReturn(Date.valueOf("2014-03-11"))
      when(mockCsMgd.getBigDecimal(6)).thenReturn(java.math.BigDecimal.valueOf(-800.00))
      when(mockCsMgd.getObject(7)).thenReturn(1)
      when(mockCsMgd.getObject(8)).thenReturn(interestDetailsRs)

      when(interestDetailsRs.next()).thenReturn(true, false)

      when(interestDetailsRs.getInt("p_desc_code")).thenReturn(2740)
      when(interestDetailsRs.getObject("p_amount")).thenReturn(java.math.BigDecimal.valueOf(-800.00))
      when(interestDetailsRs.getString("p_interest_id")).thenReturn("SAFE-CHG-00003")
      when(interestDetailsRs.getDate("p_period_start")).thenReturn(Date.valueOf("2014-01-01"))
      when(interestDetailsRs.getDate("p_period_end")).thenReturn(Date.valueOf("2014-03-31"))

      val result = repository.getInterestDetails(Regime.MGD, regNumber, 1, 10).futureValue

      result            shouldBe validResponseInterestDetailsSmall
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

      verify(interestDetailsRs, times(2)).next()
      verify(interestDetailsRs).getInt("p_desc_code")
      verify(interestDetailsRs).getObject("p_amount")
      verify(interestDetailsRs).getString("p_interest_id")
      verify(interestDetailsRs).getDate("p_period_start")
      verify(interestDetailsRs).getDate("p_period_end")

      verify(interestDetailsRs).close()
      verify(mockCsMgd).close()
    }

    "return empty InterestDetails when regNumber is null" in {
      val regNumber: Null = null
      when(mockCsMgd.getDate(2)).thenReturn(null)
      val result = repository.getInterestDetails(Regime.MGD, regNumber, 1, 10).futureValue

      result       shouldBe InterestDetails(None, None, BigDecimal(0), 0, List())
      result.items shouldBe empty

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
      verify(interestDetailsRs, times(0)).next()
      verify(interestDetailsRs, times(0)).getString("p_interest_id")
      verify(interestDetailsRs, times(0)).close()
      verify(mockCsMgd).close()
    }

    "return Empty List when InterestDetails result set is empty" in {
      val regNumber = "XWM00000001770"
      when(mockCsMgd.getDate(4)).thenReturn(Date.valueOf("2016-2-29"))
      when(mockCsMgd.getDate(5)).thenReturn(Date.valueOf("2017-6-15"))
      when(mockCsMgd.getObject(6)).thenReturn(BigDecimal.valueOf(-301.56))
      when(mockCsMgd.getObject(7)).thenReturn(0)
      when(interestDetailsRs.next()).thenReturn(false)

      val result = repository.getInterestDetails(Regime.MGD, regNumber, 1, 10).futureValue

      result shouldBe InterestDetails(Some(LocalDate.of(2016, 2, 29)), Some(LocalDate.of(2017, 6, 15)), -301.56, 0, List())

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
      verify(interestDetailsRs, times(0)).next()
      verify(interestDetailsRs, times(0)).getString("p_interest_id")
      verify(interestDetailsRs, times(0)).close()
      verify(mockCsMgd).close()
    }

    Regime.values.toList.filterNot(_ == Regime.MGD).foreach { regime =>
      s"return InterestDetails for $regime regime when stored procedure returns data" in {

        val regNumber = "XWM12345678901"

        when(mockCsGtr.getDate(4)).thenReturn(Date.valueOf("2013-03-01"))
        when(mockCsGtr.getDate(5)).thenReturn(Date.valueOf("2014-03-11"))
        when(mockCsGtr.getBigDecimal(6)).thenReturn(java.math.BigDecimal.valueOf(-800.00))
        when(mockCsGtr.getObject(7)).thenReturn(1)
        when(mockCsGtr.getObject(8)).thenReturn(interestDetailsRs)

        when(interestDetailsRs.next()).thenReturn(true, false)
        when(interestDetailsRs.getInt("p_desc_code")).thenReturn(2740)
        when(interestDetailsRs.getObject("p_amount")).thenReturn(java.math.BigDecimal.valueOf(-800.00))
        when(interestDetailsRs.getString("p_interest_id")).thenReturn("SAFE-CHG-00003")
        when(interestDetailsRs.getDate("p_period_start")).thenReturn(Date.valueOf("2014-01-01"))
        when(interestDetailsRs.getDate("p_period_end")).thenReturn(Date.valueOf("2014-03-31"))

        val result = repository.getInterestDetails(regime, regNumber, 1, 10).futureValue

        result            shouldBe validResponseInterestDetailsSmall
        result.items.size shouldBe 1

        verify(mockCsGtr).setString(1, regNumber)
        verify(mockCsGtr).setInt(2, 1)
        verify(mockCsGtr).setInt(3, 10)

        verify(mockCsGtr).registerOutParameter(4, oracle.jdbc.OracleTypes.DATE)
        verify(mockCsGtr).registerOutParameter(5, oracle.jdbc.OracleTypes.DATE)
        verify(mockCsGtr).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
        verify(mockCsGtr).registerOutParameter(7, oracle.jdbc.OracleTypes.NUMERIC)
        verify(mockCsGtr).registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR)

        verify(mockCsGtr).execute()

        verify(mockCsGtr).getDate(4)
        verify(mockCsGtr).getDate(5)
        verify(mockCsGtr).getBigDecimal(6)
        verify(mockCsGtr).getObject(7)
        verify(mockCsGtr).getObject(8)

        verify(interestDetailsRs, times(2)).next()
        verify(interestDetailsRs).getInt("p_desc_code")
        verify(interestDetailsRs).getObject("p_amount")
        verify(interestDetailsRs).getString("p_interest_id")
        verify(interestDetailsRs).getDate("p_period_start")
        verify(interestDetailsRs).getDate("p_period_end")

        verify(interestDetailsRs).close()
        verify(mockCsGtr).close()
      }
    }

  }
}
