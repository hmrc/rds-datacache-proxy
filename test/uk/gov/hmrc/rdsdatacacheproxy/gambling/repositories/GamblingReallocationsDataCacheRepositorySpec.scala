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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.ReallocationsOut.Reallocation
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseReallocationsInSmall

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class GamblingReallocationsDataCacheRepositorySpec extends AnyWordSpec with Matchers with BeforeAndAfter {

  private val gtrDb: GTRDatabase = mock(classOf[Database]).asInstanceOf[GTRDatabase]
  private val mgdDb: MGDDatabase = mock(classOf[Database]).asInstanceOf[MGDDatabase]
  private val mgdMockConnection: Connection = mock(classOf[Connection])
  private val gtrMockConnection: Connection = mock(classOf[Connection])
  private val mockCsMgd: CallableStatement = mock(classOf[CallableStatement])
  private val mockCsGtr: CallableStatement = mock(classOf[CallableStatement])
  private val reallocationsOutRs: ResultSet = mock(classOf[ResultSet])
  private val reallocationsInRs: ResultSet = mock(classOf[ResultSet])
  private val repository: GamblingReallocationsDataCacheRepository = new GamblingReallocationsDataCacheRepository(
    mgdDb = mgdDb,
    gtrDb = gtrDb
  )

  private val startDate = Date.valueOf("2026-5-11")
  private val endDate = Date.valueOf("2026-5-17")
  private val dateProcessed = Date.valueOf("2026-5-14")

  before {
    Mockito.reset(mgdDb, gtrDb, mgdMockConnection, gtrMockConnection, mockCsMgd, mockCsGtr, reallocationsOutRs, reallocationsInRs)
    when(mgdDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mgdMockConnection)
    }

    when(gtrDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(gtrMockConnection)
    }

    when(mgdMockConnection.prepareCall("{ call MGD_LNP_PK.getMGDReallocationsOutDetails(?, ?, ?, ?, ?, ?, ?, ?) }")).thenReturn(mockCsMgd)
    when(gtrMockConnection.prepareCall("{ call GTR_LNP_PK.getGTRReallocationsOutDetails(?, ?, ?, ?, ?, ?, ?, ?) }")).thenReturn(mockCsGtr)

    when(mgdMockConnection.prepareCall("{ call MGD_LNP_PK.getMGDReallocationsInDetails(?, ?, ?, ?, ?, ?, ?, ?) }")).thenReturn(mockCsMgd)
    when(gtrMockConnection.prepareCall("{ call GTR_LNP_PK.getGTRReallocationsInDetails(?, ?, ?, ?, ?, ?, ?, ?) }")).thenReturn(mockCsGtr)

    when(mgdMockConnection.prepareCall("{ call MGD_LNP_PK.getMGDReallocationsDetails(?, ?, ?, ?, ?, ?) }")).thenReturn(mockCsMgd)
    when(gtrMockConnection.prepareCall("{ call GTR_LNP_PK.getGTRReallocationsDetails(?, ?, ?, ?, ?, ?) }")).thenReturn(mockCsGtr)
  }

  "getReallocationsIn" should {
    "return ReallocationsIn for MGD regime when stored procedure returns data" in {

      val regNumber = "XWM12345678901"

      when(mockCsMgd.getDate(4)).thenReturn(Date.valueOf("2016-02-29"))
      when(mockCsMgd.getDate(5)).thenReturn(Date.valueOf("2017-06-15"))
      when(mockCsMgd.getBigDecimal(6)).thenReturn(java.math.BigDecimal.valueOf(301.56))
      when(mockCsMgd.getObject(7)).thenReturn(1)
      when(mockCsMgd.getObject(8)).thenReturn(reallocationsInRs)

      when(reallocationsInRs.next()).thenReturn(true, false)
      when(reallocationsInRs.getDate("p_date_processed")).thenReturn(Date.valueOf("2016-03-09"))
      when(reallocationsInRs.getObject("p_amount")).thenReturn(java.math.BigDecimal.valueOf(943.21))

      val result = repository.getReallocationsIn(Regime.MGD, regNumber, 1, 10).futureValue

      result            shouldBe validResponseReallocationsInSmall
      result.items.size shouldBe 1

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setInt(2, 1)
      verify(mockCsMgd).setInt(3, 10)

      verify(mockCsMgd).registerOutParameter(4, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(5, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
      verify(mockCsMgd).registerOutParameter(7, oracle.jdbc.OracleTypes.NUMERIC)
      verify(mockCsMgd).registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR)

      verify(mockCsMgd).execute()

      verify(mockCsMgd).getDate(4)
      verify(mockCsMgd).getDate(5)
      verify(mockCsMgd).getBigDecimal(6)
      verify(mockCsMgd).getObject(7)
      verify(mockCsMgd).getObject(8)

      verify(reallocationsInRs, times(2)).next()
      verify(reallocationsInRs).getDate("p_date_processed")
      verify(reallocationsInRs).getObject("p_amount")
      verify(reallocationsInRs).close()
      verify(mockCsMgd).close()
    }

    Regime.values.toList.filterNot(_ == Regime.MGD).foreach { regime =>
      s"return ReallocationsIn for $regime regime when stored procedure returns data" in {

        val regNumber = "XWM12345678901"

        when(mockCsGtr.getDate(4)).thenReturn(Date.valueOf("2016-02-29"))
        when(mockCsGtr.getDate(5)).thenReturn(Date.valueOf("2017-06-15"))
        when(mockCsGtr.getBigDecimal(6)).thenReturn(java.math.BigDecimal.valueOf(301.56))
        when(mockCsGtr.getObject(7)).thenReturn(1)
        when(mockCsGtr.getObject(8)).thenReturn(reallocationsInRs)

        when(reallocationsInRs.next()).thenReturn(true, false)
        when(reallocationsInRs.getDate("p_date_processed")).thenReturn(Date.valueOf("2016-03-09"))
        when(reallocationsInRs.getObject("p_amount")).thenReturn(java.math.BigDecimal.valueOf(943.21))

        val result = repository.getReallocationsIn(regime, regNumber, 1, 10).futureValue

        result            shouldBe validResponseReallocationsInSmall
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

        verify(reallocationsInRs, times(2)).next()
        verify(reallocationsInRs).getDate("p_date_processed")
        verify(reallocationsInRs).getObject("p_amount")
        verify(reallocationsInRs).close()
        verify(mockCsGtr).close()
      }
    }

    "return empty ReallocationsIn when cursor is null" in {
      val regNumber: Null = null

      when(mockCsMgd.getDate(4)).thenReturn(null)
      when(mockCsMgd.getDate(5)).thenReturn(null)
      when(mockCsMgd.getBigDecimal(6)).thenReturn(null)
      when(mockCsMgd.getObject(7)).thenReturn(null)
      when(mockCsMgd.getObject(8)).thenReturn(null)

      val result = repository.getReallocationsIn(Regime.MGD, regNumber, 1, 10).futureValue

      result shouldBe Reallocations(None, None, None, None, List())

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setInt(2, 1)
      verify(mockCsMgd).setInt(3, 10)

      verify(mockCsMgd).registerOutParameter(4, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(5, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
      verify(mockCsMgd).registerOutParameter(7, oracle.jdbc.OracleTypes.NUMERIC)
      verify(mockCsMgd).registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR)

      verify(mockCsMgd).execute()

      verify(mockCsMgd).getDate(4)
      verify(mockCsMgd).getDate(5)
      verify(mockCsMgd).getBigDecimal(6)
      verify(mockCsMgd).getObject(7)
      verify(mockCsMgd).getObject(8)

      verify(reallocationsInRs, times(0)).next()
      verify(reallocationsInRs, times(0)).close()
      verify(mockCsMgd).close()
    }

    "return empty list when ReallocationsIn result set is empty" in {

      val regNumber = "XWM00000001770"

      when(mockCsMgd.getDate(4)).thenReturn(Date.valueOf("2016-02-29"))
      when(mockCsMgd.getDate(5)).thenReturn(Date.valueOf("2017-06-15"))
      when(mockCsMgd.getBigDecimal(6)).thenReturn(java.math.BigDecimal.valueOf(301.56))
      when(mockCsMgd.getObject(7)).thenReturn(0)
      when(mockCsMgd.getObject(8)).thenReturn(reallocationsInRs)

      when(reallocationsInRs.next()).thenReturn(false)

      val result = repository.getReallocationsIn(Regime.MGD, regNumber, 1, 10).futureValue

      result shouldBe Reallocations(
        Some(LocalDate.of(2016, 2, 29)),
        Some(LocalDate.of(2017, 6, 15)),
        Some(301.56),
        Some(0),
        List()
      )

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setInt(2, 1)
      verify(mockCsMgd).setInt(3, 10)

      verify(mockCsMgd).registerOutParameter(4, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(5, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
      verify(mockCsMgd).registerOutParameter(7, oracle.jdbc.OracleTypes.NUMERIC)
      verify(mockCsMgd).registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR)

      verify(mockCsMgd).execute()

      verify(mockCsMgd).getDate(4)
      verify(mockCsMgd).getDate(5)
      verify(mockCsMgd).getBigDecimal(6)
      verify(mockCsMgd).getObject(7)
      verify(mockCsMgd).getObject(8)

      verify(reallocationsInRs, times(1)).next()
      verify(reallocationsInRs, times(0)).getDate("p_date_processed")
      verify(reallocationsInRs, times(0)).getObject("p_amount")
      verify(reallocationsInRs).close()
      verify(mockCsMgd).close()
    }

    "return single ReallocationsIn record" in {

      val regNumber = "XWM12345678901"

      val validResponseReallocationsInSmall: Reallocations = Reallocations(
        periodStartDate = Some(LocalDate.of(2016, 2, 29)),
        periodEndDate   = Some(LocalDate.of(2017, 6, 15)),
        total           = Some(301.56),
        totalRecords    = Some(1),
        items = List(
          ReallocationItem(
            dateProcessed = Some(LocalDate.of(2016, 3, 9)),
            amount        = Some(943.21)
          )
        )
      )

      when(mockCsMgd.getDate(4)).thenReturn(Date.valueOf("2016-02-29"))
      when(mockCsMgd.getDate(5)).thenReturn(Date.valueOf("2017-06-15"))
      when(mockCsMgd.getBigDecimal(6)).thenReturn(java.math.BigDecimal.valueOf(301.56))
      when(mockCsMgd.getObject(7)).thenReturn(1)
      when(mockCsMgd.getObject(8)).thenReturn(reallocationsInRs)

      when(reallocationsInRs.next()).thenReturn(true, false)
      when(reallocationsInRs.getDate("p_date_processed")).thenReturn(Date.valueOf("2016-03-09"))
      when(reallocationsInRs.getObject("p_amount")).thenReturn(java.math.BigDecimal.valueOf(943.21))

      val result = repository.getReallocationsIn(Regime.MGD, regNumber, 1, 10).futureValue

      result            shouldBe validResponseReallocationsInSmall
      result.items.size shouldBe 1

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setInt(2, 1)
      verify(mockCsMgd).setInt(3, 10)

      verify(mockCsMgd).registerOutParameter(4, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(5, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
      verify(mockCsMgd).registerOutParameter(7, oracle.jdbc.OracleTypes.NUMERIC)
      verify(mockCsMgd).registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR)

      verify(mockCsMgd).execute()

      verify(mockCsMgd).getDate(4)
      verify(mockCsMgd).getDate(5)
      verify(mockCsMgd).getBigDecimal(6)
      verify(mockCsMgd).getObject(7)
      verify(mockCsMgd).getObject(8)

      verify(reallocationsInRs, times(2)).next()
      verify(reallocationsInRs).getDate("p_date_processed")
      verify(reallocationsInRs).getObject("p_amount")
      verify(reallocationsInRs).close()
      verify(mockCsMgd).close()
    }
  }

  "getReallocationsOut" should {
    "return ReallocationsOut for MGD regime when stored procedure returns data" in {
      val regNumber = "XWM12345678901"

      when(mockCsMgd.getDate(4)).thenReturn(startDate)
      when(mockCsMgd.getDate(5)).thenReturn(endDate)
      when(mockCsMgd.getObject(6)).thenReturn(BigDecimal.valueOf(-301.56))
      when(mockCsMgd.getObject(7)).thenReturn(1)
      when(mockCsMgd.getObject(8)).thenReturn(reallocationsOutRs)
      when(reallocationsOutRs.next()).thenReturn(true, false)
      when(reallocationsOutRs.getDate("p_date_processed")).thenReturn(dateProcessed)
      when(reallocationsOutRs.getObject("p_amount")).thenReturn(BigDecimal(-301.56))

      val result = repository.getReallocationsOut(Regime.MGD, regNumber, 1, 10).futureValue

      result shouldBe ReallocationsOut(
        Option(startDate.toLocalDate),
        Option(endDate.toLocalDate),
        BigDecimal(-301.56),
        1,
        Seq(Reallocation(dateProcessed.toLocalDate, BigDecimal(-301.56)))
      )

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setInt(2, 1)
      verify(mockCsMgd).setInt(3, 10)
      verify(mockCsMgd).registerOutParameter(4, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(5, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
      verify(mockCsMgd).registerOutParameter(7, oracle.jdbc.OracleTypes.NUMERIC)
      verify(mockCsMgd).registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR)
      verify(mockCsMgd).execute()

      verify(mockCsMgd).getDate(4)
      verify(mockCsMgd).getDate(5)
      verify(mockCsMgd).getBigDecimal(6)
      verify(mockCsMgd).getObject(7)
      verify(mockCsMgd).getObject(8)
      verify(reallocationsOutRs, times(2)).next()
      verify(reallocationsOutRs).getDate("p_date_processed")
      verify(reallocationsOutRs).getObject("p_amount")
      verify(reallocationsOutRs).close()
      verify(mockCsMgd).close()
    }

    Regime.values.toList.filterNot(_ == Regime.MGD).foreach { regime =>
      s"return ReallocationsOut for $regime regime when stored procedure returns data" in {
        val regNumber = "XWM12345678901"

        when(mockCsGtr.getDate(4)).thenReturn(startDate)
        when(mockCsGtr.getDate(5)).thenReturn(endDate)
        when(mockCsGtr.getObject(6)).thenReturn(BigDecimal.valueOf(-301.56))
        when(mockCsGtr.getObject(7)).thenReturn(1)
        when(mockCsGtr.getObject(8)).thenReturn(reallocationsOutRs)
        when(reallocationsOutRs.next()).thenReturn(true, false)
        when(reallocationsOutRs.getDate("p_date_processed")).thenReturn(dateProcessed)
        when(reallocationsOutRs.getObject("p_amount")).thenReturn(BigDecimal(-301.56))

        val result = repository.getReallocationsOut(regime, regNumber, 1, 10).futureValue

        result shouldBe ReallocationsOut(
          Option(startDate.toLocalDate),
          Option(endDate.toLocalDate),
          BigDecimal(-301.56),
          1,
          Seq(Reallocation(dateProcessed.toLocalDate, BigDecimal(-301.56)))
        )

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
        verify(reallocationsOutRs, times(2)).next()
        verify(reallocationsOutRs).getDate("p_date_processed")
        verify(reallocationsOutRs).getObject("p_amount")
        verify(reallocationsOutRs).close()
        verify(mockCsGtr).close()
      }
    }

    "return empty ReallocationsOut when regNumber is null" in {
      val regNumber: Null = null
      when(mockCsMgd.getDate(2)).thenReturn(null)
      val result = repository.getReallocationsOut(Regime.MGD, regNumber, 1, 10).futureValue

      result shouldBe ReallocationsOut.empty

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setInt(2, 1)
      verify(mockCsMgd).setInt(3, 10)
      verify(mockCsMgd).registerOutParameter(4, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(5, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
      verify(mockCsMgd).registerOutParameter(7, oracle.jdbc.OracleTypes.NUMERIC)
      verify(mockCsMgd).registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR)
      verify(mockCsMgd).execute()

      verify(mockCsMgd).getDate(4)
      verify(mockCsMgd).getDate(5)
      verify(mockCsMgd).getBigDecimal(6)
      verify(mockCsMgd).getObject(7)
      verify(mockCsMgd).getObject(8)
      verify(reallocationsOutRs, times(0)).next()
      verify(reallocationsOutRs, times(0)).getInt("p_date_processed")
      verify(reallocationsOutRs, times(0)).close()
      verify(mockCsMgd).close()
    }

    "return Empty List when ReallocationsOut result set is empty" in {
      val regNumber = "XWM00000001770"
      when(mockCsMgd.getDate(4)).thenReturn(startDate)
      when(mockCsMgd.getDate(5)).thenReturn(endDate)
      when(mockCsMgd.getObject(6)).thenReturn(BigDecimal.valueOf(0))
      when(mockCsMgd.getObject(7)).thenReturn(0)
      when(reallocationsOutRs.next()).thenReturn(false)

      val result = repository.getReallocationsOut(Regime.MGD, regNumber, 1, 10).futureValue

      result shouldBe ReallocationsOut(Option(startDate.toLocalDate), Option(endDate.toLocalDate), BigDecimal(0), 0, Seq.empty)

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setInt(2, 1)
      verify(mockCsMgd).setInt(3, 10)
      verify(mockCsMgd).registerOutParameter(4, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(5, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
      verify(mockCsMgd).registerOutParameter(7, oracle.jdbc.OracleTypes.NUMERIC)
      verify(mockCsMgd).registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR)
      verify(mockCsMgd).execute()

      verify(mockCsMgd).getDate(4)
      verify(mockCsMgd).getDate(5)
      verify(mockCsMgd).getBigDecimal(6)
      verify(mockCsMgd).getObject(7)
      verify(mockCsMgd).getObject(8)
      verify(reallocationsOutRs, times(0)).next()
      verify(reallocationsOutRs, times(0)).getInt("p_date_processed")
      verify(reallocationsOutRs, times(0)).close()
      verify(mockCsMgd).close()
    }
  }

  "getReallocationsDetails" should {
    "return ReallocationsDetails for MGD regime when stored procedure returns data" in {
      val regNumber = "XWM12345678901"

      when(mockCsMgd.getDate(2)).thenReturn(startDate)
      when(mockCsMgd.getDate(3)).thenReturn(endDate)
      when(mockCsMgd.getObject(4)).thenReturn(BigDecimal.valueOf(201.56))
      when(mockCsMgd.getObject(5)).thenReturn(BigDecimal.valueOf(-301.56))
      when(mockCsMgd.getObject(6)).thenReturn(BigDecimal.valueOf(-101.56))

      val result = repository.getReallocationsDetails(Regime.MGD, regNumber).futureValue

      result shouldBe ReallocationsDetails(
        Option(startDate.toLocalDate),
        Option(endDate.toLocalDate),
        BigDecimal(201.56),
        BigDecimal(-301.56),
        BigDecimal(-101.56)
      )

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).registerOutParameter(2, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(3, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(4, oracle.jdbc.OracleTypes.DECIMAL)
      verify(mockCsMgd).registerOutParameter(5, oracle.jdbc.OracleTypes.DECIMAL)
      verify(mockCsMgd).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
      verify(mockCsMgd).execute()
      verify(mockCsMgd).close()
    }

    Regime.values.toList.filterNot(_ == Regime.MGD).foreach { regime =>
      s"return ReallocationsDetails for $regime regime when stored procedure returns data" in {
        val regNumber = "XWM12345678901"

        when(mockCsGtr.getDate(2)).thenReturn(startDate)
        when(mockCsGtr.getDate(3)).thenReturn(endDate)
        when(mockCsGtr.getObject(4)).thenReturn(BigDecimal.valueOf(201.56))
        when(mockCsGtr.getObject(5)).thenReturn(BigDecimal.valueOf(-301.56))
        when(mockCsGtr.getObject(6)).thenReturn(BigDecimal.valueOf(-101.56))

        val result = repository.getReallocationsDetails(regime, regNumber).futureValue

        result shouldBe ReallocationsDetails(
          Option(startDate.toLocalDate),
          Option(endDate.toLocalDate),
          BigDecimal(201.56),
          BigDecimal(-301.56),
          BigDecimal(-101.56)
        )

        verify(mockCsGtr).setString(1, regNumber)
        verify(mockCsGtr).registerOutParameter(2, oracle.jdbc.OracleTypes.DATE)
        verify(mockCsGtr).registerOutParameter(3, oracle.jdbc.OracleTypes.DATE)
        verify(mockCsGtr).registerOutParameter(4, oracle.jdbc.OracleTypes.DECIMAL)
        verify(mockCsGtr).registerOutParameter(5, oracle.jdbc.OracleTypes.DECIMAL)
        verify(mockCsGtr).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
        verify(mockCsGtr).execute()
        verify(mockCsGtr).close()
      }
    }

    "return empty ReallocationsDetails when regNumber is null" in {
      val regNumber: Null = null
      when(mockCsMgd.getDate(2)).thenReturn(null)
      val result = repository.getReallocationsDetails(Regime.MGD, regNumber).futureValue

      result shouldBe ReallocationsDetails(None, None, 0, 0, 0)

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).registerOutParameter(2, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(3, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).registerOutParameter(4, oracle.jdbc.OracleTypes.DECIMAL)
      verify(mockCsMgd).registerOutParameter(5, oracle.jdbc.OracleTypes.DECIMAL)
      verify(mockCsMgd).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
      verify(mockCsMgd).execute()

      verify(mockCsMgd).getDate(2)
      verify(mockCsMgd).getDate(3)
      verify(mockCsMgd).getBigDecimal(4)
      verify(mockCsMgd).getBigDecimal(5)
      verify(mockCsMgd).getBigDecimal(6)
      verify(mockCsMgd).close()
    }
  }
}
