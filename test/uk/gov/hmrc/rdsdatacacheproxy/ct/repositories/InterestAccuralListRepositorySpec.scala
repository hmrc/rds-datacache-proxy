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

import oracle.jdbc.OracleTypes
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database

import java.sql.{CallableStatement, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{InterestAccrual, InterestAccruals}

import java.time.LocalDate

class InterestAccrualListRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: InterestAccrualListDatacacheRepositoryImpl = _
  var mockConnection: java.sql.Connection = _
  var mockCallableStatement: CallableStatement = _
  var rs: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[java.sql.Connection])
    mockCallableStatement = mock(classOf[CallableStatement])
    rs                    = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repository = new InterestAccrualListDatacacheRepositoryImpl(db)
  }

  "getInterestAccrualList" should "return empty list" in {

    when(mockCallableStatement.getObject(eqTo(4), eqTo(classOf[ResultSet]))).thenReturn(rs)
    when(rs.next()).thenReturn(false) // return empty list

    val taxRef: Long = 3L
    val accPeriod: Long = 2L
    val interestType: String = "IDE"

    val result = repository.getInterestAccrualList(taxRef, accPeriod, interestType).futureValue
    result shouldBe InterestAccruals(List.empty)

    verify(mockConnection).prepareCall("{call CT_DC_PK.getInterestAccrualList(?, ?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, taxRef)
    verify(mockCallableStatement).setLong(2, accPeriod)
    verify(mockCallableStatement).setString(3, interestType)

    verify(mockCallableStatement).registerOutParameter(4, OracleTypes.CURSOR)

    verify(mockCallableStatement).execute()

    verify(rs, times(1)).next()

    verify(mockCallableStatement).close()
  }

  "getInterestAccrualList" should "return Accrual Interest list with a single item" in {
    when(mockCallableStatement.getObject(eqTo(4), eqTo(classOf[ResultSet]))).thenReturn(rs)
    when(rs.next()).thenReturn(true, false)

    when(rs.getBigDecimal("computation_amount")).thenReturn(scala.math.BigDecimal(1).bigDecimal)
    when(rs.getDate("interest_accrual_from_date")).thenReturn(java.sql.Date.valueOf("2021-03-07"))
    when(rs.getDate("interest_accrual_to_date")).thenReturn(java.sql.Date.valueOf("2021-05-07"))
    when(rs.getBigDecimal("interest_rate")).thenReturn(scala.math.BigDecimal(2).bigDecimal)
    when(rs.getBigDecimal("interest_amount")).thenReturn(scala.math.BigDecimal(10).bigDecimal)
    when(rs.getDate("ap_end_date")).thenReturn(java.sql.Date.valueOf("2021-06-07"))

    val taxRef: Long = 1L
    val accPeriod: Long = 2L
    val interestType: String = "IDE"

    val result = repository.getInterestAccrualList(taxRef, accPeriod, interestType).futureValue
    result shouldBe InterestAccruals(
      List(
        InterestAccrual(
          computationAmount       = 1,
          interestAccrualFromDate = LocalDate.of(2021, 3, 7),
          interestAccrualToDate   = LocalDate.of(2021, 5, 7),
          interestRate            = 2,
          interestAmount          = 10,
          apEndDate               = LocalDate.of(2021, 6, 7)
        )
      )
    )

    verify(mockConnection).prepareCall("{call CT_DC_PK.getInterestAccrualList(?, ?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, taxRef)
    verify(mockCallableStatement).setLong(2, accPeriod)
    verify(mockCallableStatement).setString(3, interestType)

    verify(mockCallableStatement).registerOutParameter(4, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(rs, times(2)).next()

    verify(mockCallableStatement).close()
  }

  "getInterestAccrualList" should "return Accrual Interest list with multiple items" in {
    when(mockCallableStatement.getObject(eqTo(4), eqTo(classOf[ResultSet]))).thenReturn(rs)
    when(rs.next()).thenReturn(true, true, false)

    when(rs.getBigDecimal("computation_amount")).thenReturn(scala.math.BigDecimal(1).bigDecimal, scala.math.BigDecimal(1).bigDecimal)
    when(rs.getDate("interest_accrual_from_date")).thenReturn(java.sql.Date.valueOf("2021-03-07"), java.sql.Date.valueOf("2021-06-10"))
    when(rs.getDate("interest_accrual_to_date")).thenReturn(java.sql.Date.valueOf("2021-05-07"), java.sql.Date.valueOf("2021-08-10"))
    when(rs.getBigDecimal("interest_rate")).thenReturn(scala.math.BigDecimal(2).bigDecimal, scala.math.BigDecimal(3).bigDecimal)
    when(rs.getBigDecimal("interest_amount")).thenReturn(scala.math.BigDecimal(10).bigDecimal, scala.math.BigDecimal(23).bigDecimal)
    when(rs.getDate("ap_end_date")).thenReturn(java.sql.Date.valueOf("2021-06-07"), java.sql.Date.valueOf("2021-09-10"))

    val taxRef: Long = 2L
    val accPeriod: Long = 2L
    val interestType: String = "IDE"

    val result = repository.getInterestAccrualList(taxRef, accPeriod, interestType).futureValue
    result shouldBe InterestAccruals(
      List(
        InterestAccrual(
          computationAmount       = 1,
          interestAccrualFromDate = LocalDate.of(2021, 3, 7),
          interestAccrualToDate   = LocalDate.of(2021, 5, 7),
          interestRate            = 2,
          interestAmount          = 10,
          apEndDate               = LocalDate.of(2021, 6, 7)
        ),
        InterestAccrual(
          computationAmount       = 1,
          interestAccrualFromDate = LocalDate.of(2021, 6, 10),
          interestAccrualToDate   = LocalDate.of(2021, 8, 10),
          interestRate            = 3,
          interestAmount          = 23,
          apEndDate               = LocalDate.of(2021, 9, 10)
        )
      )
    )

    verify(mockConnection).prepareCall("{call CT_DC_PK.getInterestAccrualList(?, ?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, taxRef)
    verify(mockCallableStatement).setLong(2, accPeriod)
    verify(mockCallableStatement).setString(3, interestType)

    verify(mockCallableStatement).registerOutParameter(4, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(rs, times(3)).next()

    verify(mockCallableStatement).close()
  }

}
