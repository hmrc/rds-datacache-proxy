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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError.RecordNotFound
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{Regime, StatementOverview}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}

import java.sql.{CallableStatement, Connection, Date}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class StatementOverviewDataCacheRepositorySpec extends AnyWordSpec with Matchers with BeforeAndAfter {

  private var mgdDb: MGDDatabase = _
  private var gtrDb: GTRDatabase = _
  var repository: StatementOverviewDataCacheRepository = _
  private var mgdMockConnection: Connection = _
  private var gtrMockConnection: Connection = _
  private var mockCsMgd: CallableStatement = _
  private var mockCsGtr: CallableStatement = _

  private val mgdCall = "{ call MGD_LNP_PK.getMGDAccountOverview(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }"
  private val gtrCall = "{ call GTR_LNP_PK.getGTRAccountOverview(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }"

  private val regNumber = "XWM12345678901"

  before {
    mgdDb             = mock(classOf[Database]).asInstanceOf[MGDDatabase]
    gtrDb             = mock(classOf[Database]).asInstanceOf[GTRDatabase]
    mgdMockConnection = mock(classOf[Connection])
    gtrMockConnection = mock(classOf[Connection])
    mockCsMgd         = mock(classOf[CallableStatement])
    mockCsGtr         = mock(classOf[CallableStatement])

    when(mgdDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mgdMockConnection)
    }

    when(gtrDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(gtrMockConnection)
    }

    when(mgdMockConnection.prepareCall(mgdCall)).thenReturn(mockCsMgd)
    when(gtrMockConnection.prepareCall(gtrCall)).thenReturn(mockCsGtr)

    repository = new StatementOverviewDataCacheRepository(mgdDb = mgdDb, gtrDb = gtrDb)
  }

  private def stubFullResponse(cs: CallableStatement): Unit = {
    when(cs.getDate(2)).thenReturn(Date.valueOf("2013-01-01"))
    when(cs.getDate(3)).thenReturn(Date.valueOf("2014-11-03"))
    when(cs.getBigDecimal(4)).thenReturn(java.math.BigDecimal.valueOf(-15562.47))
    when(cs.getBigDecimal(5)).thenReturn(java.math.BigDecimal.valueOf(-500.00))
    when(cs.getBigDecimal(6)).thenReturn(java.math.BigDecimal.valueOf(-24500.00))
    when(cs.getBigDecimal(7)).thenReturn(java.math.BigDecimal.valueOf(-4500.00))
    when(cs.getBigDecimal(8)).thenReturn(java.math.BigDecimal.valueOf(-1200.00))
    when(cs.getBigDecimal(9)).thenReturn(java.math.BigDecimal.valueOf(-250.00))
    when(cs.getBigDecimal(10)).thenReturn(java.math.BigDecimal.valueOf(-1500.00))
    when(cs.getBigDecimal(11)).thenReturn(java.math.BigDecimal.valueOf(-3500.00))
    when(cs.getBigDecimal(12)).thenReturn(java.math.BigDecimal.valueOf(-1624.97))
    when(cs.getBigDecimal(13)).thenReturn(java.math.BigDecimal.valueOf(22012.50))
    when(cs.getBigDecimal(14)).thenReturn(null)
  }

  private val expectedOverview = StatementOverview(
    gtrPeriodStartDate = Some(LocalDate.of(2013, 1, 1)),
    gtrPeriodEndDate   = Some(LocalDate.of(2014, 11, 3)),
    total              = BigDecimal("-15562.47"),
    balance            = BigDecimal("-500.0"),
    amountDeclared     = BigDecimal("-24500.0"),
    assessments        = BigDecimal("-4500.0"),
    penalties          = BigDecimal("-1200.0"),
    adjustments        = BigDecimal("-250.0"),
    reallocations      = BigDecimal("-1500.0"),
    otherAssessments   = BigDecimal("-3500.0"),
    interest           = BigDecimal("-1624.97"),
    payments           = BigDecimal("22012.5"),
    repayments         = None
  )

  "getStatementOverview" should {

    "return Some(StatementOverview) for MGD regime when stored procedure returns data" in {
      stubFullResponse(mockCsMgd)

      val result = repository.getStatementOverview(Regime.MGD, regNumber).futureValue

      result shouldBe Right(expectedOverview)

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).registerOutParameter(2, java.sql.Types.DATE)
      verify(mockCsMgd).registerOutParameter(3, java.sql.Types.DATE)
      verify(mockCsMgd).registerOutParameter(4, java.sql.Types.DECIMAL)
      verify(mockCsMgd).registerOutParameter(5, java.sql.Types.DECIMAL)
      verify(mockCsMgd).registerOutParameter(6, java.sql.Types.DECIMAL)
      verify(mockCsMgd).registerOutParameter(7, java.sql.Types.DECIMAL)
      verify(mockCsMgd).registerOutParameter(8, java.sql.Types.DECIMAL)
      verify(mockCsMgd).registerOutParameter(9, java.sql.Types.DECIMAL)
      verify(mockCsMgd).registerOutParameter(10, java.sql.Types.DECIMAL)
      verify(mockCsMgd).registerOutParameter(11, java.sql.Types.DECIMAL)
      verify(mockCsMgd).registerOutParameter(12, java.sql.Types.DECIMAL)
      verify(mockCsMgd).registerOutParameter(13, java.sql.Types.DECIMAL)
      verify(mockCsMgd).registerOutParameter(14, java.sql.Types.DECIMAL)
      verify(mockCsMgd).execute()
      verify(mockCsMgd).close()
    }

    "return None when P_TOTAL is null (operator not registered)" in {
      when(mockCsMgd.getBigDecimal(4)).thenReturn(null)
      when(mockCsMgd.getObject(4)).thenReturn(null)

      val result = repository.getStatementOverview(Regime.MGD, regNumber).futureValue

      result shouldBe Left(RecordNotFound)

      verify(mockCsMgd).execute()
      verify(mockCsMgd).close()
    }

    "populate repayments when P_REPAYMENTS is non-null" in {
      stubFullResponse(mockCsMgd)
      when(mockCsMgd.getBigDecimal(14)).thenReturn(java.math.BigDecimal.valueOf(-350.00))

      val result = repository.getStatementOverview(Regime.MGD, regNumber).futureValue

      val repay = result match {
        case Right(s) => s.repayments
        case _        => None
      }
      repay shouldBe Some(BigDecimal("-350.0"))
    }

    "return None for period dates when Oracle returns null dates" in {
      stubFullResponse(mockCsMgd)
      when(mockCsMgd.getDate(2)).thenReturn(null)
      when(mockCsMgd.getDate(3)).thenReturn(null)

      val result = repository.getStatementOverview(Regime.MGD, regNumber).futureValue

      val gtrPeriodStartDate = result match {
        case Right(s) => s.gtrPeriodStartDate
        case _        => None
      }

      gtrPeriodStartDate shouldBe None

      val gtrPeriodEndDate = result match {
        case Right(s) => s.gtrPeriodEndDate
        case _        => None
      }

      gtrPeriodEndDate shouldBe None
    }

    Regime.values.toList.filterNot(_ == Regime.MGD).foreach { regime =>
      s"return Some(StatementOverview) for $regime regime using the GTR stored procedure" in {
        stubFullResponse(mockCsGtr)

        val result = repository.getStatementOverview(regime, regNumber).futureValue

        result shouldBe Right(expectedOverview)

        verify(mockCsGtr).setString(1, regNumber)
        verify(mockCsGtr).execute()
        verify(mockCsGtr).close()
      }
    }
  }
}
