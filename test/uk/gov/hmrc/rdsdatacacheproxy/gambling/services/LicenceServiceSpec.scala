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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.services

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.{reset, verify, verifyNoMoreInteractions, when}
import org.scalatest.matchers.must.Matchers.mustBe
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.LicenceDetails
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.Regime
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError.{InvalidRegNumber, InvalidRegimeCode, UnexpectedError}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.LicenceDataSource

import java.time.LocalDate
import scala.concurrent.Future

final class LicenceServiceSpec extends SpecBase {

  private val repository = mock[LicenceDataSource]
  private val service = new LicenceService(repository)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(repository)
  }

  private val validRegime = Regime.MGD
  private val lowercaseRegNumber = "xem00000001335 "
  private val normalisedRegNumber = "XEM00000001335"

  private val validResponseLicenceDetails = LicenceDetails(
    mgdRegNumber          = normalisedRegNumber,
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

  "LicenceService#getLicenceDetails" - {

    "return validResponseLicenceDetails when repository succeeds AND normalise input (trim + uppercase) before calling repository" in {
      when(repository.getLicenceDetails(eqTo(validRegime), eqTo(normalisedRegNumber)))
        .thenReturn(Future.successful(validResponseLicenceDetails))

      val result = service.getLicenceDetails(validRegime.toString, lowercaseRegNumber).futureValue

      result mustBe Right(validResponseLicenceDetails)
      verify(repository).getLicenceDetails(eqTo(validRegime), eqTo(normalisedRegNumber))
      verifyNoMoreInteractions(repository)
    }

    "return InvalidRegimeCode and not call repository when Regime input is invalid" in {
      val result = service.getLicenceDetails("INVALID", lowercaseRegNumber).futureValue
      result mustBe Left(InvalidRegimeCode)
      verifyNoMoreInteractions(repository)
    }

    "return InvalidRegNumber and not call repository when RegNumber input is invalid" in {
      val invalidRegNumber = "xem12345678"
      val result = service.getLicenceDetails(validRegime.toString, invalidRegNumber).futureValue
      result mustBe Left(InvalidRegNumber)
      verifyNoMoreInteractions(repository)
    }

    "return UnexpectedError when repository throws exception" in {
      when(repository.getLicenceDetails(eqTo(validRegime), eqTo(normalisedRegNumber)))
        .thenReturn(Future.failed(new RuntimeException("DB failure when calling repo")))

      val result = service.getLicenceDetails(validRegime.toString, lowercaseRegNumber).futureValue

      result mustBe Left(UnexpectedError)
      verify(repository).getLicenceDetails(eqTo(validRegime), eqTo(normalisedRegNumber))
      verifyNoMoreInteractions(repository)
    }
  }
}
