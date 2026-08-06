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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, verify, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.PayRepayReallocations

import java.math.BigDecimal
import java.sql.CallableStatement
import scala.concurrent.ExecutionContext.Implicits.global

class PayRepayReallocationRepositorySpec extends AnyFreeSpec with Matchers with BeforeAndAfter {

  val emptyPayRepayReallocations: PayRepayReallocations = PayRepayReallocations(Some(0), Some(0))
  val payRepayReallocations: PayRepayReallocations =
    PayRepayReallocations(
      totalAmountReoRfrRto = Some(BigDecimal(50.00)),
      totalAmountPayments  = Some(BigDecimal(60.00))
    )

  var db: Database = _
  var repo: PayRepayReallocationRepositoryImpl = _
  var mockConnection: java.sql.Connection = _
  var mockCallableStatement: CallableStatement = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[java.sql.Connection])
    mockCallableStatement = mock(classOf[CallableStatement])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repo = new PayRepayReallocationRepositoryImpl(db)
  }

  "getTotalAmounts" - {
    "return an empty payment repayment reallocation total amounts" in {
      when(mockCallableStatement.getBigDecimal(3)).thenReturn(scala.math.BigDecimal(0).bigDecimal)
      when(mockCallableStatement.getBigDecimal(4)).thenReturn(scala.math.BigDecimal(0).bigDecimal)

      val result = repo.getTotalAmounts(taxRef = 2L, accPeriod = 3L).futureValue
      result shouldBe emptyPayRepayReallocations

      verify(mockConnection).prepareCall("{call CT_DC_PK.getTotAmntsForPayRepayRealloc(?, ?, ?, ?)}")

      verify(mockCallableStatement).setLong(1, 2L)
      verify(mockCallableStatement).setLong(2, 3L)

      verify(mockCallableStatement).registerOutParameter(3, java.sql.Types.NUMERIC)
      verify(mockCallableStatement).registerOutParameter(4, java.sql.Types.NUMERIC)

      verify(mockCallableStatement).execute()

      verify(mockCallableStatement).close()
    }

    "return a payment repayment reallocation total amounts" in {

      when(mockCallableStatement.getBigDecimal(3)).thenReturn(scala.math.BigDecimal(50.00).bigDecimal)
      when(mockCallableStatement.getBigDecimal(4)).thenReturn(scala.math.BigDecimal(60.00).bigDecimal)

      val result = repo.getTotalAmounts(taxRef = 4L, accPeriod = 2L).futureValue
      result shouldBe payRepayReallocations

      verify(mockConnection).prepareCall("{call CT_DC_PK.getTotAmntsForPayRepayRealloc(?, ?, ?, ?)}")

      verify(mockCallableStatement).setLong(1, 4L)
      verify(mockCallableStatement).setLong(2, 2L)

      verify(mockCallableStatement).registerOutParameter(3, java.sql.Types.NUMERIC)
      verify(mockCallableStatement).registerOutParameter(4, java.sql.Types.NUMERIC)

      verify(mockCallableStatement).execute()

      verify(mockCallableStatement).close()
    }

    "return an exception and close the connection" in {
      when(mockCallableStatement.execute()).thenThrow(new RuntimeException("DB error"))

      val ex = repo.getTotalAmounts(1L, 2L).failed.futureValue
      ex.getMessage should include("DB error")

      verify(mockCallableStatement).close()
    }
  }
}
