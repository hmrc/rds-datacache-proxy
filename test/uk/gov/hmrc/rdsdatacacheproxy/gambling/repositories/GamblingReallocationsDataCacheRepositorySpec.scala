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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.ReallocationsOut.Reallocation

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class GamblingReallocationsDataCacheRepositorySpec extends AnyWordSpec with Matchers with BeforeAndAfter {

  private var db: Database = _
  private var repository: GamblingReallocationsDataCacheRepository = _
  private var mockConnection: Connection = _
  private var mockCsMgd: CallableStatement = _
  private var mockCsGtr: CallableStatement = _
  private var reallocationsOutRs: ResultSet = _

  private val startDate = Date.valueOf("2026-5-11")
  private val endDate = Date.valueOf("2026-5-17")
  private val dateProcessed = Date.valueOf("2026-5-14")

  before {
    db                 = mock(classOf[Database])
    mockConnection     = mock(classOf[Connection])
    mockCsMgd          = mock(classOf[CallableStatement])
    mockCsGtr          = mock(classOf[CallableStatement])
    reallocationsOutRs = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mockConnection)
    }

    when(mockConnection.prepareCall("{ call MGD_LNP_PK.getMGDReallocationsOutDetails(?, ?, ?, ?, ?, ?, ?, ?) }")).thenReturn(mockCsMgd)
    when(mockConnection.prepareCall("{ call GTR_LNP_PK.getGTRReallocationsOutDetails(?, ?, ?, ?, ?, ?, ?, ?) }")).thenReturn(mockCsGtr)

    repository = new GamblingReallocationsDataCacheRepository(db)
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
}
