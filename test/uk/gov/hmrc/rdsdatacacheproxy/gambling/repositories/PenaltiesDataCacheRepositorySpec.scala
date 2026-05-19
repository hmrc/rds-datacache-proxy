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
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponsePenaltiesSmall

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class PenaltiesDataCacheRepositorySpec extends AnyWordSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: PenaltiesDataCacheRepository = _
  var mockConnection: Connection = _
  var mockCs: CallableStatement = _
  var itemsRs: ResultSet = _
  var penaltiesRs: ResultSet = _

  before {
    db               = mock(classOf[Database])
    mockConnection   = mock(classOf[Connection])
    mockCs           = mock(classOf[CallableStatement])
    itemsRs          = mock(classOf[ResultSet])
    penaltiesRs      = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCs)

    repository = new PenaltiesDataCacheRepository(db)
  }

  "getPenalties" should {
    "return Penalties when stored procedure returns data" in {

      val regNumber = "XWM12345678901"

      when(mockCs.getDate(4)).thenReturn(Date.valueOf("2013-03-01"))
      when(mockCs.getDate(5)).thenReturn(Date.valueOf("2014-03-11"))
      when(mockCs.getBigDecimal(6)).thenReturn(java.math.BigDecimal.valueOf(-2500.00))
      when(mockCs.getObject(7)).thenReturn(1)
      when(mockCs.getObject(8)).thenReturn(penaltiesRs)

      when(penaltiesRs.next()).thenReturn(true, false)

      when(penaltiesRs.getDate("p_date_raised")).thenReturn(Date.valueOf("2014-09-01"))
      when(penaltiesRs.getInt("p_desc_code")).thenReturn(2680)
      when(penaltiesRs.getObject("p_amount")).thenReturn(java.math.BigDecimal.valueOf(-800.00))
      when(penaltiesRs.getDate("p_period_start")).thenReturn(Date.valueOf("2014-04-01"))
      when(penaltiesRs.getDate("p_period_end")).thenReturn(Date.valueOf("2014-06-30"))

      val result = repository.getPenalties(regNumber, 1, 10).futureValue

      result shouldBe validResponsePenaltiesSmall
      result.items.size shouldBe 1

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

      verify(penaltiesRs, times(2)).next()
      verify(penaltiesRs).getDate("p_date_raised")
      verify(penaltiesRs).getInt("p_desc_code")
      verify(penaltiesRs).getObject("p_amount")
      verify(penaltiesRs).getDate("p_period_start")
      verify(penaltiesRs).getDate("p_period_end")

      verify(penaltiesRs).close()
      verify(mockCs).close()
    }

    "return empty Penalties when regNumber is null" in {
      val regNumber: Null = null
      when(mockCs.getDate(2)).thenReturn(null)
      val result = repository.getPenalties(regNumber, 1, 10).futureValue

      result       shouldBe Penalties(None, None, None, None, List())
      result.items shouldBe empty

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
      verify(penaltiesRs, times(0)).next()
      verify(penaltiesRs, times(0)).getDate("p_date_raised")
      verify(penaltiesRs, times(0)).close()
      verify(mockCs).close()
    }

    "return Empty List when Penalties result set is empty" in {
      val regNumber = "XWM00000001770"
      when(mockCs.getDate(4)).thenReturn(Date.valueOf("2016-2-29"))
      when(mockCs.getDate(5)).thenReturn(Date.valueOf("2017-6-15"))
      when(mockCs.getObject(6)).thenReturn(BigDecimal.valueOf(-301.56))
      when(mockCs.getObject(7)).thenReturn(0)
      when(penaltiesRs.next()).thenReturn(false)

      val result = repository.getPenalties(regNumber, 1, 10).futureValue

      result shouldBe Penalties(Some(LocalDate.of(2016, 2, 29)), Some(LocalDate.of(2017, 6, 15)), Some(-301.56), Some(0), List())

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
      verify(penaltiesRs, times(0)).next()
      verify(penaltiesRs, times(0)).getDate("p_date_raised")
      verify(penaltiesRs, times(0)).close()
      verify(mockCs).close()
    }
  }
}
