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

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.*
import play.api.test.FakeRequest
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.GamblingReturnsError.{InvalidRegNumber, InvalidRegimeCode, UnexpectedError}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.services.GamblingReturnsService
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.{validRegime, validResponseReturnsSubmitted}
import scala.concurrent.Future

class GamblingReturnsControllerSpec extends SpecBase with MockitoSugar {

  private trait Setup {
    val mockService: GamblingReturnsService = mock[GamblingReturnsService]
    val controller = new GamblingReturnsController(fakeAuthAction, mockService, cc)
  }

  "GamblingReturnsController#getReturnsSubmitted" - {

    "returns 200 when service succeeds" in new Setup {

      when(mockService.getReturnsSubmitted(eqTo(validRegime), eqTo("XWM00000001770"), eqTo(1), eqTo(10))(any()))
        .thenReturn(Future.successful(Right(validResponseReturnsSubmitted)))

      val req = FakeRequest(GET, s"/gambling/returns-submitted/$validRegime/XWM00000001770?pageNo=1&pageSize=10")
      val res = controller.getReturnsSubmitted(validRegime, "XWM00000001770", 1, 10)(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)
      contentAsJson(res) mustBe Json.toJson(validResponseReturnsSubmitted)

      verify(mockService).getReturnsSubmitted(eqTo(validRegime), eqTo("XWM00000001770"), eqTo(1), eqTo(10))(any())
      verifyNoMoreInteractions(mockService)
    }
  }

  "returns 400 when InvalidRegNumber" in new Setup {
    when(mockService.getReturnsSubmitted(any(), any(), any(), any())(any()))
      .thenReturn(Future.successful(Left(InvalidRegNumber)))

    val req = FakeRequest(GET, s"/gambling/returns-submitted/$validRegime/InvalidRegNo")
    val res = controller.getReturnsSubmitted(" ", " ", 1, 10)(req)

    status(res) mustBe BAD_REQUEST
    contentAsJson(res) mustBe Json.obj(
      "code"    -> "INVALID_REG_NUMBER",
      "message" -> "regNumber does not exist"
    )

    verify(mockService).getReturnsSubmitted(eqTo(" "), eqTo(" "), eqTo(1), eqTo(10))(any())
  }

  "returns 400 when InvalidRegimeError" in new Setup {
    when(mockService.getReturnsSubmitted(any(), any(), any(), any())(any()))
      .thenReturn(Future.successful(Left(InvalidRegimeCode)))

    val req = FakeRequest(GET, "/gambling/returns-submitted/INVALID_REGIME/XWM00000001770")
    val res = controller.getReturnsSubmitted(" ", " ", 1, 10)(req)

    status(res) mustBe BAD_REQUEST
    contentAsJson(res) mustBe Json.obj(
      "code"    -> "INVALID_REGIME_CODE",
      "message" -> "Invalid Regime Code"
    )

    verify(mockService).getReturnsSubmitted(eqTo(" "), eqTo(" "), eqTo(1), eqTo(10))(any())
  }

  "returns 500 when UnexpectedError" in new Setup {
    when(mockService.getReturnsSubmitted(any(), any(), any(), any())(any()))
      .thenReturn(Future.successful(Left(UnexpectedError)))

    val req = FakeRequest(GET, s"/gambling/returns-submitted/$validRegime/ERR00001770")
    val res = controller.getReturnsSubmitted(validRegime, "ERR00001770", 1, 10)(req)

    status(res) mustBe INTERNAL_SERVER_ERROR
    contentAsJson(res) mustBe Json.obj(
      "code"    -> "UNEXPECTED_ERROR",
      "message" -> "Unexpected error occurred"
    )

    verify(mockService).getReturnsSubmitted(eqTo(validRegime), eqTo("ERR00001770"), eqTo(1), eqTo(10))(any())
  }

}
