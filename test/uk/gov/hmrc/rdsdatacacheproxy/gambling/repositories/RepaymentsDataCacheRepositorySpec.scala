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
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseRepaymentsSummary

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class RepaymentsDataCacheRepositorySpec extends AnyWordSpec with Matchers with BeforeAndAfter {

  private var mgdDb: MGDDatabase = _
  private var gtrDb: GTRDatabase = _
  var repository: RepaymentsDataCacheRepository = _
  private var mgdMockConnection: Connection = _
  private var gtrMockConnection: Connection = _
  private var mockCsMgd: CallableStatement = _
  private var mockCsGtr: CallableStatement = _
  var repaymentsRs: ResultSet = _

  before {
    gtrDb             = mock(classOf[Database]).asInstanceOf[GTRDatabase]
    mgdDb             = mock(classOf[Database]).asInstanceOf[MGDDatabase]
    mgdMockConnection = mock(classOf[Connection])
    gtrMockConnection = mock(classOf[Connection])
    mockCsMgd         = mock(classOf[CallableStatement])
    mockCsGtr         = mock(classOf[CallableStatement])
    repaymentsRs      = mock(classOf[ResultSet])

    when(mgdDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mgdMockConnection)
    }

    when(gtrDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(gtrMockConnection)
    }

    when(mgdMockConnection.prepareCall("{ call MGD_LNP_PK.getMGDRepaymentSummary(?, ?, ?, ?, ?, ?) }")).thenReturn(mockCsMgd)
    when(gtrMockConnection.prepareCall("{ call GTR_LNP_PK.getGTRRepaymentSummary(?, ?, ?, ?, ?, ?) }")).thenReturn(mockCsGtr)

    repository = new RepaymentsDataCacheRepository(
      mgdDb = mgdDb,
      gtrDb = gtrDb
    )
  }

  "getRepaymentsSummary" should {
    "return a RepaymentsSummary for MGD regime when stored procedure returns data" in {

      val regNumber = "XWM12345678901"

      when(mockCsMgd.getDate(2)).thenReturn(Date.valueOf("2013-03-01"))
      when(mockCsMgd.getDate(3)).thenReturn(Date.valueOf("2014-03-11"))
      when(mockCsMgd.getBigDecimal(4)).thenReturn(java.math.BigDecimal.valueOf(71.84))
      when(mockCsMgd.getBigDecimal(5)).thenReturn(java.math.BigDecimal.valueOf(-35.76))
      when(mockCsMgd.getBigDecimal(6)).thenReturn(java.math.BigDecimal.valueOf(36.08))

      val result = repository.getRepaymentsSummary(Regime.MGD, regNumber).futureValue

      result shouldBe validResponseRepaymentsSummary

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
      verifyNoInteractions(mockCsGtr)
    }

    "return empty RepaymentsSummary when regNumber is null" in {
      val regNumber: Null = null
      when(mockCsMgd.getDate(2)).thenReturn(null)
      val result = repository.getRepaymentsSummary(Regime.MGD, regNumber).futureValue

      result shouldBe RepaymentsSummary(None, None, BigDecimal(0), BigDecimal(0), BigDecimal(0))

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
      verifyNoInteractions(mockCsGtr)

    }

    Regime.values.toList.filterNot(_ == Regime.MGD).foreach { regime =>
      s"return Penalties for $regime regime when stored procedure returns data" in {

        val regNumber = "XWM12345678901"

        when(mockCsGtr.getDate(2)).thenReturn(Date.valueOf("2013-03-01"))
        when(mockCsGtr.getDate(3)).thenReturn(Date.valueOf("2014-03-11"))
        when(mockCsGtr.getBigDecimal(4)).thenReturn(java.math.BigDecimal.valueOf(71.84))
        when(mockCsGtr.getBigDecimal(5)).thenReturn(java.math.BigDecimal.valueOf(-35.76))
        when(mockCsGtr.getBigDecimal(6)).thenReturn(java.math.BigDecimal.valueOf(36.08))

        val result = repository.getRepaymentsSummary(regime, regNumber).futureValue

        result shouldBe validResponseRepaymentsSummary

        verify(mockCsGtr).setString(1, regNumber)
        verify(mockCsGtr).registerOutParameter(2, oracle.jdbc.OracleTypes.DATE)
        verify(mockCsGtr).registerOutParameter(3, oracle.jdbc.OracleTypes.DATE)
        verify(mockCsGtr).registerOutParameter(4, oracle.jdbc.OracleTypes.DECIMAL)
        verify(mockCsGtr).registerOutParameter(5, oracle.jdbc.OracleTypes.DECIMAL)
        verify(mockCsGtr).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)

        verify(mockCsGtr).execute()

        verify(mockCsGtr).getDate(2)
        verify(mockCsGtr).getDate(3)
        verify(mockCsGtr).getBigDecimal(4)
        verify(mockCsGtr).getBigDecimal(5)
        verify(mockCsGtr).getBigDecimal(6)

        verify(mockCsGtr).close()
        verifyNoInteractions(mockCsMgd)
      }
    }
  }
}
