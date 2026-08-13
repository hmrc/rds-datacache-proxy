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
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError.{InvalidRegNumber, InvalidRegimeCode, UnexpectedError}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.services.PartnerDetailsService
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.{validRegime, validResponsePartnerDetails}

import scala.concurrent.Future

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
class PartnerDetailsControllerSpec extends SpecBase with MockitoSugar {

  private trait Setup {
    val mockService: PartnerDetailsService = mock[PartnerDetailsService]
    val controller = new PartnerDetailsController(fakeAuthAction, mockService, cc)
  }

  "GamblingReturnsController#getReturnsSubmitted" - {

    "returns 200 when service succeeds" in new Setup {

      when(mockService.getPartnerDetails(eqTo(validRegime), eqTo("XWM00000001770"))(any()))
        .thenReturn(Future.successful(Right(validResponsePartnerDetails)))

      val req = FakeRequest(GET, s"/gambling/partner-details/$validRegime/XWM00000001770")
      val res: Future[Result] = controller.getPartnerDetails(validRegime, "XWM00000001770")(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)
      contentAsJson(res) mustBe Json.toJson(validResponsePartnerDetails)

      verify(mockService).getPartnerDetails(eqTo(validRegime), eqTo("XWM00000001770"))(any())
      verifyNoMoreInteractions(mockService)
    }

    "returns 400 when InvalidRegimeError" in new Setup {
      when(mockService.getPartnerDetails(any(), any())(any()))
        .thenReturn(Future.successful(Left(InvalidRegimeCode)))

      val req = FakeRequest(GET, "/gambling/partner-details/INVALID_REGIME/XWM00000001770")
      val res: Future[Result] = controller.getPartnerDetails(" ", " ")(req)

      status(res) mustBe BAD_REQUEST
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "INVALID_REGIME_CODE",
        "message" -> "Invalid Regime Code"
      )

      verify(mockService).getPartnerDetails(eqTo(" "), eqTo(" "))(any())
    }

    "returns 400 when InvalidRegNumber" in new Setup {
      when(mockService.getPartnerDetails(any(), any())(any()))
        .thenReturn(Future.successful(Left(InvalidRegNumber)))

      val req = FakeRequest(GET, s"/gambling/partner-details/$validRegime/InvalidRegNo")
      val res: Future[Result] = controller.getPartnerDetails(" ", " ")(req)

      status(res) mustBe BAD_REQUEST
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "INVALID_REG_NUMBER",
        "message" -> "regNumber has invalid format"
      )

      verify(mockService).getPartnerDetails(eqTo(" "), eqTo(" "))(any())
    }

    "returns 500 when UnexpectedError" in new Setup {
      when(mockService.getPartnerDetails(any(), any())(any()))
        .thenReturn(Future.successful(Left(UnexpectedError)))

      val req = FakeRequest(GET, s"/gambling/partner-details/$validRegime/ERR00001770")
      val res: Future[Result] = controller.getPartnerDetails(validRegime, "ERR00001770")(req)

      status(res) mustBe INTERNAL_SERVER_ERROR
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "UNEXPECTED_ERROR",
        "message" -> "Unexpected error occurred"
      )

      verify(mockService).getPartnerDetails(eqTo(validRegime), eqTo("ERR00001770"))(any())
    }
  }
}
