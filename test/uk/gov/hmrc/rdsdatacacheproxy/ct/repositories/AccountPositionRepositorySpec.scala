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
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.AccountPositionHelper
import java.sql.{CallableStatement, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import org.mockito.ArgumentMatchers.eq as eqTo

class AccountPositionRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter with AccountPositionHelper {

  var db: Database = _
  var repository: AccountPositionRepositoryImpl = _
  var mockConnection: java.sql.Connection = _
  var mockCallableStatement: CallableStatement = _
  var rs: ResultSet = _
  var rs2: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[java.sql.Connection])
    mockCallableStatement = mock(classOf[CallableStatement])

    rs  = mock(classOf[ResultSet])
    rs2 = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repository = new AccountPositionRepositoryImpl(db)
  }

  "getAccountPosition" should "return default record" in {
    when(mockCallableStatement.getBigDecimal(2)).thenReturn(java.math.BigDecimal.valueOf(15.18))
    when(mockCallableStatement.getDate(4)).thenReturn(Date.valueOf(LocalDate.parse("2026-01-01")))
    when(mockCallableStatement.getString(3)).thenReturn("N")
    when(mockCallableStatement.getString(7)).thenReturn("Y")

    when(mockCallableStatement.getObject(eqTo(5), eqTo(classOf[ResultSet]))).thenReturn(rs)
    when(rs.next()).thenReturn(true, false)

    when(mockCallableStatement.getObject(eqTo(6), eqTo(classOf[ResultSet]))).thenReturn(rs2)
    when(rs2.next()).thenReturn(true, false)

    when(rs.getLong("taxpayer_reference")).thenReturn(1002L)

    when(rs2.getLong("Accounting_period")).thenReturn(51L)
    when(rs2.getDate("AP_End_date")).thenReturn(Date.valueOf("2024-02-03"))
    when(rs2.getBigDecimal("Amount_Due_For_AP")).thenReturn(java.math.BigDecimal.valueOf(4.326))
    when(rs2.getString("AP_Status")).thenReturn("N")

    val result = repository.getAccountPosition(taxRef = 17L).futureValue
    result shouldBe defaultRecord

    verify(mockConnection).prepareCall("{call CT_LNP_PK.getAccountPosition(?, ?, ?, ?, ?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, 17L)

//    verify(mockCallableStatement).registerOutParameter(3, oracle.jdbc.OracleTypes.VARCHAR)
//    verify(mockCallableStatement).registerOutParameter(4, oracle.jdbc.OracleTypes.VARCHAR)
//    verify(mockCallableStatement).registerOutParameter(5, oracle.jdbc.OracleTypes.VARCHAR)
//
//    verify(mockCallableStatement).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
//    verify(mockCallableStatement).registerOutParameter(7, oracle.jdbc.OracleTypes.DECIMAL)
//    verify(mockCallableStatement).registerOutParameter(8, oracle.jdbc.OracleTypes.DECIMAL)
//    verify(mockCallableStatement).registerOutParameter(9, oracle.jdbc.OracleTypes.DECIMAL)
//    verify(mockCallableStatement).registerOutParameter(10, oracle.jdbc.OracleTypes.DECIMAL)

    verify(mockCallableStatement).execute()

    verify(mockCallableStatement).close()
  }

  /*
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
   */

}
