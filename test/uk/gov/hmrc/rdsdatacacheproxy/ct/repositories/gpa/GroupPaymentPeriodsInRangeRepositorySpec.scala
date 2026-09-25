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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfter, concurrent}
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.PeriodWithinRangeHelper.{periodWithinRangeFalse, periodWithinRangeTrue}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.gpa.PeriodWithinRange
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.gpa.GroupPaymentPeriodsInRangeRepositoryImpl

import java.sql.{CallableStatement, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class GroupPaymentPeriodsInRangeRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: GroupPaymentPeriodsInRangeRepositoryImpl = _
  var mockConnection: java.sql.Connection = _
  var mockCallableStatement: CallableStatement = _
  var mockResultSet: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[java.sql.Connection])
    mockCallableStatement = mock(classOf[CallableStatement])
    mockResultSet         = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repository = new GroupPaymentPeriodsInRangeRepositoryImpl(db)
  }

  "getGroupPaymentPeriodsInRange" should "return PeriodWithinRange with field set to false" in {
    val gpaUTR: Long = 10L
    val nominatedCompanyUTR: Long = 1000L
    val pPeriod: Long = 1L
    val pMonthRestriction: Long = 1L

    when(mockCallableStatement.getString(5)).thenReturn("N")

    val result = repository.getGroupPaymentPeriodsInRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction).futureValue

    result shouldBe periodWithinRangeFalse

    verify(mockConnection).prepareCall("{call CT_DC_PK.isGrpPaymntPeriodInValidRange(?, ?, ?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, gpaUTR)
    verify(mockCallableStatement).setLong(2, nominatedCompanyUTR)
    verify(mockCallableStatement).setLong(3, pPeriod)
    verify(mockCallableStatement).setLong(4, pMonthRestriction)

    verify(mockCallableStatement).registerOutParameter(5, java.sql.Types.VARCHAR) // pIS_PERIOD_WITHIN_RANGE

    verify(mockCallableStatement).execute()

    verify(mockCallableStatement).close()
  }

  "getGroupPaymentPeriodsInRange" should "return PeriodWithinRange with field set to true" in {
    val gpaUTR: Long = 20L
    val nominatedCompanyUTR: Long = 1000L
    val pPeriod: Long = 1L
    val pMonthRestriction: Long = 1L

    when(mockCallableStatement.getString(5)).thenReturn("Y")

    val result = repository.getGroupPaymentPeriodsInRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction).futureValue

    result shouldBe periodWithinRangeTrue

    verify(mockConnection).prepareCall("{call CT_DC_PK.isGrpPaymntPeriodInValidRange(?, ?, ?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, gpaUTR)
    verify(mockCallableStatement).setLong(2, nominatedCompanyUTR)
    verify(mockCallableStatement).setLong(3, pPeriod)
    verify(mockCallableStatement).setLong(4, pMonthRestriction)

    verify(mockCallableStatement).registerOutParameter(5, java.sql.Types.VARCHAR) // pIS_PERIOD_WITHIN_RANGE

    verify(mockCallableStatement).execute()

    verify(mockCallableStatement).close()
  }

  "getGroupPaymentPeriodsInRange" should "close resources when execution throws exception" in {
    val gpaUTR: Long = 999L
    val nominatedCompanyUTR: Long = 1000L
    val pPeriod: Long = 1L
    val pMonthRestriction: Long = 1L

    when(mockCallableStatement.execute()).thenThrow(new RuntimeException("Error from downstream"))

    val ex = repository.getGroupPaymentPeriodsInRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction).failed.futureValue

    ex.getMessage should include("Error from downstream")

    verify(mockCallableStatement).close()
  }
}
