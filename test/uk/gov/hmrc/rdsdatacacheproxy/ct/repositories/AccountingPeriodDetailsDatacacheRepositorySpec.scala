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
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.AccountingPeriodDetailsStubData

import java.sql.{CallableStatement, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class AccountingPeriodDetailsDatacacheRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter with AccountingPeriodDetailsStubData {

  var db: Database = _
  var repository: AccountingPeriodDetailsRepositoryImpl = _
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

    repository = new AccountingPeriodDetailsRepositoryImpl(db)
  }

  "getIsAPBalanced" should "return default record" in {
    when(mockCallableStatement.getString(3)).thenReturn("N")
    when(mockCallableStatement.getString(4)).thenReturn("N")
    when(mockCallableStatement.getString(5)).thenReturn("Y")

    when(mockCallableStatement.getBigDecimal(6)).thenReturn(java.math.BigDecimal.valueOf(101.161))
    when(mockCallableStatement.getBigDecimal(7)).thenReturn(java.math.BigDecimal.valueOf(191.7891))
    when(mockCallableStatement.getBigDecimal(8)).thenReturn(java.math.BigDecimal.valueOf(301.563))
    when(mockCallableStatement.getBigDecimal(9)).thenReturn(java.math.BigDecimal.valueOf(401.3236))
    when(mockCallableStatement.getBigDecimal(10)).thenReturn(java.math.BigDecimal.valueOf(501.896))

    val result = repository.getIsAPBalanced(taxRef = 17L, accPeriod = 2L).futureValue
    result shouldBe aPBalancedItemDefault

    verify(mockConnection).prepareCall("{call CT_LNP_PK.isAPBalanced(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, 17L)
    verify(mockCallableStatement).setLong(2, 2L)

    verify(mockCallableStatement).registerOutParameter(3, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCallableStatement).registerOutParameter(4, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCallableStatement).registerOutParameter(5, oracle.jdbc.OracleTypes.VARCHAR)

    verify(mockCallableStatement).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
    verify(mockCallableStatement).registerOutParameter(7, oracle.jdbc.OracleTypes.DECIMAL)
    verify(mockCallableStatement).registerOutParameter(8, oracle.jdbc.OracleTypes.DECIMAL)
    verify(mockCallableStatement).registerOutParameter(9, oracle.jdbc.OracleTypes.DECIMAL)
    verify(mockCallableStatement).registerOutParameter(10, oracle.jdbc.OracleTypes.DECIMAL)

    verify(mockCallableStatement).execute()

    verify(mockCallableStatement).close()
  }

  "getIsAPBalanced" should "return empty record" in {
    when(mockCallableStatement.getString(3)).thenReturn(null)
    when(mockCallableStatement.getString(4)).thenReturn(null)
    when(mockCallableStatement.getString(5)).thenReturn(null)

    when(mockCallableStatement.getBigDecimal(6)).thenReturn(null)
    when(mockCallableStatement.getBigDecimal(7)).thenReturn(null)
    when(mockCallableStatement.getBigDecimal(8)).thenReturn(null)
    when(mockCallableStatement.getBigDecimal(9)).thenReturn(null)
    when(mockCallableStatement.getBigDecimal(10)).thenReturn(null)

    val result = repository.getIsAPBalanced(taxRef = 17L, accPeriod = 2L).futureValue
    result shouldBe aPBalancedItemEmpty

    verify(mockConnection).prepareCall("{call CT_LNP_PK.isAPBalanced(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, 17L)
    verify(mockCallableStatement).setLong(2, 2L)

    verify(mockCallableStatement).registerOutParameter(3, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCallableStatement).registerOutParameter(4, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCallableStatement).registerOutParameter(5, oracle.jdbc.OracleTypes.VARCHAR)

    verify(mockCallableStatement).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
    verify(mockCallableStatement).registerOutParameter(7, oracle.jdbc.OracleTypes.DECIMAL)
    verify(mockCallableStatement).registerOutParameter(8, oracle.jdbc.OracleTypes.DECIMAL)
    verify(mockCallableStatement).registerOutParameter(9, oracle.jdbc.OracleTypes.DECIMAL)
    verify(mockCallableStatement).registerOutParameter(10, oracle.jdbc.OracleTypes.DECIMAL)

    verify(mockCallableStatement).execute()

    verify(mockCallableStatement).close()
  }

}
