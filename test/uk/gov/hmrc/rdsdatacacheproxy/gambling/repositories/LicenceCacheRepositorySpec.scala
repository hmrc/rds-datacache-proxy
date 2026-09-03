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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{LicenceDetails, Regime}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}

import java.sql.{CallableStatement, Connection, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class LicenceCacheRepositorySpec extends AnyWordSpec with Matchers with BeforeAndAfter {

  private val gtrDb: GTRDatabase = mock(classOf[Database]).asInstanceOf[GTRDatabase]
  private val mgdDb: MGDDatabase = mock(classOf[Database]).asInstanceOf[MGDDatabase]
  private val mgdMockConnection: Connection = mock(classOf[Connection])
  private val gtrMockConnection: Connection = mock(classOf[Connection])
  private val mockCsMgd: CallableStatement = mock(classOf[CallableStatement])
  private val licenceRs: ResultSet = mock(classOf[ResultSet])

  private val regNumber = "XEM00000001335"

  private val repository: LicenceCacheRepository = new LicenceCacheRepository(
    mgdDb = mgdDb,
    gtrDb = gtrDb
  )

  before {
    Mockito.reset(mgdDb, gtrDb, mgdMockConnection, gtrMockConnection, mockCsMgd, licenceRs)

    when(mgdDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mgdMockConnection)
    }

    when(gtrDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(gtrMockConnection)
    }

    when(mgdMockConnection.prepareCall("{ call MGD_DC_VARIATION_PK.GET_LICENCE_DETAILS(?, ?) }"))
      .thenReturn(mockCsMgd)

    when(mockCsMgd.getObject(2)).thenReturn(licenceRs)
  }

  "getLicenceDetails" should {

    "return LicenceDetails for MGD regime when stored procedure returns data" in {
      when(licenceRs.next()).thenReturn(true)
      when(licenceRs.getString("mgd_reg_number")).thenReturn(regNumber)
      when(licenceRs.getString("HAVE_GAMBLING_LICENCE_NO")).thenReturn("1")
      when(licenceRs.getString("GAMBLING_LICENCE_NO")).thenReturn("123-456789-A-123456-789")
      when(licenceRs.getString("HELD_BY_LANDLORD")).thenReturn("1")
      when(licenceRs.getString("LOCAL_AUTHORITY")).thenReturn("1")
      when(licenceRs.getString("FAMILY_ENTERTAINMENT")).thenReturn("0")
      when(licenceRs.getString("CLUB_GAMING")).thenReturn("0")
      when(licenceRs.getString("CLUB_LICENCE")).thenReturn("1")
      when(licenceRs.getString("PRIZE_GAMING")).thenReturn("0")
      when(licenceRs.getString("ON_PREMISES")).thenReturn("1")
      when(licenceRs.getString("CLUB_PREMISES")).thenReturn("0")
      when(licenceRs.getString("REG_CERT")).thenReturn("0")
      when(licenceRs.getString("BOOKMAKING")).thenReturn("0")
      when(licenceRs.getString("BINGO")).thenReturn("0")
      when(licenceRs.getString("AMUSEMENT")).thenReturn("0")
      when(licenceRs.getString("SERVE_ALCOHOL")).thenReturn("0")
      when(licenceRs.getString("PREMISES_NOT_COVERED")).thenReturn("0")
      when(licenceRs.getString("system_date")).thenReturn("2026-05-31")

      val result = repository.getLicenceDetails(Regime.MGD, regNumber).futureValue

      result shouldBe LicenceDetails(
        mgdRegNumber          = regNumber,
        haveGamblingLicenceNo = Some("1"),
        gamblingLicenceNo     = "123-456789-A-123456-789",
        heldByLandlord        = Some("1"),
        localAuthority        = Some("1"),
        familyEntertainment   = Some("0"),
        clubGaming            = Some("0"),
        clubLicence           = Some("1"),
        prizeGaming           = Some("0"),
        onPremises            = Some("1"),
        clubPremises          = Some("0"),
        regCert               = Some("0"),
        bookmaking            = Some("0"),
        bingo                 = Some("0"),
        amusement             = Some("0"),
        serveAlcohol          = Some("0"),
        premisesNotCovered    = Some("0"),
        systemDate            = Some(LocalDate.of(2026, 5, 31))
      )

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
      verify(mockCsMgd).execute()
      verify(licenceRs).close()
      verify(mockCsMgd).close()
    }

    "default gamblingLicenceNo to an empty string when the column is null" in {
      when(licenceRs.next()).thenReturn(true)
      when(licenceRs.getString("mgd_reg_number")).thenReturn(regNumber)
      when(licenceRs.getString("GAMBLING_LICENCE_NO")).thenReturn(null)

      val result = repository.getLicenceDetails(Regime.MGD, regNumber).futureValue

      result.gamblingLicenceNo shouldBe ""
    }

    "fail when the result set has no rows" in {
      when(licenceRs.next()).thenReturn(false)

      val ex = repository.getLicenceDetails(Regime.MGD, regNumber).failed.futureValue
      ex shouldBe a[RuntimeException]

      verify(licenceRs).close()
      verify(mockCsMgd).close()
    }

    "fail when the result set is null" in {
      when(mockCsMgd.getObject(2)).thenReturn(null)

      val ex = repository.getLicenceDetails(Regime.MGD, regNumber).failed.futureValue
      ex shouldBe a[RuntimeException]

      verify(mockCsMgd).close()
    }

    "fail for a regime other than MGD" in {
      val ex = repository.getLicenceDetails(Regime.GBD, regNumber).failed.futureValue
      ex shouldBe a[RuntimeException]
    }
  }
}
