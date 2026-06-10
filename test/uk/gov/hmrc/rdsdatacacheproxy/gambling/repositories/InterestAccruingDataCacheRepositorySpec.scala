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
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseInterestAccruingDrilldown

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class InterestAccruingDataCacheRepositorySpec extends AnyWordSpec with Matchers with BeforeAndAfter {

  private val mgdDb: MGDDatabase = mock(classOf[Database]).asInstanceOf[MGDDatabase]
  private val gtrDb: GTRDatabase = mock(classOf[Database]).asInstanceOf[GTRDatabase]
  private val mgdMockConnection: Connection = mock(classOf[Connection])
  private val gtrMockConnection: Connection = mock(classOf[Connection])
  private val mockCsMgd: CallableStatement = mock(classOf[CallableStatement])
  private val mockCsGtr: CallableStatement = mock(classOf[CallableStatement])
  private val itemsRs: ResultSet = mock(classOf[ResultSet])

  private val repository: InterestAccruingDataCacheRepository = new InterestAccruingDataCacheRepository(
    mgdDb = mgdDb,
    gtrDb = gtrDb
  )

  before {
    Mockito.reset(mgdDb, gtrDb, mgdMockConnection, gtrMockConnection, mockCsMgd, mockCsGtr, itemsRs)

    when(mgdDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mgdMockConnection)
    }

    when(gtrDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(gtrMockConnection)
    }

    when(mgdMockConnection.prepareCall("{ call MGD_LNP_PK.getMGDAccruingDrilldown(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }")).thenReturn(mockCsMgd)
    when(gtrMockConnection.prepareCall("{ call GTR_LNP_PK.getGTRAccruingDrilldown(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }")).thenReturn(mockCsGtr)
  }

  "getInterestAccruingDrilldown" should {

    "return InterestAccruing for MGD regime when stored procedure returns data" in {
      val regNumber = "XWM12345678901"
      val interestId = "INT001"

      when(mockCsMgd.getDate(5)).thenReturn(Date.valueOf("2013-01-01"))
      when(mockCsMgd.getDate(6)).thenReturn(Date.valueOf("2014-11-03"))
      when(mockCsMgd.getBigDecimal(7)).thenReturn(java.math.BigDecimal.valueOf(1250.50))
      when(mockCsMgd.getObject(8)).thenReturn(1)
      when(mockCsMgd.getObject(9)).thenReturn(42)
      when(mockCsMgd.getObject(10)).thenReturn(itemsRs)

      when(itemsRs.next()).thenReturn(true, false)
      when(itemsRs.getObject("p_interest_on")).thenReturn(java.math.BigDecimal.valueOf(1000.00))
      when(itemsRs.getDate("p_date_from")).thenReturn(Date.valueOf("2013-06-01"))
      when(itemsRs.getDate("p_date_to")).thenReturn(Date.valueOf("2014-06-01"))
      when(itemsRs.getObject("p_no_of_days")).thenReturn(java.math.BigDecimal.valueOf(365))
      when(itemsRs.getObject("p_rate")).thenReturn(java.math.BigDecimal.valueOf(2.5))
      when(itemsRs.getObject("p_amount")).thenReturn(java.math.BigDecimal.valueOf(1250.50))

      val result = repository.getInterestAccruingDrilldown(Regime.MGD, regNumber, interestId, 1, 10).futureValue

      result            shouldBe validResponseInterestAccruingDrilldown
      result.items.size shouldBe 1

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setString(2, interestId)
      verify(mockCsMgd).setInt(3, 1)
      verify(mockCsMgd).setInt(4, 10)
      verify(mockCsMgd).registerOutParameter(5, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(6, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(7, oracle.jdbc.OracleTypes.DECIMAL)
      verify(mockCsMgd).registerOutParameter(8, oracle.jdbc.OracleTypes.NUMERIC)
      verify(mockCsMgd).registerOutParameter(9, oracle.jdbc.OracleTypes.NUMERIC)
      verify(mockCsMgd).registerOutParameter(10, oracle.jdbc.OracleTypes.CURSOR)
      verify(mockCsMgd).execute()

      verify(mockCsMgd).getDate(5)
      verify(mockCsMgd).getDate(6)
      verify(mockCsMgd).getBigDecimal(7)
      verify(mockCsMgd).getObject(8)
      verify(mockCsMgd).getObject(9)
      verify(mockCsMgd).getObject(10)

      verify(itemsRs, times(2)).next()
      verify(itemsRs).getObject("p_interest_on")
      verify(itemsRs).getDate("p_date_from")
      verify(itemsRs).getDate("p_date_to")
      verify(itemsRs).getObject("p_no_of_days")
      verify(itemsRs).getObject("p_rate")
      verify(itemsRs).getObject("p_amount")
      verify(itemsRs).close()
      verify(mockCsMgd).close()
    }

    "return empty InterestAccruing when cursor is null" in {
      val regNumber: Null = null
      val interestId = "INT001"

      when(mockCsMgd.getDate(5)).thenReturn(null)
      when(mockCsMgd.getDate(6)).thenReturn(null)
      when(mockCsMgd.getBigDecimal(7)).thenReturn(null)
      when(mockCsMgd.getObject(8)).thenReturn(null)
      when(mockCsMgd.getObject(9)).thenReturn(null)
      when(mockCsMgd.getObject(10)).thenReturn(null)

      val result = repository.getInterestAccruingDrilldown(Regime.MGD, regNumber, interestId, 1, 10).futureValue

      result shouldBe InterestAccruingDrilldown(None, None, BigDecimal(0), 0, None, List())

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setString(2, interestId)
      verify(mockCsMgd).setInt(3, 1)
      verify(mockCsMgd).setInt(4, 10)
      verify(mockCsMgd).registerOutParameter(5, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(6, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(7, oracle.jdbc.OracleTypes.DECIMAL)
      verify(mockCsMgd).registerOutParameter(8, oracle.jdbc.OracleTypes.NUMERIC)
      verify(mockCsMgd).registerOutParameter(9, oracle.jdbc.OracleTypes.NUMERIC)
      verify(mockCsMgd).registerOutParameter(10, oracle.jdbc.OracleTypes.CURSOR)
      verify(mockCsMgd).execute()

      verify(mockCsMgd).getDate(5)
      verify(mockCsMgd).getDate(6)
      verify(mockCsMgd).getBigDecimal(7)
      verify(mockCsMgd).getObject(8)
      verify(mockCsMgd).getObject(9)
      verify(mockCsMgd).getObject(10)
      verify(itemsRs, times(0)).next()
      verify(itemsRs, times(0)).close()
      verify(mockCsMgd).close()
    }

    "return empty items list when cursor result set is empty" in {
      val regNumber = "XWM00000001770"
      val interestId = "INT002"

      when(mockCsMgd.getDate(5)).thenReturn(Date.valueOf("2016-02-29"))
      when(mockCsMgd.getDate(6)).thenReturn(Date.valueOf("2017-06-15"))
      when(mockCsMgd.getBigDecimal(6)).thenReturn(java.math.BigDecimal.valueOf(500.00))
      when(mockCsMgd.getObject(7)).thenReturn(0)
      when(mockCsMgd.getObject(9)).thenReturn(null)
      when(mockCsMgd.getObject(10)).thenReturn(itemsRs)
      when(itemsRs.next()).thenReturn(false)

      val result = repository.getInterestAccruingDrilldown(Regime.MGD, regNumber, interestId, 1, 10).futureValue

      result shouldBe InterestAccruingDrilldown(Some(LocalDate.of(2016, 2, 29)), Some(LocalDate.of(2017, 6, 15)), BigDecimal(0), 0, None, List())

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setString(2, interestId)
      verify(mockCsMgd).setInt(3, 1)
      verify(mockCsMgd).setInt(4, 10)
      verify(mockCsMgd).registerOutParameter(5, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(6, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(7, oracle.jdbc.OracleTypes.DECIMAL)
      verify(mockCsMgd).registerOutParameter(8, oracle.jdbc.OracleTypes.NUMERIC)
      verify(mockCsMgd).registerOutParameter(9, oracle.jdbc.OracleTypes.NUMERIC)
      verify(mockCsMgd).registerOutParameter(10, oracle.jdbc.OracleTypes.CURSOR)
      verify(mockCsMgd).execute()

      verify(mockCsMgd).getDate(5)
      verify(mockCsMgd).getDate(6)
      verify(mockCsMgd).getBigDecimal(7)
      verify(mockCsMgd).getObject(8)
      verify(mockCsMgd).getObject(9)
      verify(mockCsMgd).getObject(10)
      verify(itemsRs, times(1)).next()
      verify(itemsRs, times(0)).getObject("p_interest_on")
      verify(itemsRs).close()
      verify(mockCsMgd).close()
    }

    Regime.values.toList.filterNot(_ == Regime.MGD).foreach { regime =>
      s"return InterestAccruing for $regime regime when stored procedure returns data" in {
        val regNumber = "XWM12345678901"
        val interestId = "INT001"

        when(mockCsGtr.getDate(5)).thenReturn(Date.valueOf("2013-01-01"))
        when(mockCsGtr.getDate(6)).thenReturn(Date.valueOf("2014-11-03"))
        when(mockCsGtr.getBigDecimal(7)).thenReturn(java.math.BigDecimal.valueOf(1250.50))
        when(mockCsGtr.getObject(8)).thenReturn(1)
        when(mockCsGtr.getObject(9)).thenReturn(42)
        when(mockCsGtr.getObject(10)).thenReturn(itemsRs)

        when(itemsRs.next()).thenReturn(true, false)
        when(itemsRs.getObject("p_interest_on")).thenReturn(java.math.BigDecimal.valueOf(1000.00))
        when(itemsRs.getDate("p_date_from")).thenReturn(Date.valueOf("2013-06-01"))
        when(itemsRs.getDate("p_date_to")).thenReturn(Date.valueOf("2014-06-01"))
        when(itemsRs.getObject("p_no_of_days")).thenReturn(java.math.BigDecimal.valueOf(365))
        when(itemsRs.getObject("p_rate")).thenReturn(java.math.BigDecimal.valueOf(2.5))
        when(itemsRs.getObject("p_amount")).thenReturn(java.math.BigDecimal.valueOf(1250.50))

        val result = repository.getInterestAccruingDrilldown(regime, regNumber, interestId, 1, 10).futureValue

        result            shouldBe validResponseInterestAccruingDrilldown
        result.items.size shouldBe 1

        verify(mockCsGtr).setString(1, regNumber)
        verify(mockCsGtr).setString(2, interestId)
        verify(mockCsGtr).setInt(3, 1)
        verify(mockCsGtr).setInt(4, 10)
        verify(mockCsGtr).registerOutParameter(5, oracle.jdbc.OracleTypes.DATE)
        verify(mockCsGtr).registerOutParameter(6, oracle.jdbc.OracleTypes.DATE)
        verify(mockCsGtr).registerOutParameter(7, oracle.jdbc.OracleTypes.DECIMAL)
        verify(mockCsGtr).registerOutParameter(8, oracle.jdbc.OracleTypes.NUMERIC)
        verify(mockCsGtr).registerOutParameter(9, oracle.jdbc.OracleTypes.NUMERIC)
        verify(mockCsGtr).registerOutParameter(10, oracle.jdbc.OracleTypes.CURSOR)
        verify(mockCsGtr).execute()

        verify(mockCsGtr).getDate(5)
        verify(mockCsGtr).getDate(6)
        verify(mockCsGtr).getBigDecimal(7)
        verify(mockCsGtr).getObject(8)
        verify(mockCsGtr).getObject(9)
        verify(mockCsGtr).getObject(10)

        verify(itemsRs, times(2)).next()
        verify(itemsRs).getObject("p_interest_on")
        verify(itemsRs).getDate("p_date_from")
        verify(itemsRs).getDate("p_date_to")
        verify(itemsRs).getObject("p_no_of_days")
        verify(itemsRs).getObject("p_rate")
        verify(itemsRs).getObject("p_amount")
        verify(itemsRs).close()
        verify(mockCsGtr).close()
      }
    }
  }
}
