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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.ReallocationsOut
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.UrlParameterError.{InvalidRegNumber, InvalidRegimeCode, UnexpectedError}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.services.GamblingReallocationsService
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.{validRegime, validResponseReallocationsIn}

import scala.concurrent.Future

class GamblingReallocationsControllerSpec extends SpecBase with MockitoSugar {

  private trait Setup {
    val mockService: GamblingReallocationsService = mock[GamblingReallocationsService]
    val controller = new GamblingReallocationsController(fakeAuthAction, mockService, cc)
  }

  "GamblingReallocationsController#getReallocationsIn" - {

    "returns 200 when service succeeds" in new Setup {

      when(mockService.getReallocationsIn(eqTo(validRegime), eqTo("XWM00000001770"), eqTo(1), eqTo(10))(any()))
        .thenReturn(Future.successful(Right(validResponseReallocationsIn)))

      val req = FakeRequest(GET, s"/gambling/reallocations-in/$validRegime/XWM00000001770?pageNo=1&pageSize=10")
      val res: Future[Result] = controller.getReallocationsIn(validRegime, "XWM00000001770", 1, 10)(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)
      contentAsJson(res) mustBe Json.toJson(validResponseReallocationsIn)

      verify(mockService).getReallocationsIn(eqTo(validRegime), eqTo("XWM00000001770"), eqTo(1), eqTo(10))(any())
      verifyNoMoreInteractions(mockService)
    }

    "returns 400 when InvalidRegimeError" in new Setup {
      when(mockService.getReallocationsIn(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(InvalidRegimeCode)))

      val req = FakeRequest(GET, "/gambling/reallocations-in/INVALID_REGIME/XWM00000001770")
      val res = controller.getReallocationsIn(" ", " ", 1, 10)(req)

      status(res) mustBe BAD_REQUEST
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "INVALID_REGIME_CODE",
        "message" -> "Invalid Regime Code"
      )

      verify(mockService).getReallocationsIn(eqTo(" "), eqTo(" "), eqTo(1), eqTo(10))(any())
    }

    "returns 400 when InvalidRegNumber" in new Setup {
      when(mockService.getReallocationsIn(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(InvalidRegNumber)))

      val req = FakeRequest(GET, s"/gambling/reallocations-in/$validRegime/InvalidRegNo")
      val res: Future[Result] = controller.getReallocationsIn(" ", " ", 1, 10)(req)

      status(res) mustBe BAD_REQUEST
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "INVALID_REG_NUMBER",
        "message" -> "regNumber has invalid format"
      )

      verify(mockService).getReallocationsIn(eqTo(" "), eqTo(" "), eqTo(1), eqTo(10))(any())
    }

    "returns 500 when UnexpectedError" in new Setup {
      when(mockService.getReallocationsIn(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(UnexpectedError)))

      val req = FakeRequest(GET, s"/gambling/reallocations-in/$validRegime/ERR00001770")
      val res: Future[Result] = controller.getReallocationsIn(validRegime, "ERR00001770", 1, 10)(req)

      status(res) mustBe INTERNAL_SERVER_ERROR
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "UNEXPECTED_ERROR",
        "message" -> "Unexpected error occurred"
      )

      verify(mockService).getReallocationsIn(eqTo(validRegime), eqTo("ERR00001770"), eqTo(1), eqTo(10))(any())
    }
  }

  "getReallocationsOut" - {

    "returns 200 when service succeeds" in new Setup {

      when(mockService.getReallocationsOut(eqTo("mgd"), eqTo("XWM00000001770"), eqTo(1), eqTo(10))(any()))
        .thenReturn(Future.successful(Right(ReallocationsOut.empty)))

      val req = FakeRequest(GET, s"/gambling/reallocations-out/mgd/XWM00000001770?pageNo=1&pageSize=10")
      val res: Future[Result] = controller.getReallocationsOut("mgd", "XWM00000001770", 1, 10)(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)
      contentAsJson(res) mustBe Json.toJson(ReallocationsOut.empty)

      verify(mockService).getReallocationsOut(eqTo("mgd"), eqTo("XWM00000001770"), eqTo(1), eqTo(10))(any())
      verifyNoMoreInteractions(mockService)
    }
  }

  "returns 400 when InvalidRegNumber" in new Setup {
    when(mockService.getReallocationsOut(any(), any(), any(), any())(any()))
      .thenReturn(Future.successful(Left(InvalidRegNumber)))

    val req = FakeRequest(GET, s"/gambling/reallocations-out/mgd/XWM00000001770?pageNo=1&pageSize=10")
    val res: Future[Result] = controller.getReallocationsOut("mgd", "XWM00000001770", 1, 10)(req)

    status(res) mustBe BAD_REQUEST
    contentAsJson(res) mustBe Json.obj(
      "code"    -> "INVALID_REG_NUMBER",
      "message" -> "regNumber has invalid format"
    )

    verify(mockService).getReallocationsOut(eqTo("mgd"), eqTo("XWM00000001770"), eqTo(1), eqTo(10))(any())
  }

  "returns 500 when UnexpectedError" in new Setup {
    when(mockService.getReallocationsOut(any(), any(), any(), any())(any()))
      .thenReturn(Future.successful(Left(UnexpectedError)))

    val req = FakeRequest(GET, s"/gambling/reallocations-out/mgd/XWM00000001770?pageNo=1&pageSize=10")
    val res: Future[Result] = controller.getReallocationsOut("mgd", "XWM00000001770", 1, 10)(req)

    status(res) mustBe INTERNAL_SERVER_ERROR
    contentAsJson(res) mustBe Json.obj(
      "code"    -> "UNEXPECTED_ERROR",
      "message" -> "Unexpected error occurred"
    )

    verify(mockService).getReallocationsOut(eqTo("mgd"), eqTo("XWM00000001770"), eqTo(1), eqTo(10))(any())
  }
}
