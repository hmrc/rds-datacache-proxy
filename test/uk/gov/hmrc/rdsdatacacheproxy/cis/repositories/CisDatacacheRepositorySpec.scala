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
import uk.gov.hmrc.rdsdatacacheproxy.cis.models.*

import java.sql.{CallableStatement, Connection, ResultSet, Types}
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
      scheme.accountOfficeReference mustBe "123PA12345678"
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

  "enqueueMessage" should {

    "enqueue message without tracking and return messageId" in {
      val messageId = 12345L
      val db = mock(classOf[Database])
      val conn = mock(classOf[java.sql.Connection])

      when(db.withTransaction(anyArg[Connection => Any])).thenAnswer { inv =>
        inv.getArgument(0, classOf[Connection => Any]).apply(conn)
      }

      val csHeader = mock(classOf[CallableStatement])
      val csClob = mock(classOf[CallableStatement])

      when(conn.prepareCall("{ call udas_queue.enqueue_message_header(?, ?, ?, ?, ?, ?) }")).thenReturn(csHeader)

      when(csHeader.getLong(6)).thenReturn(messageId)
      when(csHeader.wasNull()).thenReturn(false)

      when(conn.prepareCall("{ call udas_queue.enqueue_clob(?, ?, ?, ?, ?, ?, ?, ?, ?) }")).thenReturn(csClob)

      when(csClob.getLong(9)).thenReturn(messageId)
      when(csClob.wasNull()).thenReturn(false)

      val repo = new CisDatacacheRepository(db)

      val request = EnqueueMessageRequest(
        message = EnqueueMessage(
          sender        = "Portal",
          queueName     = "AGTAUTH",
          replyQueue    = "",
          correlationID = "",
          filter        = "UpdateAgentOwnReference",
          payload = Map(
            "IRAgentID"    -> "123456789",
            "Service"      -> "CIS",
            "TaxReference" -> "123/ABC123"
          )
        )
      )

      val out = repo.enqueueMessage(request).futureValue

      out mustBe messageId

      // Header
      verify(conn).prepareCall("{ call udas_queue.enqueue_message_header(?, ?, ?, ?, ?, ?) }")
      verify(csHeader).setString(1, "Portal")
      verify(csHeader).setString(2, "AGTAUTH")
      verify(csHeader).setString(3, "")
      verify(csHeader).setString(4, "")
      verify(csHeader).setString(5, "UpdateAgentOwnReference")
      verify(csHeader).registerOutParameter(6, Types.NUMERIC)
      verify(csHeader).execute()
      verify(csHeader).getLong(6)
      verify(csHeader).wasNull()

      // CLOBs
      verify(conn, times(3)).prepareCall("{ call udas_queue.enqueue_clob(?, ?, ?, ?, ?, ?, ?, ?, ?) }")

      verify(csClob, times(3)).setLong(1, messageId)
      verify(csClob, times(3)).setString(2, "Portal")
      verify(csClob, times(3)).setString(3, "AGTAUTH")
      verify(csClob, times(3)).setString(4, "")
      verify(csClob, times(3)).setString(5, "")
      verify(csClob, times(3)).setString(6, "UpdateAgentOwnReference")

      verify(csClob).setString(7, "IRAgentID")
      verify(csClob).setString(8, "123456789")
      verify(csClob).setString(7, "Service")
      verify(csClob).setString(8, "CIS")
      verify(csClob).setString(7, "TaxReference")
      verify(csClob).setString(8, "123/ABC123")

      verify(csClob, times(3)).registerOutParameter(9, Types.NUMERIC)
      verify(csClob, times(3)).execute()
      verify(csClob, times(3)).getLong(9)
      verify(csClob, times(3)).wasNull()

      verify(csHeader).close()
      verify(csClob, times(3)).close()
    }

    "enqueue message with tracking and return messageId" in {
      val messageId = 12345L
      val trackingMessageId = 54321L

      val db = mock(classOf[Database])
      val conn = mock(classOf[java.sql.Connection])

      when(db.withTransaction(anyArg[Connection => Any])).thenAnswer { inv =>
        inv.getArgument(0, classOf[Connection => Any]).apply(conn)
      }

      val csHeader = mock(classOf[CallableStatement])
      val csTrackingHeader = mock(classOf[CallableStatement])
      val csClob = mock(classOf[CallableStatement])
      val csNumber = mock(classOf[CallableStatement])

      when(conn.prepareCall("{ call udas_queue.enqueue_message_header(?, ?, ?, ?, ?, ?) }")).thenReturn(csHeader).thenReturn(csTrackingHeader)

      when(csHeader.getLong(6)).thenReturn(messageId)
      when(csHeader.wasNull()).thenReturn(false)

      when(csTrackingHeader.getLong(6)).thenReturn(trackingMessageId)
      when(csTrackingHeader.wasNull()).thenReturn(false)

      when(conn.prepareCall("{ call udas_queue.enqueue_clob(?, ?, ?, ?, ?, ?, ?, ?, ?) }")).thenReturn(csClob)

      when(csClob.getLong(9)).thenReturn(
        messageId,
        messageId,
        messageId,
        trackingMessageId,
        trackingMessageId,
        trackingMessageId,
        trackingMessageId,
        trackingMessageId,
        trackingMessageId,
        trackingMessageId
      )

      when(csClob.wasNull()).thenReturn(false)

      when(conn.prepareCall("{ call udas_queue.enqueue_number(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }")).thenReturn(csNumber)

      when(csNumber.getLong(10)).thenReturn(trackingMessageId)
      when(csNumber.wasNull()).thenReturn(false)

      val repo = new CisDatacacheRepository(db)

      val request = EnqueueMessageRequest(
        message = EnqueueMessage(
          sender        = "Portal",
          queueName     = "AGTAUTH",
          replyQueue    = "",
          correlationID = "",
          filter        = "RemoveClient",
          payload = Map(
            "IRAgentID"    -> "123456789",
            "Service"      -> "CIS",
            "TaxReference" -> "123/ABC123"
          )
        ),
        tracking = Some(
          EnqueueTracking(
            message = EnqueueMessage(
              sender        = "Portal",
              queueName     = "Tracking",
              replyQueue    = "",
              correlationID = "",
              filter        = "AGENTAUTH",
              payload = Map(
                "GGIS_DTSTAMP"    -> "20260826",
                "MESSAGE_TYPE"    -> "AGENT_AUTH_PORTAL",
                "ADDITIONAL_INFO" -> "Request client removal",
                "GW_AGENT_ID"     -> "AGENT123",
                "IR_CLIENT_REF"   -> "123/ABC123",
                "USER_ID"         -> "user123",
                "Service"         -> "CIS"
              )
            ),
            number = EnqueueNumber(
              dataType = 1,
              payload = Map(
                "EVENT_TYPE" -> 1010L
              )
            )
          )
        )
      )

      val out = repo.enqueueMessage(request).futureValue

      out mustBe messageId

      // Main header
      verify(csHeader).setString(1, "Portal")
      verify(csHeader).setString(2, "AGTAUTH")
      verify(csHeader).setString(5, "RemoveClient")
      verify(csHeader).registerOutParameter(6, Types.NUMERIC)
      verify(csHeader).execute()
      verify(csHeader).getLong(6)
      verify(csHeader).wasNull()

      // Tracking header
      verify(csTrackingHeader).setString(1, "Portal")
      verify(csTrackingHeader).setString(2, "Tracking")
      verify(csTrackingHeader).setString(5, "AGENTAUTH")
      verify(csTrackingHeader).registerOutParameter(6, Types.NUMERIC)
      verify(csTrackingHeader).execute()
      verify(csTrackingHeader).getLong(6)
      verify(csTrackingHeader).wasNull()

      // 3 main CLOBs + 7 tracking CLOBs
      verify(csClob, times(10)).execute()
      verify(csClob, times(10)).getLong(9)
      verify(csClob, times(10)).wasNull()
      verify(csClob, times(10)).registerOutParameter(9, Types.NUMERIC)

      // Tracking CLOBs must use tracking message ID
      verify(csClob, times(7)).setLong(1, trackingMessageId)

      // Tracking number
      verify(csNumber).setLong(1, trackingMessageId)
      verify(csNumber).setString(2, "Portal")
      verify(csNumber).setString(3, "Tracking")
      verify(csNumber).setString(4, "")
      verify(csNumber).setString(5, "")
      verify(csNumber).setString(6, "AGENTAUTH")
      verify(csNumber).setString(7, "EVENT_TYPE")
      verify(csNumber).setInt(8, 1)
      verify(csNumber).setLong(9, 1010L)
      verify(csNumber).registerOutParameter(10, Types.NUMERIC)
      verify(csNumber).execute()
      verify(csNumber).getLong(10)
      verify(csNumber).wasNull()

      verify(csHeader).close()
      verify(csTrackingHeader).close()
      verify(csClob, times(10)).close()
      verify(csNumber).close()
    }

    "fail when enqueueMessageHeader returns a negative messageId" in {
      val db = mock(classOf[Database])
      val conn = mock(classOf[java.sql.Connection])
      val csHeader = mock(classOf[CallableStatement])

      when(db.withTransaction(anyArg[Connection => Any])).thenAnswer { inv =>
        inv.getArgument(0, classOf[Connection => Any]).apply(conn)
      }

      when(conn.prepareCall("{ call udas_queue.enqueue_message_header(?, ?, ?, ?, ?, ?) }")).thenReturn(csHeader)

      when(csHeader.getLong(6)).thenReturn(-1L)
      when(csHeader.wasNull()).thenReturn(false)

      val repo = new CisDatacacheRepository(db)

      val request = EnqueueMessageRequest(
        message = EnqueueMessage(
          sender        = "Portal",
          queueName     = "AGTAUTH",
          replyQueue    = "",
          correlationID = "",
          filter        = "RemoveClient",
          payload = Map(
            "IRAgentID" -> "123456789"
          )
        )
      )

      val result = repo.enqueueMessage(request).failed.futureValue

      result mustBe a[RuntimeException]
      result.getMessage mustBe "Failed to callEnqueueMessageHeader: sender=Portal, queueName=AGTAUTH, filter=RemoveClient"

      verify(csHeader).wasNull()
      verify(csHeader).close()
    }

    "fail when enqueueClob returns a negative messageIdOut" in {
      val messageId = 12345L

      val db = mock(classOf[Database])
      val conn = mock(classOf[java.sql.Connection])

      val csHeader = mock(classOf[CallableStatement])
      val csClob = mock(classOf[CallableStatement])

      when(db.withTransaction(anyArg[Connection => Any])).thenAnswer { inv =>
        inv.getArgument(0, classOf[Connection => Any]).apply(conn)
      }

      when(conn.prepareCall("{ call udas_queue.enqueue_message_header(?, ?, ?, ?, ?, ?) }")).thenReturn(csHeader)

      when(csHeader.getLong(6)).thenReturn(messageId)
      when(csHeader.wasNull()).thenReturn(false)

      when(conn.prepareCall("{ call udas_queue.enqueue_clob(?, ?, ?, ?, ?, ?, ?, ?, ?) }")).thenReturn(csClob)

      when(csClob.getLong(9)).thenReturn(-1L)
      when(csClob.wasNull()).thenReturn(false)

      val repo = new CisDatacacheRepository(db)

      val request = EnqueueMessageRequest(
        message = EnqueueMessage(
          sender        = "Portal",
          queueName     = "AGTAUTH",
          replyQueue    = "",
          correlationID = "",
          filter        = "RemoveClient",
          payload = Map(
            "IRAgentID" -> "123456789"
          )
        )
      )

      val result = repo.enqueueMessage(request).failed.futureValue

      result mustBe a[RuntimeException]
      result.getMessage mustBe "Failed to callEnqueueClob: messageID=12345, messageIDOut=-1, key=IRAgentID"

      verify(csHeader).close()
      verify(csClob).close()
    }

    "fail when enqueueNumber returns a negative messageIdOut" in {
      val messageId = 12345L
      val trackingMessageId = 54321L

      val db = mock(classOf[Database])
      val conn = mock(classOf[java.sql.Connection])

      when(db.withTransaction(anyArg[Connection => Any])).thenAnswer { inv =>
        inv.getArgument(0, classOf[Connection => Any]).apply(conn)
      }

      val csHeader = mock(classOf[CallableStatement])
      val csTrackingHeader = mock(classOf[CallableStatement])
      val csClob = mock(classOf[CallableStatement])
      val csNumber = mock(classOf[CallableStatement])

      when(conn.prepareCall("{ call udas_queue.enqueue_message_header(?, ?, ?, ?, ?, ?) }")).thenReturn(csHeader).thenReturn(csTrackingHeader)

      when(csHeader.getLong(6)).thenReturn(messageId)
      when(csHeader.wasNull()).thenReturn(false)

      when(csTrackingHeader.getLong(6)).thenReturn(trackingMessageId)
      when(csTrackingHeader.wasNull()).thenReturn(false)

      when(conn.prepareCall("{ call udas_queue.enqueue_clob(?, ?, ?, ?, ?, ?, ?, ?, ?) }")).thenReturn(csClob)

      when(csClob.getLong(9)).thenReturn(
        messageId,
        messageId,
        messageId,
        trackingMessageId,
        trackingMessageId,
        trackingMessageId,
        trackingMessageId,
        trackingMessageId,
        trackingMessageId,
        trackingMessageId
      )

      when(csClob.wasNull()).thenReturn(false)

      when(conn.prepareCall("{ call udas_queue.enqueue_number(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }")).thenReturn(csNumber)

      when(csNumber.getLong(10)).thenReturn(-1L)
      when(csNumber.wasNull()).thenReturn(false)

      val repo = new CisDatacacheRepository(db)

      val request = EnqueueMessageRequest(
        message = EnqueueMessage(
          sender        = "Portal",
          queueName     = "AGTAUTH",
          replyQueue    = "",
          correlationID = "",
          filter        = "RemoveClient",
          payload = Map(
            "IRAgentID"    -> "123456789",
            "Service"      -> "CIS",
            "TaxReference" -> "123/ABC123"
          )
        ),
        tracking = Some(
          EnqueueTracking(
            message = EnqueueMessage(
              sender        = "Portal",
              queueName     = "Tracking",
              replyQueue    = "",
              correlationID = "",
              filter        = "AGENTAUTH",
              payload = Map(
                "GGIS_DTSTAMP"    -> "20260826",
                "MESSAGE_TYPE"    -> "AGENT_AUTH_PORTAL",
                "ADDITIONAL_INFO" -> "Request client removal",
                "GW_AGENT_ID"     -> "AGENT123",
                "IR_CLIENT_REF"   -> "123/ABC123",
                "USER_ID"         -> "user123",
                "Service"         -> "CIS"
              )
            ),
            number = EnqueueNumber(
              dataType = 1,
              payload = Map(
                "EVENT_TYPE" -> 1010L
              )
            )
          )
        )
      )

      val result = repo.enqueueMessage(request).failed.futureValue

      result mustBe a[RuntimeException]
      result.getMessage mustBe "Failed to callEnqueueNumber: messageID=54321, messageIDOut=-1, key=EVENT_TYPE"

      // Main header
      verify(csHeader).setString(1, "Portal")
      verify(csHeader).setString(2, "AGTAUTH")
      verify(csHeader).setString(5, "RemoveClient")
      verify(csHeader).registerOutParameter(6, Types.NUMERIC)
      verify(csHeader).execute()
      verify(csHeader).getLong(6)
      verify(csHeader).wasNull()

      // Tracking header
      verify(csTrackingHeader).setString(1, "Portal")
      verify(csTrackingHeader).setString(2, "Tracking")
      verify(csTrackingHeader).setString(5, "AGENTAUTH")
      verify(csTrackingHeader).registerOutParameter(6, Types.NUMERIC)
      verify(csTrackingHeader).execute()
      verify(csTrackingHeader).getLong(6)
      verify(csTrackingHeader).wasNull()

      // 3 main CLOBs + 7 tracking CLOBs
      verify(csClob, times(10)).execute()
      verify(csClob, times(10)).getLong(9)
      verify(csClob, times(10)).wasNull()
      verify(csClob, times(10)).registerOutParameter(9, Types.NUMERIC)

      // Tracking CLOBs must use tracking message ID
      verify(csClob, times(7)).setLong(1, trackingMessageId)

      // Tracking number
      verify(csNumber).setLong(1, trackingMessageId)
      verify(csNumber).setString(2, "Portal")
      verify(csNumber).setString(3, "Tracking")
      verify(csNumber).setString(4, "")
      verify(csNumber).setString(5, "")
      verify(csNumber).setString(6, "AGENTAUTH")
      verify(csNumber).setString(7, "EVENT_TYPE")
      verify(csNumber).setInt(8, 1)
      verify(csNumber).setLong(9, 1010L)
      verify(csNumber).registerOutParameter(10, Types.NUMERIC)
      verify(csNumber).execute()
      verify(csNumber).getLong(10)
      verify(csNumber).wasNull()

      verify(csHeader).close()
      verify(csTrackingHeader).close()
      verify(csClob, times(10)).close()
      verify(csNumber).close()
    }

    "fail when enqueueNumber returns a different messageIdOut" in {
      val messageId = 12345L
      val trackingMessageId = 54321L

      val db = mock(classOf[Database])
      val conn = mock(classOf[java.sql.Connection])

      when(db.withTransaction(anyArg[Connection => Any])).thenAnswer { inv =>
        inv.getArgument(0, classOf[Connection => Any]).apply(conn)
      }

      val csHeader = mock(classOf[CallableStatement])
      val csTrackingHeader = mock(classOf[CallableStatement])
      val csClob = mock(classOf[CallableStatement])
      val csNumber = mock(classOf[CallableStatement])

      when(conn.prepareCall("{ call udas_queue.enqueue_message_header(?, ?, ?, ?, ?, ?) }")).thenReturn(csHeader).thenReturn(csTrackingHeader)

      when(csHeader.getLong(6)).thenReturn(messageId)
      when(csHeader.wasNull()).thenReturn(false)

      when(csTrackingHeader.getLong(6)).thenReturn(trackingMessageId)
      when(csTrackingHeader.wasNull()).thenReturn(false)

      when(conn.prepareCall("{ call udas_queue.enqueue_clob(?, ?, ?, ?, ?, ?, ?, ?, ?) }")).thenReturn(csClob)

      when(csClob.getLong(9)).thenReturn(
        messageId,
        messageId,
        messageId,
        trackingMessageId,
        trackingMessageId,
        trackingMessageId,
        trackingMessageId,
        trackingMessageId,
        trackingMessageId,
        trackingMessageId
      )

      when(csClob.wasNull()).thenReturn(false)

      when(conn.prepareCall("{ call udas_queue.enqueue_number(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }")).thenReturn(csNumber)

      when(csNumber.getLong(10)).thenReturn(150L)
      when(csNumber.wasNull()).thenReturn(false)

      val repo = new CisDatacacheRepository(db)

      val request = EnqueueMessageRequest(
        message = EnqueueMessage(
          sender        = "Portal",
          queueName     = "AGTAUTH",
          replyQueue    = "",
          correlationID = "",
          filter        = "RemoveClient",
          payload = Map(
            "IRAgentID"    -> "123456789",
            "Service"      -> "CIS",
            "TaxReference" -> "123/ABC123"
          )
        ),
        tracking = Some(
          EnqueueTracking(
            message = EnqueueMessage(
              sender        = "Portal",
              queueName     = "Tracking",
              replyQueue    = "",
              correlationID = "",
              filter        = "AGENTAUTH",
              payload = Map(
                "GGIS_DTSTAMP"    -> "20260826",
                "MESSAGE_TYPE"    -> "AGENT_AUTH_PORTAL",
                "ADDITIONAL_INFO" -> "Request client removal",
                "GW_AGENT_ID"     -> "AGENT123",
                "IR_CLIENT_REF"   -> "123/ABC123",
                "USER_ID"         -> "user123",
                "Service"         -> "CIS"
              )
            ),
            number = EnqueueNumber(
              dataType = 1,
              payload = Map(
                "EVENT_TYPE" -> 1010L
              )
            )
          )
        )
      )

      val result = repo.enqueueMessage(request).failed.futureValue

      result mustBe a[RuntimeException]
      result.getMessage mustBe
        "Failed to callEnqueueNumber: messageID=54321, messageIDOut=150, key=EVENT_TYPE"

      // Main header
      verify(csHeader).setString(1, "Portal")
      verify(csHeader).setString(2, "AGTAUTH")
      verify(csHeader).setString(5, "RemoveClient")
      verify(csHeader).registerOutParameter(6, Types.NUMERIC)
      verify(csHeader).execute()
      verify(csHeader).getLong(6)
      verify(csHeader).wasNull()

      // Tracking header
      verify(csTrackingHeader).setString(1, "Portal")
      verify(csTrackingHeader).setString(2, "Tracking")
      verify(csTrackingHeader).setString(5, "AGENTAUTH")
      verify(csTrackingHeader).registerOutParameter(6, Types.NUMERIC)
      verify(csTrackingHeader).execute()
      verify(csTrackingHeader).getLong(6)
      verify(csTrackingHeader).wasNull()

      // 3 main CLOBs + 7 tracking CLOBs
      verify(csClob, times(10)).execute()
      verify(csClob, times(10)).getLong(9)
      verify(csClob, times(10)).wasNull()
      verify(csClob, times(10)).registerOutParameter(9, Types.NUMERIC)

      // Tracking CLOBs must use tracking message ID
      verify(csClob, times(7)).setLong(1, trackingMessageId)

      // Tracking number
      verify(csNumber).setLong(1, trackingMessageId)
      verify(csNumber).setString(2, "Portal")
      verify(csNumber).setString(3, "Tracking")
      verify(csNumber).setString(4, "")
      verify(csNumber).setString(5, "")
      verify(csNumber).setString(6, "AGENTAUTH")
      verify(csNumber).setString(7, "EVENT_TYPE")
      verify(csNumber).setInt(8, 1)
      verify(csNumber).setLong(9, 1010L)
      verify(csNumber).registerOutParameter(10, Types.NUMERIC)
      verify(csNumber).execute()
      verify(csNumber).getLong(10)
      verify(csNumber).wasNull()

      verify(csHeader).close()
      verify(csTrackingHeader).close()
      verify(csClob, times(10)).close()
      verify(csNumber).close()
    }
  }
}
