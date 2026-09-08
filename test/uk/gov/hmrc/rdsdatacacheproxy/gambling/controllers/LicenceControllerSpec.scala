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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.controllers

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.LicenceDetails
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError.{InvalidRegNumber, InvalidRegimeCode, UnexpectedError}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.services.LicenceService
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validRegime

import java.time.LocalDate
import scala.concurrent.Future

class LicenceControllerSpec extends SpecBase with MockitoSugar {

  private trait Setup {
    val mockService: LicenceService = mock[LicenceService]
    val controller = new LicenceController(fakeAuthAction, mockService, cc)
  }

  private val regNumber = "XEM00000001335"

  private val validResponseLicenceDetails = LicenceDetails(
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

  "LicenceController#getLicenceDetails" - {

    "returns 200 when service succeeds" in new Setup {

      when(mockService.getLicenceDetails(eqTo(validRegime), eqTo(regNumber))(any()))
        .thenReturn(Future.successful(Right(validResponseLicenceDetails)))

      val req = FakeRequest(GET, s"/gambling/licence-details/$validRegime/$regNumber")
      val res: Future[Result] = controller.getLicenceDetails(validRegime, regNumber)(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)
      contentAsJson(res) mustBe Json.toJson(validResponseLicenceDetails)

      verify(mockService).getLicenceDetails(eqTo(validRegime), eqTo(regNumber))(any())
      verifyNoMoreInteractions(mockService)
    }

    "returns 400 when InvalidRegimeCode" in new Setup {
      when(mockService.getLicenceDetails(any(), any())(any()))
        .thenReturn(Future.successful(Left(InvalidRegimeCode)))

      val req = FakeRequest(GET, s"/gambling/licence-details/INVALID_REGIME/$regNumber")
      val res: Future[Result] = controller.getLicenceDetails("INVALID_REGIME", regNumber)(req)

      status(res) mustBe BAD_REQUEST
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "INVALID_REGIME_CODE",
        "message" -> "Invalid Regime Code"
      )
    }

    "returns 400 when InvalidRegNumber" in new Setup {
      when(mockService.getLicenceDetails(any(), any())(any()))
        .thenReturn(Future.successful(Left(InvalidRegNumber)))

      val req = FakeRequest(GET, s"/gambling/licence-details/$validRegime/InvalidRegNo")
      val res: Future[Result] = controller.getLicenceDetails(validRegime, "InvalidRegNo")(req)

      status(res) mustBe BAD_REQUEST
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "INVALID_REG_NUMBER",
        "message" -> "regNumber has invalid format"
      )
    }

    "returns 500 when UnexpectedError" in new Setup {
      when(mockService.getLicenceDetails(any(), any())(any()))
        .thenReturn(Future.successful(Left(UnexpectedError)))

      val req = FakeRequest(GET, s"/gambling/licence-details/$validRegime/$regNumber")
      val res: Future[Result] = controller.getLicenceDetails(validRegime, regNumber)(req)

      status(res) mustBe INTERNAL_SERVER_ERROR
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "UNEXPECTED_ERROR",
        "message" -> "Unexpected error occurred"
      )
    }
  }
}
