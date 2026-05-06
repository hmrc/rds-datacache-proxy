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
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.*
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseReturnsSubmittedSmall

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class GamblingReturnsDataCacheRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: GamblingReturnsDataCacheRepository = _
  var mockConnection: Connection = _
  var mockCs: CallableStatement = _
  var amountDeclaredRs: ResultSet = _

  before {
    db               = mock(classOf[Database])
    mockConnection   = mock(classOf[Connection])
    mockCs           = mock(classOf[CallableStatement])
    amountDeclaredRs = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCs)

    repository = new GamblingReturnsDataCacheRepository(db)
  }

  "getReturnsSubmitted" should "return ReturnsSubmitted when stored procedure returns data" in {

    val regNumber = "XWM12345678901"

    when(mockCs.getDate(4)).thenReturn(Date.valueOf("2016-2-29"))
    when(mockCs.getDate(5)).thenReturn(Date.valueOf("2017-6-15"))
    when(mockCs.getObject(6)).thenReturn(BigDecimal.valueOf(-301.56))
    when(mockCs.getObject(7)).thenReturn(1)
    when(mockCs.getObject(8)).thenReturn(amountDeclaredRs)
    when(amountDeclaredRs.next()).thenReturn(true, false)

    when(amountDeclaredRs.getInt("p_desc_code")).thenReturn(4455)
    when(amountDeclaredRs.getDate("p_period_start")).thenReturn(Date.valueOf("2016-3-09"))
    when(amountDeclaredRs.getDate("p_period_end")).thenReturn(Date.valueOf("2016-5-20"))
    when(amountDeclaredRs.getObject("p_amount")).thenReturn(BigDecimal.valueOf(-943.21))

    val result = repository.getReturnsSubmitted(regNumber, 1, 10).futureValue

    result                     shouldBe validResponseReturnsSubmittedSmall
    result.amountDeclared.size shouldBe 1

    verify(mockCs).setString(1, regNumber)
    verify(mockCs).setInt(2, 1)
    verify(mockCs).setInt(3, 10)
    verify(mockCs).registerOutParameter(4, oracle.jdbc.OracleTypes.DATE)
    verify(mockCs).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
    verify(mockCs).registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCs).execute()

    verify(mockCs).getDate(4)
    verify(mockCs).getDate(5)
    verify(mockCs).getBigDecimal(6)
    verify(mockCs).getObject(7)
    verify(mockCs).getObject(8)
    verify(amountDeclaredRs, times(2)).next()
    verify(amountDeclaredRs).getInt("p_desc_code")
    verify(amountDeclaredRs).getDate("p_period_start")
    verify(amountDeclaredRs).getDate("p_period_end")
    verify(amountDeclaredRs).getObject("p_amount")
    verify(amountDeclaredRs).close()
    verify(mockCs).close()

  }

  "getReturnsSubmitted" should "return empty ReturnsSubmitted when regNumber is null" in {
    val regNumber: Null = null
    when(mockCs.getDate(2)).thenReturn(null)
    val result = repository.getReturnsSubmitted(regNumber, 1, 10).futureValue

    result                shouldBe ReturnsSubmitted(None, None, None, None, List())
    result.amountDeclared shouldBe empty

    verify(mockCs).setString(1, regNumber)
    verify(mockCs).setInt(2, 1)
    verify(mockCs).setInt(3, 10)
    verify(mockCs).registerOutParameter(4, oracle.jdbc.OracleTypes.DATE)
    verify(mockCs).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
    verify(mockCs).registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCs).execute()

    verify(mockCs).getDate(4)
    verify(mockCs).getDate(5)
    verify(mockCs).getBigDecimal(6)
    verify(mockCs).getObject(7)
    verify(mockCs).getObject(8)
    verify(amountDeclaredRs, times(0)).next()
    verify(amountDeclaredRs, times(0)).getInt("p_desc_code")
    verify(amountDeclaredRs, times(0)).close()
    verify(mockCs).close()
  }

  "getReturnsSubmitted" should "return Empty List when AmountDeclared result set is empty" in {
    val regNumber = "XWM00000001770"
    when(mockCs.getDate(4)).thenReturn(Date.valueOf("2016-2-29"))
    when(mockCs.getDate(5)).thenReturn(Date.valueOf("2017-6-15"))
    when(mockCs.getObject(6)).thenReturn(BigDecimal.valueOf(-301.56))
    when(mockCs.getObject(7)).thenReturn(0)
    when(amountDeclaredRs.next()).thenReturn(false)

    val result = repository.getReturnsSubmitted(regNumber, 1, 10).futureValue

    result shouldBe ReturnsSubmitted(Some(LocalDate.of(2016, 2, 29)), Some(LocalDate.of(2017, 6, 15)), Some(-301.56), Some(0), List())

    verify(mockCs).setString(1, regNumber)
    verify(mockCs).setInt(2, 1)
    verify(mockCs).setInt(3, 10)
    verify(mockCs).registerOutParameter(4, oracle.jdbc.OracleTypes.DATE)
    verify(mockCs).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
    verify(mockCs).registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCs).execute()

    verify(mockCs).getDate(4)
    verify(mockCs).getDate(5)
    verify(mockCs).getBigDecimal(6)
    verify(mockCs).getObject(7)
    verify(mockCs).getObject(8)
    verify(amountDeclaredRs, times(0)).next()
    verify(amountDeclaredRs, times(0)).getInt("p_desc_code")
    verify(amountDeclaredRs, times(0)).close()
    verify(mockCs).close()
  }

}
