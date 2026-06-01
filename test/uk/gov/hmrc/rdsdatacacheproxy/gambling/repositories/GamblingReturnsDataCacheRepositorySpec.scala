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
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseReturnsSubmittedSmall

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class GamblingReturnsDataCacheRepositorySpec extends AnyWordSpec with Matchers with BeforeAndAfter {

  private val gtrDb: GTRDatabase = mock(classOf[Database]).asInstanceOf[GTRDatabase]
  private val mgdDb: MGDDatabase = mock(classOf[Database]).asInstanceOf[MGDDatabase]
  private val mgdMockConnection: Connection = mock(classOf[Connection])
  private val gtrMockConnection: Connection = mock(classOf[Connection])
  private val mockCsMgd: CallableStatement = mock(classOf[CallableStatement])
  private val mockCsGtr: CallableStatement = mock(classOf[CallableStatement])
  private val amountDeclaredRs: ResultSet = mock(classOf[ResultSet])
  private val assessmentsRs: ResultSet = mock(classOf[ResultSet])

  private val repository: GamblingReturnsDataCacheRepository = new GamblingReturnsDataCacheRepository(
    mgdDb = mgdDb,
    gtrDb = gtrDb
  )

  before {
    Mockito.reset(mgdDb, gtrDb, mgdMockConnection, gtrMockConnection, mockCsMgd, mockCsGtr, amountDeclaredRs, assessmentsRs)

    when(mgdDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mgdMockConnection)
    }

    when(gtrDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(gtrMockConnection)
    }

    when(mgdMockConnection.prepareCall(any[String])).thenReturn(mockCsMgd)
    when(gtrMockConnection.prepareCall(any[String])).thenReturn(mockCsGtr)
  }

  "getReturnsSubmitted" should {
    "return ReturnsSubmitted when regime is MGD and stored procedure returns data" in {
      val regNumber = "XWM12345678901"

      when(mockCsMgd.getDate(4)).thenReturn(Date.valueOf("2016-2-29"))
      when(mockCsMgd.getDate(5)).thenReturn(Date.valueOf("2017-6-15"))
      when(mockCsMgd.getObject(6)).thenReturn(BigDecimal.valueOf(-301.56))
      when(mockCsMgd.getObject(7)).thenReturn(1)
      when(mockCsMgd.getObject(8)).thenReturn(amountDeclaredRs)
      when(amountDeclaredRs.next()).thenReturn(true, false)

      when(amountDeclaredRs.getInt("p_desc_code")).thenReturn(4455)
      when(amountDeclaredRs.getDate("p_period_start")).thenReturn(Date.valueOf("2016-3-09"))
      when(amountDeclaredRs.getDate("p_period_end")).thenReturn(Date.valueOf("2016-5-20"))
      when(amountDeclaredRs.getObject("p_amount")).thenReturn(BigDecimal.valueOf(-943.21))

      val result = repository.getReturnsSubmitted(Regime.MGD, regNumber, 1, 10).futureValue

      result                     shouldBe validResponseReturnsSubmittedSmall
      result.amountDeclared.size shouldBe 1

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
      verify(amountDeclaredRs, times(2)).next()
      verify(amountDeclaredRs).getInt("p_desc_code")
      verify(amountDeclaredRs).getDate("p_period_start")
      verify(amountDeclaredRs).getDate("p_period_end")
      verify(amountDeclaredRs).getObject("p_amount")
      verify(amountDeclaredRs).close()
      verify(mockCsMgd).close()
    }

    Regime.values.toList.filter(_ != Regime.MGD).foreach { regime =>
      s"return ReturnsSubmitted when regime is $regime and stored procedure returns data" in {
        val regNumber = "XWM12345678901"

        when(mockCsGtr.getDate(4)).thenReturn(Date.valueOf("2016-2-29"))
        when(mockCsGtr.getDate(5)).thenReturn(Date.valueOf("2017-6-15"))
        when(mockCsGtr.getObject(6)).thenReturn(BigDecimal.valueOf(-301.56))
        when(mockCsGtr.getObject(7)).thenReturn(1)
        when(mockCsGtr.getObject(8)).thenReturn(amountDeclaredRs)
        when(amountDeclaredRs.next()).thenReturn(true, false)

        when(amountDeclaredRs.getInt("p_desc_code")).thenReturn(4455)
        when(amountDeclaredRs.getDate("p_period_start")).thenReturn(Date.valueOf("2016-3-09"))
        when(amountDeclaredRs.getDate("p_period_end")).thenReturn(Date.valueOf("2016-5-20"))
        when(amountDeclaredRs.getObject("p_amount")).thenReturn(BigDecimal.valueOf(-943.21))

        val result = repository.getReturnsSubmitted(regime, regNumber, 1, 10).futureValue

        result                     shouldBe validResponseReturnsSubmittedSmall
        result.amountDeclared.size shouldBe 1

        verify(mockCsGtr).setString(1, regNumber)
        verify(mockCsGtr).setInt(2, 1)
        verify(mockCsGtr).setInt(3, 10)
        verify(mockCsGtr).registerOutParameter(4, oracle.jdbc.OracleTypes.DATE)
        verify(mockCsGtr).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
        verify(mockCsGtr).registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR)
        verify(mockCsGtr).execute()

        verify(mockCsGtr).getDate(4)
        verify(mockCsGtr).getDate(5)
        verify(mockCsGtr).getBigDecimal(6)
        verify(mockCsGtr).getObject(7)
        verify(mockCsGtr).getObject(8)
        verify(amountDeclaredRs, times(2)).next()
        verify(amountDeclaredRs).getInt("p_desc_code")
        verify(amountDeclaredRs).getDate("p_period_start")
        verify(amountDeclaredRs).getDate("p_period_end")
        verify(amountDeclaredRs).getObject("p_amount")
        verify(amountDeclaredRs).close()
        verify(mockCsGtr).close()
      }
    }

    "return empty ReturnsSubmitted when regNumber is null" in {
      val regNumber: Null = null
      when(mockCsMgd.getDate(2)).thenReturn(null)
      val result = repository.getReturnsSubmitted(Regime.MGD, regNumber, 1, 10).futureValue

      result                shouldBe ReturnsSubmitted(None, None, None, None, List())
      result.amountDeclared shouldBe empty

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
      verify(amountDeclaredRs, times(0)).next()
      verify(amountDeclaredRs, times(0)).getInt("p_desc_code")
      verify(amountDeclaredRs, times(0)).close()
      verify(mockCsMgd).close()
    }

    "return Empty List when AmountDeclared result set is empty" in {
      val regNumber = "XWM00000001770"
      when(mockCsMgd.getDate(4)).thenReturn(Date.valueOf("2016-2-29"))
      when(mockCsMgd.getDate(5)).thenReturn(Date.valueOf("2017-6-15"))
      when(mockCsMgd.getObject(6)).thenReturn(BigDecimal.valueOf(-301.56))
      when(mockCsMgd.getObject(7)).thenReturn(0)
      when(amountDeclaredRs.next()).thenReturn(false)

      val result = repository.getReturnsSubmitted(Regime.MGD, regNumber, 1, 10).futureValue

      result shouldBe ReturnsSubmitted(Some(LocalDate.of(2016, 2, 29)), Some(LocalDate.of(2017, 6, 15)), Some(-301.56), Some(0), List())

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
      verify(amountDeclaredRs, times(0)).next()
      verify(amountDeclaredRs, times(0)).getInt("p_desc_code")
      verify(amountDeclaredRs, times(0)).close()
      verify(mockCsMgd).close()
    }
  }
}
