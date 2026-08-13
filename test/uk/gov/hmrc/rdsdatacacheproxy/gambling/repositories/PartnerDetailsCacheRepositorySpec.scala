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
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.*
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.Regime.GBD
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponsePartnerDetails

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class PartnerDetailsCacheRepositorySpec extends AnyWordSpec with Matchers with BeforeAndAfter {
  private val gtrDb: GTRDatabase = mock(classOf[Database]).asInstanceOf[GTRDatabase]
  private val mgdDb: MGDDatabase = mock(classOf[Database]).asInstanceOf[MGDDatabase]
  private val mgdMockConnection: Connection = mock(classOf[Connection])
  private val gtrMockConnection: Connection = mock(classOf[Connection])
  private val mockCsMgd: CallableStatement = mock(classOf[CallableStatement])

  private val partnerDetailsResultSet: ResultSet = mock(classOf[ResultSet])

  private val repository: PartnerDetailsCacheRepository = new PartnerDetailsCacheRepository(
    mgdDb = mgdDb,
    gtrDb = gtrDb
  )

  before {
    Mockito.reset(mgdDb, gtrDb, mgdMockConnection, gtrMockConnection, mockCsMgd, partnerDetailsResultSet)

    when(mgdDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mgdMockConnection)
    }

    when(gtrDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(gtrMockConnection)
    }

    when(mgdMockConnection.prepareCall(any[String])).thenReturn(mockCsMgd)
  }

  "getPartnerDetails" should {
    "return PartnerDetails when regime is MGD and stored procedure returns data" in {
      val regNumber = "XWM12345678901"

      when(mockCsMgd.getObject(2)).thenReturn(partnerDetailsResultSet)
      when(mockCsMgd.getDate(3)).thenReturn(Date.valueOf("2026-7-30"))

      when(partnerDetailsResultSet.next()).thenReturn(true, false)

      when(partnerDetailsResultSet.getString("mgd_reg_number")).thenReturn("XWM12345678901")
      when(partnerDetailsResultSet.getString("BUSINESS_PARTNER_NUMBER")).thenReturn("0100049899")
      when(partnerDetailsResultSet.getDate("DATE_OF_JOINING")).thenReturn(Date.valueOf("2025-01-01"))
      when(partnerDetailsResultSet.getString("DATE_OF_LEAVING")).thenReturn("2026-01-01")
      when(partnerDetailsResultSet.getString("SOLE_PROP_TITLE")).thenReturn("Ms")
      when(partnerDetailsResultSet.getString("SOLE_PROP_FIRST_NAME")).thenReturn("Amelia")
      when(partnerDetailsResultSet.getString("SOLE_PROP_MIDDLE_NAME")).thenReturn("Rose")
      when(partnerDetailsResultSet.getString("SOLE_PROP_LAST_NAME")).thenReturn("Hartley")
      when(partnerDetailsResultSet.getString("BUSINESS_NAME")).thenReturn("Hartley Financial Services")
      when(partnerDetailsResultSet.getString("TRADING_NAME")).thenReturn("Hartley Advisory")
      when(partnerDetailsResultSet.getDate("DATE_OF_BIRTH")).thenReturn(Date.valueOf("1986-9-22"))
      when(partnerDetailsResultSet.getString("NINO")).thenReturn("QQ123456C")
      when(partnerDetailsResultSet.getString("UTR")).thenReturn("1234567890")
      when(partnerDetailsResultSet.getString("VRN")).thenReturn("GB123456789")
      when(partnerDetailsResultSet.getString("CRN")).thenReturn("09876543")
      when(partnerDetailsResultSet.getDate("DATE_OF_INCORPORATION")).thenReturn(Date.valueOf("2022-11-1"))
      when(partnerDetailsResultSet.getString("COUNTRY_OF_INCORPORATION")).thenReturn("United Kingdom")
      when(partnerDetailsResultSet.getString("FOREIGN_CORPORATE_REF")).thenReturn("FCR-UK-987654")
      when(partnerDetailsResultSet.getString("ADDRESS_1")).thenReturn("42 Mockingbird Lane")
      when(partnerDetailsResultSet.getString("ADDRESS_2")).thenReturn("Suite 5")
      when(partnerDetailsResultSet.getString("ADDRESS_3")).thenReturn("Westbridge Business Park")
      when(partnerDetailsResultSet.getString("ADDRESS_4")).thenReturn("Bristol")
      when(partnerDetailsResultSet.getString("POSTCODE")).thenReturn("BS1 4AB")
      when(partnerDetailsResultSet.getString("COUNTRY")).thenReturn("United Kingdom")
      when(partnerDetailsResultSet.getString("ADI")).thenReturn("ADI-123456")
      when(partnerDetailsResultSet.getString("IOM_OR_CI_FLAG")).thenReturn("N")
      when(partnerDetailsResultSet.getString("PHONE_NUMBER")).thenReturn("0117 555 1234")
      when(partnerDetailsResultSet.getString("MOBILE_PHONE_NUMBER")).thenReturn("07700 900123")
      when(partnerDetailsResultSet.getString("FAX_NUMBER")).thenReturn("0117 555 5678")
      when(partnerDetailsResultSet.getString("EMAIL_ADDR")).thenReturn("amelia.hartley@example.test")
      when(
        partnerDetailsResultSet.getObject(
          "IS_FUTURE_LEAVE_DATE",
          classOf[java.lang.Integer]
        )
      ).thenReturn(Integer.valueOf(1))
      when(
        partnerDetailsResultSet.getObject(
          "IS_FUTURE_JOIN_DATE",
          classOf[java.lang.Integer]
        )
      ).thenReturn(Integer.valueOf(0))

      when(
        partnerDetailsResultSet.getObject(
          "BUSINESS_TYPE",
          classOf[java.lang.Integer]
        )
      ).thenReturn(Integer.valueOf(2))

      val result = repository.getPartnerDetails(Regime.MGD, regNumber).futureValue

      result               shouldBe validResponsePartnerDetails
      result.partners.size shouldBe 1

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
      verify(mockCsMgd).registerOutParameter(3, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).execute()

      verify(partnerDetailsResultSet, times(2)).next()

      verify(partnerDetailsResultSet).getString("mgd_reg_number")
      verify(partnerDetailsResultSet).getString("BUSINESS_PARTNER_NUMBER")
      verify(partnerDetailsResultSet).getDate("DATE_OF_JOINING")
      verify(partnerDetailsResultSet).getString("DATE_OF_LEAVING")
      verify(partnerDetailsResultSet).getString("SOLE_PROP_TITLE")
      verify(partnerDetailsResultSet).getString("SOLE_PROP_FIRST_NAME")
      verify(partnerDetailsResultSet).getString("SOLE_PROP_MIDDLE_NAME")
      verify(partnerDetailsResultSet).getString("SOLE_PROP_LAST_NAME")
      verify(partnerDetailsResultSet).getString("BUSINESS_NAME")
      verify(partnerDetailsResultSet).getString("TRADING_NAME")
      verify(partnerDetailsResultSet).getDate("DATE_OF_BIRTH")
      verify(partnerDetailsResultSet).getString("NINO")
      verify(partnerDetailsResultSet).getString("UTR")
      verify(partnerDetailsResultSet).getString("VRN")
      verify(partnerDetailsResultSet).getString("CRN")
      verify(partnerDetailsResultSet).getDate("DATE_OF_INCORPORATION")
      verify(partnerDetailsResultSet).getString("COUNTRY_OF_INCORPORATION")
      verify(partnerDetailsResultSet).getString("FOREIGN_CORPORATE_REF")
      verify(partnerDetailsResultSet).getString("ADDRESS_1")
      verify(partnerDetailsResultSet).getString("ADDRESS_2")
      verify(partnerDetailsResultSet).getString("ADDRESS_3")
      verify(partnerDetailsResultSet).getString("ADDRESS_4")
      verify(partnerDetailsResultSet).getString("POSTCODE")
      verify(partnerDetailsResultSet).getString("COUNTRY")
      verify(partnerDetailsResultSet).getString("ADI")
      verify(partnerDetailsResultSet).getString("IOM_OR_CI_FLAG")
      verify(partnerDetailsResultSet).getString("PHONE_NUMBER")
      verify(partnerDetailsResultSet).getString("MOBILE_PHONE_NUMBER")
      verify(partnerDetailsResultSet).getString("FAX_NUMBER")
      verify(partnerDetailsResultSet).getString("EMAIL_ADDR")
      verify(partnerDetailsResultSet).getObject(
        "IS_FUTURE_LEAVE_DATE",
        classOf[java.lang.Integer]
      )

      verify(partnerDetailsResultSet).getObject(
        "IS_FUTURE_JOIN_DATE",
        classOf[java.lang.Integer]
      )

      verify(partnerDetailsResultSet).getObject(
        "BUSINESS_TYPE",
        classOf[java.lang.Integer]
      )

      verify(partnerDetailsResultSet).close()
      verify(mockCsMgd).close()
    }

    Regime.values.toList.filter(_ != Regime.MGD).foreach { regime =>
      s"return PartnerDetails error when regime is $regime" in {
        val regNumber = "XWM12345678901"

        val result = repository.getPartnerDetails(GBD, regNumber).failed.futureValue
        result          shouldBe a[RuntimeException]
        result.getMessage should include("Regime GBD is not supported for getPartnerDetails")

        verify(mockCsMgd, times(0)).setString(1, regNumber)
        verify(mockCsMgd, times(0)).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
        verify(mockCsMgd, times(0)).registerOutParameter(3, oracle.jdbc.OracleTypes.DATE)
        verify(mockCsMgd, times(0)).execute()

        verify(partnerDetailsResultSet, times(0)).next()
        verify(partnerDetailsResultSet, times(0)).getString("mgd_reg_number")
        verify(partnerDetailsResultSet, times(0)).getDate("DATE_OF_JOINING")
        verify(partnerDetailsResultSet, times(0)).getDate("DATE_OF_LEAVING")
        verify(partnerDetailsResultSet, times(0)).getString("SOLE_PROP_TITLE")
        verify(partnerDetailsResultSet, times(0)).getString("SOLE_PROP_FIRST_NAME")
        verify(partnerDetailsResultSet, times(0)).getString("SOLE_PROP_MIDDLE_NAME")
        verify(partnerDetailsResultSet, times(0)).getString("SOLE_PROP_LAST_NAME")
        verify(partnerDetailsResultSet, times(0)).getString("BUSINESS_NAME")
        verify(partnerDetailsResultSet, times(0)).getString("TRADING_NAME")
        verify(partnerDetailsResultSet, times(0)).getDate("DATE_OF_BIRTH")
        verify(partnerDetailsResultSet, times(0)).getString("NINO")
        verify(partnerDetailsResultSet, times(0)).getString("UTR")
        verify(partnerDetailsResultSet, times(0)).getString("VRN")
        verify(partnerDetailsResultSet, times(0)).getString("CRN")
        verify(partnerDetailsResultSet, times(0)).getDate("DATE_OF_INCORPORATION")
        verify(partnerDetailsResultSet, times(0)).getString("COUNTRY_OF_INCORPORATION")
        verify(partnerDetailsResultSet, times(0)).getString("FOREIGN_CORPORATE_REF")
        verify(partnerDetailsResultSet, times(0)).getString("ADDRESS_1")
        verify(partnerDetailsResultSet, times(0)).getString("ADDRESS_2")
        verify(partnerDetailsResultSet, times(0)).getString("ADDRESS_3")
        verify(partnerDetailsResultSet, times(0)).getString("ADDRESS_4")
        verify(partnerDetailsResultSet, times(0)).getString("POSTCODE")
        verify(partnerDetailsResultSet, times(0)).getString("COUNTRY")
        verify(partnerDetailsResultSet, times(0)).getString("ADI")
        verify(partnerDetailsResultSet, times(0)).getString("IOM_OR_CI_FLAG")
        verify(partnerDetailsResultSet, times(0)).getString("PHONE_NUMBER")
        verify(partnerDetailsResultSet, times(0)).getString("MOBILE_PHONE_NUMBER")
        verify(partnerDetailsResultSet, times(0)).getString("FAX_NUMBER")
        verify(partnerDetailsResultSet, times(0)).getString("EMAIL_ADDR")
        verify(partnerDetailsResultSet, times(0)).getInt("IS_FUTURE_LEAVE_DATE")
        verify(partnerDetailsResultSet, times(0)).getInt("IS_FUTURE_JOIN_DATE")
        verify(partnerDetailsResultSet, times(0)).getInt("BUSINESS_TYPE")
        verify(partnerDetailsResultSet, times(0)).close()
      }
    }

    "return empty PartnerDetails when regNumber is null" in {
      val regNumber: Null = null
      when(mockCsMgd.getDate(3)).thenReturn(null)
      val result = repository.getPartnerDetails(Regime.MGD, regNumber).futureValue

      result          shouldBe PartnerDetails(Seq.empty, None)
      result.partners shouldBe empty

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
      verify(mockCsMgd).registerOutParameter(3, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).execute()

      verify(partnerDetailsResultSet, times(0)).next()
      verify(partnerDetailsResultSet, times(0)).getString("mgd_reg_number")
      verify(partnerDetailsResultSet, times(0)).getDate("DATE_OF_JOINING")
      verify(partnerDetailsResultSet, times(0)).getDate("DATE_OF_LEAVING")
      verify(partnerDetailsResultSet, times(0)).getString("SOLE_PROP_TITLE")
      verify(partnerDetailsResultSet, times(0)).getString("SOLE_PROP_FIRST_NAME")
      verify(partnerDetailsResultSet, times(0)).getString("SOLE_PROP_MIDDLE_NAME")
      verify(partnerDetailsResultSet, times(0)).getString("SOLE_PROP_LAST_NAME")
      verify(partnerDetailsResultSet, times(0)).getString("BUSINESS_NAME")
      verify(partnerDetailsResultSet, times(0)).getString("TRADING_NAME")
      verify(partnerDetailsResultSet, times(0)).getDate("DATE_OF_BIRTH")
      verify(partnerDetailsResultSet, times(0)).getString("NINO")
      verify(partnerDetailsResultSet, times(0)).getString("UTR")
      verify(partnerDetailsResultSet, times(0)).getString("VRN")
      verify(partnerDetailsResultSet, times(0)).getString("CRN")
      verify(partnerDetailsResultSet, times(0)).getDate("DATE_OF_INCORPORATION")
      verify(partnerDetailsResultSet, times(0)).getString("COUNTRY_OF_INCORPORATION")
      verify(partnerDetailsResultSet, times(0)).getString("FOREIGN_CORPORATE_REF")
      verify(partnerDetailsResultSet, times(0)).getString("ADDRESS_1")
      verify(partnerDetailsResultSet, times(0)).getString("ADDRESS_2")
      verify(partnerDetailsResultSet, times(0)).getString("ADDRESS_3")
      verify(partnerDetailsResultSet, times(0)).getString("ADDRESS_4")
      verify(partnerDetailsResultSet, times(0)).getString("POSTCODE")
      verify(partnerDetailsResultSet, times(0)).getString("COUNTRY")
      verify(partnerDetailsResultSet, times(0)).getString("ADI")
      verify(partnerDetailsResultSet, times(0)).getString("IOM_OR_CI_FLAG")
      verify(partnerDetailsResultSet, times(0)).getString("PHONE_NUMBER")
      verify(partnerDetailsResultSet, times(0)).getString("MOBILE_PHONE_NUMBER")
      verify(partnerDetailsResultSet, times(0)).getString("FAX_NUMBER")
      verify(partnerDetailsResultSet, times(0)).getString("EMAIL_ADDR")
      verify(partnerDetailsResultSet, times(0)).getInt("IS_FUTURE_LEAVE_DATE")
      verify(partnerDetailsResultSet, times(0)).getInt("IS_FUTURE_JOIN_DATE")
      verify(partnerDetailsResultSet, times(0)).getInt("BUSINESS_TYPE")
      verify(partnerDetailsResultSet, times(0)).close()

      verify(mockCsMgd).close()
    }

    "return Empty List when PartnerDetails result set is empty" in {
      val regNumber = "XWM12345678901"
      when(mockCsMgd.getDate(3)).thenReturn(Date.valueOf("2026-7-30"))
      when(partnerDetailsResultSet.next()).thenReturn(false)

      val result = repository.getPartnerDetails(Regime.MGD, regNumber).futureValue

      result shouldBe PartnerDetails(Seq.empty, Some(LocalDate.of(2026, 7, 30)))

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
      verify(mockCsMgd).registerOutParameter(3, oracle.jdbc.OracleTypes.DATE)
      verify(mockCsMgd).execute()

      verify(partnerDetailsResultSet, times(0)).next()
      verify(partnerDetailsResultSet, times(0)).getString("mgd_reg_number")
      verify(partnerDetailsResultSet, times(0)).getDate("DATE_OF_JOINING")
      verify(partnerDetailsResultSet, times(0)).getDate("DATE_OF_LEAVING")
      verify(partnerDetailsResultSet, times(0)).getString("SOLE_PROP_TITLE")
      verify(partnerDetailsResultSet, times(0)).getString("SOLE_PROP_FIRST_NAME")
      verify(partnerDetailsResultSet, times(0)).getString("SOLE_PROP_MIDDLE_NAME")
      verify(partnerDetailsResultSet, times(0)).getString("SOLE_PROP_LAST_NAME")
      verify(partnerDetailsResultSet, times(0)).getString("BUSINESS_NAME")
      verify(partnerDetailsResultSet, times(0)).getString("TRADING_NAME")
      verify(partnerDetailsResultSet, times(0)).getDate("DATE_OF_BIRTH")
      verify(partnerDetailsResultSet, times(0)).getString("NINO")
      verify(partnerDetailsResultSet, times(0)).getString("UTR")
      verify(partnerDetailsResultSet, times(0)).getString("VRN")
      verify(partnerDetailsResultSet, times(0)).getString("CRN")
      verify(partnerDetailsResultSet, times(0)).getDate("DATE_OF_INCORPORATION")
      verify(partnerDetailsResultSet, times(0)).getString("COUNTRY_OF_INCORPORATION")
      verify(partnerDetailsResultSet, times(0)).getString("FOREIGN_CORPORATE_REF")
      verify(partnerDetailsResultSet, times(0)).getString("ADDRESS_1")
      verify(partnerDetailsResultSet, times(0)).getString("ADDRESS_2")
      verify(partnerDetailsResultSet, times(0)).getString("ADDRESS_3")
      verify(partnerDetailsResultSet, times(0)).getString("ADDRESS_4")
      verify(partnerDetailsResultSet, times(0)).getString("POSTCODE")
      verify(partnerDetailsResultSet, times(0)).getString("COUNTRY")
      verify(partnerDetailsResultSet, times(0)).getString("ADI")
      verify(partnerDetailsResultSet, times(0)).getString("IOM_OR_CI_FLAG")
      verify(partnerDetailsResultSet, times(0)).getString("PHONE_NUMBER")
      verify(partnerDetailsResultSet, times(0)).getString("MOBILE_PHONE_NUMBER")
      verify(partnerDetailsResultSet, times(0)).getString("FAX_NUMBER")
      verify(partnerDetailsResultSet, times(0)).getString("EMAIL_ADDR")
      verify(partnerDetailsResultSet, times(0)).getInt("IS_FUTURE_LEAVE_DATE")
      verify(partnerDetailsResultSet, times(0)).getInt("IS_FUTURE_JOIN_DATE")
      verify(partnerDetailsResultSet, times(0)).getInt("BUSINESS_TYPE")
      verify(partnerDetailsResultSet, times(0)).close()

      verify(mockCsMgd).close()

    }
  }
}
