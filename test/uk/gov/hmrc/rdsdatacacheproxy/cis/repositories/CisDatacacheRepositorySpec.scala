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

package uk.gov.hmrc.rdsdatacacheproxy.cis.repositories

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

  "getClientListDownloadStatus" should {
    "return 1 status code when stored procedure executes successfully" in {
      val db = mock(classOf[Database])
      val conn = mock(classOf[java.sql.Connection])
      val cs = mock(classOf[CallableStatement])

      when(db.withConnection(anyArg())).thenAnswer { inv =>
        val f = inv.getArgument(0, classOf[java.sql.Connection => Any])
        f(conn)
      }
      when(conn.prepareCall(anyArg[String])).thenReturn(cs)
      when(cs.getInt(4)).thenReturn(1)

      val repo = new CisDatacacheRepository(db)

      val result = repo.getClientListDownloadStatus("cred123", "cis", 14400).futureValue
      result mustBe 1

      verify(conn).prepareCall("{ call CLIENT_LIST_STATUS.GETCLIENTLISTDOWNLOADSTATUS(?, ?, ?, ?) }")
      verify(cs).setString(1, "cred123")
      verify(cs).setString(2, "cis")
      verify(cs).setInt(3, 14400)
      verify(cs).registerOutParameter(4, oracle.jdbc.OracleTypes.INTEGER)
      verify(cs).execute()
      verify(cs).close()
    }

    "return 0 status code when stored procedure executes successfully" in {
      val db = mock(classOf[Database])
      val conn = mock(classOf[java.sql.Connection])
      val cs = mock(classOf[CallableStatement])

      when(db.withConnection(anyArg())).thenAnswer { inv =>
        val f = inv.getArgument(0, classOf[java.sql.Connection => Any])
        f(conn)
      }
      when(conn.prepareCall(anyArg[String])).thenReturn(cs)
      when(cs.getInt(4)).thenReturn(0)

      val repo = new CisDatacacheRepository(db)

      val result = repo.getClientListDownloadStatus("cred456", "cis", 7200).futureValue
      result mustBe 0

      verify(conn).prepareCall("{ call CLIENT_LIST_STATUS.GETCLIENTLISTDOWNLOADSTATUS(?, ?, ?, ?) }")
      verify(cs).setString(1, "cred456")
      verify(cs).setString(2, "cis")
      verify(cs).setInt(3, 7200)
      verify(cs).registerOutParameter(4, oracle.jdbc.OracleTypes.INTEGER)
      verify(cs).execute()
      verify(cs).close()
    }

    "use custom grace period when provided" in {
      val db = mock(classOf[Database])
      val conn = mock(classOf[java.sql.Connection])
      val cs = mock(classOf[CallableStatement])

      when(db.withConnection(anyArg())).thenAnswer { inv =>
        val f = inv.getArgument(0, classOf[java.sql.Connection => Any])
        f(conn)
      }
      when(conn.prepareCall(anyArg[String])).thenReturn(cs)
      when(cs.getInt(4)).thenReturn(2)

      val repo = new CisDatacacheRepository(db)

      val customGracePeriod = 3600
      val result = repo.getClientListDownloadStatus("cred789", "cis", customGracePeriod).futureValue
      result mustBe 2

      verify(cs).setInt(3, customGracePeriod)
      verify(cs).close()
    }
  }
  "getSchemePrepopByKnownFacts" should {

    "return None when p_response is non-zero" in {
      val db = mock(classOf[Database])
      val conn = mock(classOf[java.sql.Connection])
      val cs = mock(classOf[CallableStatement])

      when(db.withConnection(anyArg())).thenAnswer { inv =>
        val f = inv.getArgument(0, classOf[java.sql.Connection => Any])
        f(conn)
      }
      when(conn.prepareCall(anyArg[String])).thenReturn(cs)
      when(cs.getInt(4)).thenReturn(-1)

      val repo = new CisDatacacheRepository(db)

      val out = repo
        .getSchemePrepopByKnownFacts("123", "AB456", "123PA12345678")
        .futureValue

      out mustBe None

      verify(conn).prepareCall(
        "{ call CISR_PREPOP_PORTAL_PK.getSchemePrepopByKnownFacts(?, ?, ?, ?, ?) }"
      )
      verify(cs).execute()
      verify(cs).close()
    }

    "return Some(SchemePrepop) on one-row cursor when p_response = 0" in {
      val db = mock(classOf[Database])
      val conn = mock(classOf[java.sql.Connection])
      val cs = mock(classOf[CallableStatement])
      val rs = mock(classOf[ResultSet])

      when(db.withConnection(anyArg())).thenAnswer { inv =>
        val f = inv.getArgument(0, classOf[java.sql.Connection => Any])
        f(conn)
      }
      when(conn.prepareCall(anyArg[String])).thenReturn(cs)

      when(cs.getInt(4)).thenReturn(0)
      when(cs.getObject(eqTo(5), eqTo(classOf[ResultSet]))).thenReturn(rs)

      when(rs.next()).thenReturn(true, false)
      when(rs.getString("TAX_OFFICE_NUMBER")).thenReturn(" 123 ")
      when(rs.getString("TAX_OFFICE_REF")).thenReturn(" AB456 ")
      when(rs.getString("AO_REF")).thenReturn(" 123PA12345678 ")
      when(rs.getString("UTR")).thenReturn(" 1123456789 ")
      when(rs.getString("SCHEME_NAME")).thenReturn(" PAL-355 Scheme ")

      val repo = new CisDatacacheRepository(db)

      val out = repo.getSchemePrepopByKnownFacts("123", "AB456", "123PA12345678").futureValue
      val scheme = out.value

      scheme.taxOfficeNumber mustBe "123"
      scheme.taxOfficeReference mustBe "AB456"
      scheme.agentOwnReference mustBe "123PA12345678"
      scheme.utr mustBe Some("1123456789")
      scheme.schemeName mustBe "PAL-355 Scheme"

      verify(conn).prepareCall(
        "{ call CISR_PREPOP_PORTAL_PK.getSchemePrepopByKnownFacts(?, ?, ?, ?, ?) }"
      )
      verify(cs).execute()
      verify(rs).close()
      verify(cs).close()
    }

    "throw IllegalStateException on multiple rows in cursor" in {
      val db = mock(classOf[Database])
      val conn = mock(classOf[java.sql.Connection])
      val cs = mock(classOf[CallableStatement])
      val rs = mock(classOf[ResultSet])

      when(db.withConnection(anyArg())).thenAnswer { inv =>
        val f = inv.getArgument(0, classOf[java.sql.Connection => Any])
        f(conn)
      }
      when(conn.prepareCall(anyArg[String])).thenReturn(cs)

      when(cs.getInt(4)).thenReturn(0)
      when(cs.getObject(eqTo(5), eqTo(classOf[ResultSet]))).thenReturn(rs)

      when(rs.next()).thenReturn(true, true, false)
      when(rs.getString("TAX_OFFICE_NUMBER")).thenReturn("123")
      when(rs.getString("TAX_OFFICE_REF")).thenReturn("AB456")
      when(rs.getString("AO_REF")).thenReturn("123PA12345678")
      when(rs.getString("UTR")).thenReturn("1123456789")
      when(rs.getString("SCHEME_NAME")).thenReturn("PAL-355 Scheme")

      val repo = new CisDatacacheRepository(db)

      val ex =
        repo
          .getSchemePrepopByKnownFacts("123", "AB456", "123PA12345678")
          .failed
          .futureValue

      ex mustBe a[IllegalStateException]

      verify(conn).prepareCall(
        "{ call CISR_PREPOP_PORTAL_PK.getSchemePrepopByKnownFacts(?, ?, ?, ?, ?) }"
      )
      verify(cs).execute()
      verify(rs).close()
      verify(cs).close()
    }
  }

  "getSubcontractorsPrepopByKnownFacts" should {

    "return empty Seq when p_response is non-zero" in {
      val db = mock(classOf[Database])
      val conn = mock(classOf[java.sql.Connection])
      val cs = mock(classOf[CallableStatement])

      when(db.withConnection(anyArg())).thenAnswer { inv =>
        val f = inv.getArgument(0, classOf[java.sql.Connection => Any])
        f(conn)
      }
      when(conn.prepareCall(anyArg[String])).thenReturn(cs)

      when(cs.getInt(4)).thenReturn(-1)

      val repo = new CisDatacacheRepository(db)

      val out = repo
        .getSubcontractorsPrepopByKnownFacts("123", "AB456", "123PA12345678")
        .futureValue

      out mustBe empty

      verify(conn).prepareCall(
        "{ call CISR_PREPOP_PORTAL_PK.getSubcontrsPrepopByKnownFacts(?, ?, ?, ?, ?) }"
      )
      verify(cs).execute()
      verify(cs).close()
    }

    "return Seq with one SubcontractorPrepopRecord on one-row cursor when p_response = 0" in {
      val db = mock(classOf[Database])
      val conn = mock(classOf[java.sql.Connection])
      val cs = mock(classOf[CallableStatement])
      val rs = mock(classOf[ResultSet])

      when(db.withConnection(anyArg())).thenAnswer { inv =>
        val f = inv.getArgument(0, classOf[java.sql.Connection => Any])
        f(conn)
      }
      when(conn.prepareCall(anyArg[String])).thenReturn(cs)

      when(cs.getInt(4)).thenReturn(0)
      when(cs.getObject(eqTo(5), eqTo(classOf[ResultSet]))).thenReturn(rs)

      when(rs.next()).thenReturn(true, false)

      when(rs.getString("SUBCONTRACTOR_TYPE")).thenReturn(" I ")
      when(rs.getString("SUBCONTRACTOR_UTR")).thenReturn(" 1123456789 ")
      when(rs.getString("VERIFICATION_NUMBER")).thenReturn(" 12345678901 ")
      when(rs.getString("VERIFICATION_SUFFIX")).thenReturn(" AB ")
      when(rs.getString("TITLE")).thenReturn(" Mr ")
      when(rs.getString("FIRST_NAME")).thenReturn(" Test ")
      when(rs.getString("SECOND_NAME")).thenReturn(null)
      when(rs.getString("SURNAME")).thenReturn(" Builder ")
      when(rs.getString("TRADING_NAME")).thenReturn(" Test Ltd ")

      val repo = new CisDatacacheRepository(db)

      val out = repo
        .getSubcontractorsPrepopByKnownFacts("123", "AB456", "123PA12345678")
        .futureValue

      out must have size 1
      val sub = out.head

      sub.subcontractorType mustBe "I"
      sub.subcontractorUtr mustBe "1123456789"
      sub.verificationNumber mustBe "12345678901"
      sub.verificationSuffix mustBe Some("AB")
      sub.title mustBe Some("Mr")
      sub.firstName mustBe Some("Test")
      sub.secondName mustBe None
      sub.surname mustBe Some("Builder")
      sub.tradingName mustBe Some("Test Ltd")

      verify(conn).prepareCall(
        "{ call CISR_PREPOP_PORTAL_PK.getSubcontrsPrepopByKnownFacts(?, ?, ?, ?, ?) }"
      )
      verify(cs).execute()
      verify(rs).close()
      verify(cs).close()
    }

    "return Seq with multiple SubcontractorPrepopRecord when cursor has multiple rows" in {
      val db = mock(classOf[Database])
      val conn = mock(classOf[java.sql.Connection])
      val cs = mock(classOf[CallableStatement])
      val rs = mock(classOf[ResultSet])

      when(db.withConnection(anyArg())).thenAnswer { inv =>
        val f = inv.getArgument(0, classOf[java.sql.Connection => Any])
        f(conn)
      }
      when(conn.prepareCall(anyArg[String])).thenReturn(cs)

      when(cs.getInt(4)).thenReturn(0)
      when(cs.getObject(eqTo(5), eqTo(classOf[ResultSet]))).thenReturn(rs)

      when(rs.next()).thenReturn(true, true, false)

      when(rs.getString("SUBCONTRACTOR_TYPE")).thenReturn("I", "O")
      when(rs.getString("SUBCONTRACTOR_UTR")).thenReturn("1123456789", "2234567890")
      when(rs.getString("VERIFICATION_NUMBER")).thenReturn("12345678901", "22345678901")
      when(rs.getString("VERIFICATION_SUFFIX")).thenReturn("AB", null)
      when(rs.getString("TITLE")).thenReturn("Mr", "Ms")
      when(rs.getString("FIRST_NAME")).thenReturn("Test", "First")
      when(rs.getString("SECOND_NAME")).thenReturn(null, "Second")
      when(rs.getString("SURNAME")).thenReturn("Builder", "Surname")
      when(rs.getString("TRADING_NAME")).thenReturn("Test Ltd", null)

      val repo = new CisDatacacheRepository(db)

      val out =
        repo
          .getSubcontractorsPrepopByKnownFacts("123", "AB456", "123PA12345678")
          .futureValue

      out must have size 2

      val first = out.head
      val second = out(1)

      first.subcontractorType mustBe "I"
      first.subcontractorUtr mustBe "1123456789"
      first.verificationNumber mustBe "12345678901"
      first.verificationSuffix mustBe Some("AB")
      first.title mustBe Some("Mr")
      first.firstName mustBe Some("Test")
      first.secondName mustBe None
      first.surname mustBe Some("Builder")
      first.tradingName mustBe Some("Test Ltd")

      second.subcontractorType mustBe "O"
      second.subcontractorUtr mustBe "2234567890"
      second.verificationNumber mustBe "22345678901"
      second.verificationSuffix mustBe None
      second.title mustBe Some("Ms")
      second.firstName mustBe Some("First")
      second.secondName mustBe Some("Second")
      second.surname mustBe Some("Surname")
      second.tradingName mustBe None

      verify(conn).prepareCall(
        "{ call CISR_PREPOP_PORTAL_PK.getSubcontrsPrepopByKnownFacts(?, ?, ?, ?, ?) }"
      )
      verify(cs).execute()
      verify(rs).close()
      verify(cs).close()
    }
  }
}
