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
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseAssessmentsInAbsenceSmall

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class AssessmentsInAbsenceDataCacheRepositorySpec extends AnyWordSpec with Matchers with BeforeAndAfter {

  private val gtrDb: Database = mock(classOf[Database])
  private val mgdDb: Database = mock(classOf[Database])
  private val mgdMockConnection: Connection = mock(classOf[Connection])
  private val gtrMockConnection: Connection = mock(classOf[Connection])
  private val mockCsMgd: CallableStatement = mock(classOf[CallableStatement])
  private val mockCsGtr: CallableStatement = mock(classOf[CallableStatement])
  private val amountDeclaredRs: ResultSet = mock(classOf[ResultSet])
  private val assessmentsWithoutRs: ResultSet = mock(classOf[ResultSet])
  private val repository: AssessmentsInAbsenceDataCacheRepository = new AssessmentsInAbsenceDataCacheRepository(
    mgdDb = mgdDb,
    gtrDb = gtrDb
  )

  before {
    Mockito.reset(mgdDb, gtrDb, mgdMockConnection, gtrMockConnection, mockCsMgd, mockCsGtr, amountDeclaredRs, assessmentsWithoutRs)
    when(mgdDb.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mgdMockConnection)
    }

    when(gtrDb.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(gtrMockConnection)
    }

    when(mgdMockConnection.prepareCall("{ call MGD_LNP_PK.getAssessmentsWithoutReturn(?, ?, ?, ?, ?, ?, ?, ?) }")).thenReturn(mockCsMgd)
    when(gtrMockConnection.prepareCall("{ call GTR_LNP_PK.getAssessmentsWithoutReturn(?, ?, ?, ?, ?, ?, ?, ?) }")).thenReturn(mockCsGtr)
  }

  "getAssessmentsWithoutReturn" should {
    "return AssessmentsWithoutReturn when regime is MGD and stored procedure returns data" in {

      val regNumber = "XWM12345678901"

      when(mockCsMgd.getDate(4)).thenReturn(Date.valueOf("2016-2-29"))
      when(mockCsMgd.getDate(5)).thenReturn(Date.valueOf("2017-6-15"))
      when(mockCsMgd.getObject(6)).thenReturn(BigDecimal.valueOf(-301.56))
      when(mockCsMgd.getObject(7)).thenReturn(1)
      when(mockCsMgd.getObject(8)).thenReturn(assessmentsWithoutRs)
      when(assessmentsWithoutRs.next()).thenReturn(true, false)

      when(assessmentsWithoutRs.getDate("p_date_raised")).thenReturn(Date.valueOf("2016-1-01"))
      when(assessmentsWithoutRs.getDate("p_period_start")).thenReturn(Date.valueOf("2016-3-09"))
      when(assessmentsWithoutRs.getDate("p_period_end")).thenReturn(Date.valueOf("2016-5-20"))
      when(assessmentsWithoutRs.getObject("p_amount")).thenReturn(BigDecimal.valueOf(-943.21))

      val result = repository.getAssessmentsWithoutReturn(Regime.MGD, regNumber, 1, 10).futureValue

      result            shouldBe validResponseAssessmentsInAbsenceSmall
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
      verify(assessmentsWithoutRs, times(2)).next()
      verify(assessmentsWithoutRs).getDate("p_date_raised")
      verify(assessmentsWithoutRs).getDate("p_period_start")
      verify(assessmentsWithoutRs).getDate("p_period_end")
      verify(assessmentsWithoutRs).getObject("p_amount")
      verify(assessmentsWithoutRs).close()
      verify(mockCsMgd).close()

    }

    Regime.values.toList.filter(_ != Regime.MGD).foreach { regime =>
      s"return AssessmentWithoutReturn when regime is $regime and stored procedure returns data" in {

        val regNumber = "XWM12345678901"

        when(mockCsGtr.getDate(4)).thenReturn(Date.valueOf("2016-2-29"))
        when(mockCsGtr.getDate(5)).thenReturn(Date.valueOf("2017-6-15"))
        when(mockCsGtr.getObject(6)).thenReturn(BigDecimal.valueOf(-301.56))
        when(mockCsGtr.getObject(7)).thenReturn(1)
        when(mockCsGtr.getObject(8)).thenReturn(assessmentsWithoutRs)
        when(assessmentsWithoutRs.next()).thenReturn(true, false)

        when(assessmentsWithoutRs.getDate("p_date_raised")).thenReturn(Date.valueOf("2016-1-01"))
        when(assessmentsWithoutRs.getDate("p_period_start")).thenReturn(Date.valueOf("2016-3-09"))
        when(assessmentsWithoutRs.getDate("p_period_end")).thenReturn(Date.valueOf("2016-5-20"))
        when(assessmentsWithoutRs.getObject("p_amount")).thenReturn(BigDecimal.valueOf(-943.21))

        val result = repository.getAssessmentsWithoutReturn(regime, regNumber, 1, 10).futureValue

        result            shouldBe validResponseAssessmentsInAbsenceSmall
        result.items.size shouldBe 1

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
        verify(assessmentsWithoutRs, times(2)).next()
        verify(assessmentsWithoutRs).getDate("p_date_raised")
        verify(assessmentsWithoutRs).getDate("p_period_start")
        verify(assessmentsWithoutRs).getDate("p_period_end")
        verify(assessmentsWithoutRs).getObject("p_amount")
        verify(assessmentsWithoutRs).close()
        verify(mockCsGtr).close()
      }

      s"return empty AssessmentsInAbsence when regNumber is null for regime $regime" in {
        val regNumber: Null = null
        when(mockCsMgd.getDate(2)).thenReturn(null)
        val result = repository.getAssessmentsWithoutReturn(Regime.MGD, regNumber, 1, 10).futureValue

        result       shouldBe AssessmentsInAbsence(None, None, None, None, List())
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
        verify(assessmentsWithoutRs, times(0)).next()
        verify(assessmentsWithoutRs, times(0)).getDate("p_date_raised")
        verify(assessmentsWithoutRs, times(0)).close()
        verify(mockCsMgd).close()
      }

      s"return Empty List when AmountDeclared result set is empty for regime $regime" in {
        val regNumber = "XWM00000001770"
        when(mockCsMgd.getDate(4)).thenReturn(Date.valueOf("2016-2-29"))
        when(mockCsMgd.getDate(5)).thenReturn(Date.valueOf("2017-6-15"))
        when(mockCsMgd.getObject(6)).thenReturn(BigDecimal.valueOf(-301.56))
        when(mockCsMgd.getObject(7)).thenReturn(0)
        when(assessmentsWithoutRs.next()).thenReturn(false)

        val result = repository.getAssessmentsWithoutReturn(Regime.MGD, regNumber, 1, 10).futureValue

        result shouldBe AssessmentsInAbsence(Some(LocalDate.of(2016, 2, 29)), Some(LocalDate.of(2017, 6, 15)), Some(-301.56), Some(0), List())

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
        verify(assessmentsWithoutRs, times(0)).next()
        verify(assessmentsWithoutRs, times(0)).getDate("p_date_raised")
        verify(assessmentsWithoutRs, times(0)).close()
        verify(mockCsMgd).close()
      }
    }
  }
}
