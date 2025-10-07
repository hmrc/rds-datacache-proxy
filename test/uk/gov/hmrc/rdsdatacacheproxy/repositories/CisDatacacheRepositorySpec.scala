/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy.repositories

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.OptionValues
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.{any as anyArg, eq as eqTo}
import play.api.db.Database
import java.sql.{CallableStatement, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

final class CisDatacacheRepositorySpec extends AnyWordSpec with Matchers with ScalaFutures with OptionValues {

  "getCisTaxpayerByTaxRef" should {
    "return None on empty cursor" in {
      val db = mock(classOf[Database])
      val conn = mock(classOf[java.sql.Connection])
      val cs = mock(classOf[CallableStatement])
      val rs = mock(classOf[ResultSet])

      when(db.withConnection(anyArg())).thenAnswer { inv =>
        val f = inv.getArgument(0, classOf[java.sql.Connection => Any])
        f(conn)
      }
      when(conn.prepareCall(anyArg[String])).thenReturn(cs)
      when(cs.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
      when(rs.next()).thenReturn(false)

      val repo = new CisDatacacheRepository(db)

      val out = repo.getCisTaxpayerByTaxRef("123", "AB456").futureValue
      out mustBe None

      verify(conn).prepareCall("{ call ECISR_SEARCH_PK.getCISTaxpayerByTaxReference(?, ?, ?) }")
      verify(cs).setString(1, "123")
      verify(cs).setString(2, "AB456")
      verify(cs).registerOutParameter(3, oracle.jdbc.OracleTypes.CURSOR)

      verify(rs).close()
      verify(cs).close()
    }
  }

  "return Some(CisTaxpayer) on one-row cursor" in {
    val db = mock(classOf[Database])
    val conn = mock(classOf[java.sql.Connection])
    val cs = mock(classOf[CallableStatement])
    val rs = mock(classOf[ResultSet])

    when(db.withConnection(anyArg())).thenAnswer { inv =>
      val f = inv.getArgument(0, classOf[java.sql.Connection => Any])
      f(conn)
    }
    when(conn.prepareCall(anyArg[String])).thenReturn(cs)

    when(cs.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)

    when(rs.next()).thenReturn(true, false)
    when(rs.getString("UNIQUE_ID")).thenReturn(" 1 ")
    when(rs.getString("TAX_OFFICE_NUMBER")).thenReturn(" 123 ")
    when(rs.getString("TAX_OFFICE_REF")).thenReturn(" AB456 ")
    when(rs.getString("EMPLOYER_NAME1")).thenReturn(" TEST LTD ")

    val repo = new CisDatacacheRepository(db)

    val out = repo.getCisTaxpayerByTaxRef("123", "AB456").futureValue
    val tp = out.value

    tp.uniqueId mustBe "1"
    tp.taxOfficeNumber mustBe "123"
    tp.taxOfficeRef mustBe "AB456"
    tp.employerName1 mustBe Some("TEST LTD")

    verify(conn).prepareCall("{ call ECISR_SEARCH_PK.getCISTaxpayerByTaxReference(?, ?, ?) }")
    verify(cs).setString(1, "123")
    verify(cs).setString(2, "AB456")
    verify(cs).registerOutParameter(3, oracle.jdbc.OracleTypes.CURSOR)

    verify(rs).close()
    verify(cs).close()
  }

  "return Some(taxpayers)  on two-row cursor" in {
    val db = mock(classOf[Database])
    val conn = mock(classOf[java.sql.Connection])
    val cs = mock(classOf[CallableStatement])
    val rs = mock(classOf[ResultSet])

    when(db.withConnection(anyArg())).thenAnswer { inv =>
      val f = inv.getArgument(0, classOf[java.sql.Connection => Any])
      f(conn)
    }
    when(conn.prepareCall(anyArg[String])).thenReturn(cs)
    when(cs.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)

    when(rs.next()).thenReturn(true, true, false)
    when(rs.getString("UNIQUE_ID")).thenReturn(" 1 ", "2")
    when(rs.getString("TAX_OFFICE_NUMBER")).thenReturn(" 123 ")
    when(rs.getString("TAX_OFFICE_REF")).thenReturn(" AB456 ")
    when(rs.getString("EMPLOYER_NAME1")).thenReturn(" TEST LTD ")

    val repo = new CisDatacacheRepository(db)

    val out = repo.getCisTaxpayerByTaxRef("123", "AB456").futureValue
    val tp = out.value

    tp.uniqueId mustBe "1"
    tp.taxOfficeNumber mustBe "123"
    tp.taxOfficeRef mustBe "AB456"
    tp.employerName1 mustBe Some("TEST LTD")

    verify(conn).prepareCall("{ call ECISR_SEARCH_PK.getCISTaxpayerByTaxReference(?, ?, ?) }")
    verify(cs).setString(1, "123")
    verify(cs).setString(2, "AB456")
    verify(cs).registerOutParameter(3, oracle.jdbc.OracleTypes.CURSOR)

    verify(rs, times(2)).next()
    verify(rs).close()
    verify(cs).close()
  }
}
