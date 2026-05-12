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
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseReallocationsInSmall

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class GamblingReallocationsDataCacheRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: GamblingReallocationsDataCacheRepository = _
  var mockConnection: Connection = _
  var mockCs: CallableStatement = _
  var reallocationsInRs: ResultSet = _

  before {
    db                = mock(classOf[Database])
    mockConnection    = mock(classOf[Connection])
    mockCs            = mock(classOf[CallableStatement])
    reallocationsInRs = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCs)

    repository = new GamblingReallocationsDataCacheRepository(db)
  }

  "getReallocationsIn" should "return ReallocationsIn when stored procedure returns data" in {

    val regNumber = "XWM12345678901"

    when(mockCs.getDate(4)).thenReturn(Date.valueOf("2016-02-29"))
    when(mockCs.getDate(5)).thenReturn(Date.valueOf("2017-06-15"))
    when(mockCs.getBigDecimal(6)).thenReturn(java.math.BigDecimal.valueOf(301.56))
    when(mockCs.getObject(7)).thenReturn(1)
    when(mockCs.getObject(8)).thenReturn(reallocationsInRs)

    when(reallocationsInRs.next()).thenReturn(true, false)
    when(reallocationsInRs.getDate("p_date_processed")).thenReturn(Date.valueOf("2016-03-09"))
    when(reallocationsInRs.getObject("p_amount")).thenReturn(java.math.BigDecimal.valueOf(943.21))

    val result = repository.getReallocationsIn(regNumber, 1, 10).futureValue

    result            shouldBe validResponseReallocationsInSmall
    result.items.size shouldBe 1

    verify(mockCs).setString(1, regNumber)
    verify(mockCs).setInt(2, 1)
    verify(mockCs).setInt(3, 10)

    verify(mockCs).registerOutParameter(4, oracle.jdbc.OracleTypes.DATE)
    verify(mockCs).registerOutParameter(5, oracle.jdbc.OracleTypes.DATE)
    verify(mockCs).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
    verify(mockCs).registerOutParameter(7, oracle.jdbc.OracleTypes.NUMERIC)
    verify(mockCs).registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR)

    verify(mockCs).execute()

    verify(mockCs).getDate(4)
    verify(mockCs).getDate(5)
    verify(mockCs).getBigDecimal(6)
    verify(mockCs).getObject(7)
    verify(mockCs).getObject(8)

    verify(reallocationsInRs, times(2)).next()
    verify(reallocationsInRs).getDate("p_date_processed")
    verify(reallocationsInRs).getObject("p_amount")
    verify(reallocationsInRs).close()
    verify(mockCs).close()
  }

  "getReallocationsIn" should "return empty ReallocationsIn when cursor is null" in {

    val regNumber: Null = null

    when(mockCs.getDate(4)).thenReturn(null)
    when(mockCs.getDate(5)).thenReturn(null)
    when(mockCs.getBigDecimal(6)).thenReturn(null)
    when(mockCs.getObject(7)).thenReturn(null)
    when(mockCs.getObject(8)).thenReturn(null)

    val result = repository.getReallocationsIn(regNumber, 1, 10).futureValue

    result shouldBe Reallocations(None, None, None, None, List())

    verify(mockCs).setString(1, regNumber)
    verify(mockCs).setInt(2, 1)
    verify(mockCs).setInt(3, 10)

    verify(mockCs).registerOutParameter(4, oracle.jdbc.OracleTypes.DATE)
    verify(mockCs).registerOutParameter(5, oracle.jdbc.OracleTypes.DATE)
    verify(mockCs).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
    verify(mockCs).registerOutParameter(7, oracle.jdbc.OracleTypes.NUMERIC)
    verify(mockCs).registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR)

    verify(mockCs).execute()

    verify(mockCs).getDate(4)
    verify(mockCs).getDate(5)
    verify(mockCs).getBigDecimal(6)
    verify(mockCs).getObject(7)
    verify(mockCs).getObject(8)

    verify(reallocationsInRs, times(0)).next()
    verify(reallocationsInRs, times(0)).close()
    verify(mockCs).close()
  }

  "getReallocationsIn" should "return empty list when ReallocationsIn result set is empty" in {

    val regNumber = "XWM00000001770"

    when(mockCs.getDate(4)).thenReturn(Date.valueOf("2016-02-29"))
    when(mockCs.getDate(5)).thenReturn(Date.valueOf("2017-06-15"))
    when(mockCs.getBigDecimal(6)).thenReturn(java.math.BigDecimal.valueOf(301.56))
    when(mockCs.getObject(7)).thenReturn(0)
    when(mockCs.getObject(8)).thenReturn(reallocationsInRs)

    when(reallocationsInRs.next()).thenReturn(false)

    val result = repository.getReallocationsIn(regNumber, 1, 10).futureValue

    result shouldBe Reallocations(
      Some(LocalDate.of(2016, 2, 29)),
      Some(LocalDate.of(2017, 6, 15)),
      Some(301.56),
      Some(0),
      List()
    )

    verify(mockCs).setString(1, regNumber)
    verify(mockCs).setInt(2, 1)
    verify(mockCs).setInt(3, 10)

    verify(mockCs).registerOutParameter(4, oracle.jdbc.OracleTypes.DATE)
    verify(mockCs).registerOutParameter(5, oracle.jdbc.OracleTypes.DATE)
    verify(mockCs).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
    verify(mockCs).registerOutParameter(7, oracle.jdbc.OracleTypes.NUMERIC)
    verify(mockCs).registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR)

    verify(mockCs).execute()

    verify(mockCs).getDate(4)
    verify(mockCs).getDate(5)
    verify(mockCs).getBigDecimal(6)
    verify(mockCs).getObject(7)
    verify(mockCs).getObject(8)

    verify(reallocationsInRs, times(1)).next()
    verify(reallocationsInRs, times(0)).getDate("p_date_processed")
    verify(reallocationsInRs, times(0)).getObject("p_amount")
    verify(reallocationsInRs).close()
    verify(mockCs).close()
  }

  "getReallocationsIn" should "return single ReallocationsIn record" in {

    val regNumber = "XWM12345678901"

    val validResponseReallocationsInSmall: Reallocations = Reallocations(
      periodStartDate = Some(LocalDate.of(2016, 2, 29)),
      periodEndDate   = Some(LocalDate.of(2017, 6, 15)),
      total           = Some(301.56),
      totalRecords    = Some(1),
      items = List(
        ReallocationItem(
          dateProcessed = Some(LocalDate.of(2016, 3, 9)),
          amount        = Some(943.21)
        )
      )
    )

    when(mockCs.getDate(4)).thenReturn(Date.valueOf("2016-02-29"))
    when(mockCs.getDate(5)).thenReturn(Date.valueOf("2017-06-15"))
    when(mockCs.getBigDecimal(6)).thenReturn(java.math.BigDecimal.valueOf(301.56))
    when(mockCs.getObject(7)).thenReturn(1)
    when(mockCs.getObject(8)).thenReturn(reallocationsInRs)

    when(reallocationsInRs.next()).thenReturn(true, false)
    when(reallocationsInRs.getDate("p_date_processed")).thenReturn(Date.valueOf("2016-03-09"))
    when(reallocationsInRs.getObject("p_amount")).thenReturn(java.math.BigDecimal.valueOf(943.21))

    val result = repository.getReallocationsIn(regNumber, 1, 10).futureValue

    result            shouldBe validResponseReallocationsInSmall
    result.items.size shouldBe 1

    verify(mockCs).setString(1, regNumber)
    verify(mockCs).setInt(2, 1)
    verify(mockCs).setInt(3, 10)

    verify(mockCs).registerOutParameter(4, oracle.jdbc.OracleTypes.DATE)
    verify(mockCs).registerOutParameter(5, oracle.jdbc.OracleTypes.DATE)
    verify(mockCs).registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL)
    verify(mockCs).registerOutParameter(7, oracle.jdbc.OracleTypes.NUMERIC)
    verify(mockCs).registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR)

    verify(mockCs).execute()

    verify(mockCs).getDate(4)
    verify(mockCs).getDate(5)
    verify(mockCs).getBigDecimal(6)
    verify(mockCs).getObject(7)
    verify(mockCs).getObject(8)

    verify(reallocationsInRs, times(2)).next()
    verify(reallocationsInRs).getDate("p_date_processed")
    verify(reallocationsInRs).getObject("p_amount")
    verify(reallocationsInRs).close()
    verify(mockCs).close()
  }

}
