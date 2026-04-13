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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import play.api.libs.json.{JsValue, Json}

import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.mgd.controllers.MgdController
import uk.gov.hmrc.rdsdatacacheproxy.mgd.models.MgdError.{InvalidMgdRegNumber, UnexpectedError}
import uk.gov.hmrc.rdsdatacacheproxy.mgd.models.ReturnSummary
import uk.gov.hmrc.rdsdatacacheproxy.mgd.services.MgdService

import scala.concurrent.Future

class MgdControllerSpec extends SpecBase with MockitoSugar {

  private trait Setup {
    val mockService: MgdService = mock[MgdService]
    val controller = new MgdController(fakeAuthAction, mockService, cc)
  }

  "MgdController#getReturnSummary" - {

    "returns 200 when service succeeds" in new Setup {
      val summary = ReturnSummary("XWM00000001770", 2, 1)

      when(mockService.getReturnSummary(eqTo("XWM00000001770"))(any()))
        .thenReturn(Future.successful(Right(summary)))

      val req = FakeRequest(GET, "/mgd/return-summary/XWM00000001770")
      val res = controller.getReturnSummary("XWM00000001770")(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)
      contentAsJson(res) mustBe Json.toJson(summary)

      verify(mockService).getReturnSummary(eqTo("XWM00000001770"))(any())
      verifyNoMoreInteractions(mockService)
    }

    "allows request through AuthAction" in new Setup {
      val summary = ReturnSummary("XWM00000001770", 2, 1)

      when(mockService.getReturnSummary(any())(any()))
        .thenReturn(Future.successful(Right(summary)))

      val req = FakeRequest(GET, "/mgd/return-summary/XWM00000001770")
      val res = controller.getReturnSummary("XWM00000001770")(req)

      status(res) mustBe OK

      verify(mockService).getReturnSummary(eqTo("XWM00000001770"))(any())
    }
  }

  "returns 400 when InvalidMgdRegNumber" in new Setup {
    when(mockService.getReturnSummary(any())(any()))
      .thenReturn(Future.successful(Left(InvalidMgdRegNumber)))

    val req = FakeRequest(GET, "/mgd/return-summary/jhrfdshgksdhg")
    val res = controller.getReturnSummary(" ")(req)

    status(res) mustBe BAD_REQUEST
    contentAsJson(res) mustBe Json.obj(
      "code"    -> "INVALID_MGD_REG_NUMBER",
      "message" -> "mgdRegNumber does not exist"
    )

    verify(mockService).getReturnSummary(eqTo(" "))(any())
  }

  "returns 500 when UnexpectedError" in new Setup {
    when(mockService.getReturnSummary(any())(any()))
      .thenReturn(Future.successful(Left(UnexpectedError)))

    val req = FakeRequest(GET, "/mgd/return-summary/ERR00001770")
    val res = controller.getReturnSummary("ERR00001770")(req)

    status(res) mustBe INTERNAL_SERVER_ERROR
    contentAsJson(res) mustBe Json.obj(
      "code"    -> "UNEXPECTED_ERROR",
      "message" -> "Unexpected error occurred"
    )

    verify(mockService).getReturnSummary(eqTo("ERR00001770"))(any())
  }

}
