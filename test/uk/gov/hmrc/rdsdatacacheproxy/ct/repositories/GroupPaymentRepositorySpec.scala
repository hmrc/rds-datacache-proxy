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
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.GroupPaymentsHelper
import java.sql.{CallableStatement, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global
import java.sql.Date

class GroupPaymentRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter with GroupPaymentsHelper {

  var db: Database = _
  var repository: GroupPaymentsRepositoryImpl = _
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

    repository = new GroupPaymentsRepositoryImpl(db)
  }

  "getGroupSummary" should "return empty record" in {
    when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
    when(rs.next()).thenReturn(false)
    when(mockCallableStatement.getString(5)).thenReturn("CompanyName")

    val result = repository.getGroupSummary(gpaUTR = 17L, nomCompanyUTR = 2L).futureValue
    result shouldBe Some(groupPaymentDetailsEmpty)

    verify(mockConnection).prepareCall("{call CT_GPA_PK.getGPAGroupSummary(?, ?, ?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, 17L)
    verify(mockCallableStatement).setLong(2, 2L)

    verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
    verify(mockCallableStatement).registerOutParameter(4, OracleTypes.CURSOR)

    verify(mockCallableStatement).execute()

    verify(rs, times(1)).next()

    verify(mockCallableStatement).close()
  }

  "getGroupSummary" should "return default record" in {
    when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
    when(mockCallableStatement.getObject(eqTo(4), eqTo(classOf[ResultSet]))).thenReturn(rs2)

    when(rs.next()).thenReturn(true, false)
    when(rs2.next()).thenReturn(true, false)

    when(rs2.getLong("taxpayer_reference")).thenReturn(112L)

    when(rs.getDate("CONTRACT_END_DATE")).thenReturn(Date.valueOf("2026-1-7"))
    when(rs.getBigDecimal("GROUP_TAX_CHARGE")).thenReturn(BigDecimal(11.01).bigDecimal)
    when(rs.getBigDecimal("GROUP_PAYMENT")).thenReturn(BigDecimal(13.02).bigDecimal)
    when(rs.getInt("GROUP_PAYMENT_RECORD_COUNT")).thenReturn(2)
    when(rs.getString("CONTRACT_STATUS")).thenReturn("ACTIVE")
    when(rs.getInt("CONTRACT_VERSION")).thenReturn(1)

    when(mockCallableStatement.getString(5)).thenReturn("Some company name")

    val result = repository.getGroupSummary(gpaUTR = 17L, nomCompanyUTR = 2L).futureValue
    result shouldBe Some(groupPaymentDetails)

    verify(mockConnection).prepareCall("{call CT_GPA_PK.getGPAGroupSummary(?, ?, ?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, 17L)
    verify(mockCallableStatement).setLong(2, 2L)

    verify(mockCallableStatement).registerOutParameter(3, OracleTypes.CURSOR)
    verify(mockCallableStatement).registerOutParameter(4, OracleTypes.CURSOR)

    verify(mockCallableStatement).execute()

    verify(rs, times(2)).next()
    verify(rs2, times(2)).next()

    verify(mockCallableStatement).close()
  }

}
