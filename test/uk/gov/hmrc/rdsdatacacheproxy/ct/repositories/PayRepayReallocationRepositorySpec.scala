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
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{PayRepayReallocations, PayRepayReallocationsList}

import java.sql.{CallableStatement, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class PayRepayReallocationRepositorySpec extends AnyFreeSpec with Matchers with BeforeAndAfter {

  val emptyPayRepayReallocationsList: PayRepayReallocationsList = PayRepayReallocationsList(List.empty)
  val payRepayReallocationsListWithOneItem: PayRepayReallocationsList = PayRepayReallocationsList(
    List(
      PayRepayReallocations(totalAmountReoRfrRto = Some(BigDecimal(50.00)), totalAmountPayments = Some(BigDecimal(60.00)))
    )
  )
  val payRepayReallocationsListWithMultipleItem: PayRepayReallocationsList = PayRepayReallocationsList(
    List(
      PayRepayReallocations(totalAmountReoRfrRto = Some(BigDecimal(70.00)), totalAmountPayments = Some(BigDecimal(80.00))),
      PayRepayReallocations(totalAmountReoRfrRto = Some(BigDecimal(70.00)), totalAmountPayments = Some(BigDecimal(80.00)))
    )
  )
  var db: Database = _
  var repo: PayRepayReallocationRepositoryImpl = _
  var mockConnection: java.sql.Connection = _
  var mockCallableStatement: CallableStatement = _
  var rs: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[java.sql.Connection])
    mockCallableStatement = mock(classOf[CallableStatement])

    rs = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repo = new PayRepayReallocationRepositoryImpl(db)

  }

  "getTotalAmounts" - {
    "return an empty payment repayment reallocation list" in {

      when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenReturn(false)

      val result = repo.getTotalAmounts(taxRef = 2L, accPeriod = 3L).futureValue
      result shouldBe emptyPayRepayReallocationsList

      verify(mockConnection).prepareCall("{call CT_DC_PK.getTotAmntsForPayRepayRealloc(?, ?, ?)}")

      verify(mockCallableStatement).setLong(1, 2L)
      verify(mockCallableStatement).setLong(2, 3L)

      verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
      verify(mockCallableStatement).execute()

      verify(rs, times(1)).next()

      verify(mockCallableStatement).close()
    }

    "return an payment repayment reallocation list with a single item" in {
      when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenReturn(true, false)

      when(rs.getBigDecimal("totalAmountReoRfrRto")).thenReturn(scala.math.BigDecimal(50.00).bigDecimal)
      when(rs.getBigDecimal("totalAmountPayments")).thenReturn(scala.math.BigDecimal(60.00).bigDecimal)

      val result = repo.getTotalAmounts(taxRef = 2L, accPeriod = 3L).futureValue
      result shouldBe payRepayReallocationsListWithOneItem

      verify(mockConnection).prepareCall("{call CT_DC_PK.getTotAmntsForPayRepayRealloc(?, ?, ?)}")

      verify(mockCallableStatement).setLong(1, 2L)
      verify(mockCallableStatement).setLong(2, 3L)

      verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
      verify(mockCallableStatement).execute()

      verify(rs, times(2)).next()

      verify(mockCallableStatement).close()
    }

    "return payment repayment reallocation list with two items" in {
      when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenReturn(true, true, false)

      when(rs.getBigDecimal("totalAmountReoRfrRto")).thenReturn(scala.math.BigDecimal(70.00).bigDecimal)
      when(rs.getBigDecimal("totalAmountPayments")).thenReturn(scala.math.BigDecimal(80.00).bigDecimal)

      val result = repo.getTotalAmounts(taxRef = 2L, accPeriod = 3L).futureValue
      result shouldBe payRepayReallocationsListWithMultipleItem

      verify(mockConnection).prepareCall("{call CT_DC_PK.getTotAmntsForPayRepayRealloc(?, ?, ?)}")

      verify(mockCallableStatement).setLong(1, 2L)
      verify(mockCallableStatement).setLong(2, 3L)

      verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
      verify(mockCallableStatement).execute()

      verify(rs, times(3)).next()

      verify(mockCallableStatement).close()
    }
  }
}
