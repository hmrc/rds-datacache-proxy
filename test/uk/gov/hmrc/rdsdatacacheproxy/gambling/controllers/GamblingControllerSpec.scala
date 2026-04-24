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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.GamblingError.{InvalidMgdRegNumber, UnexpectedError}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{BusinessDetails, ReturnSummary}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.BusinessName
import uk.gov.hmrc.rdsdatacacheproxy.gambling.services.GamblingService

import java.time.LocalDate
import scala.concurrent.Future

class GamblingControllerSpec extends SpecBase with MockitoSugar {

  private trait Setup {
    val mockService: GamblingService = mock[GamblingService]
    val controller = new GamblingController(fakeAuthAction, mockService, cc)
  }

  "GamblingController#getReturnSummary" - {

    "returns 200 when service succeeds" in new Setup {
      val summary = ReturnSummary("XWM00000001770", 2, 1)

      when(mockService.getReturnSummary(eqTo("XWM00000001770"))(any()))
        .thenReturn(Future.successful(Right(summary)))

      val req = FakeRequest(GET, "/gambling/return-summary/XWM00000001770")
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

      val req = FakeRequest(GET, "/gambling/return-summary/XWM00000001770")
      val res = controller.getReturnSummary("XWM00000001770")(req)

      status(res) mustBe OK

      verify(mockService).getReturnSummary(eqTo("XWM00000001770"))(any())
    }
    "returns 400 when InvalidMgdRegNumber" in new Setup {
      when(mockService.getReturnSummary(any())(any()))
        .thenReturn(Future.successful(Left(InvalidMgdRegNumber)))

      val req = FakeRequest(GET, "/gambling/return-summary/jhrfdshgksdhg")
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

      val req = FakeRequest(GET, "/gambling/return-summary/ERR00001770")
      val res = controller.getReturnSummary("ERR00001770")(req)

      status(res) mustBe INTERNAL_SERVER_ERROR
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "UNEXPECTED_ERROR",
        "message" -> "Unexpected error occurred"
      )

      verify(mockService).getReturnSummary(eqTo("ERR00001770"))(any())
    }
  }

  "GamblingController#getBusinessName" - {

    "returns 200 when service succeeds" in new Setup {
      val dateTime: Some[LocalDate] = Some(LocalDate.of(2026, 4, 20))
      val name = BusinessName("XWM00000001770", "fooBar", "fooBar", "fooBar", "fooBar", "fooBar", "fooBar", "fooBar", dateTime)

      when(mockService.getBusinessName(eqTo("XWM00000001770"))(any()))
        .thenReturn(Future.successful(Right(name)))

      val req = FakeRequest(GET, "/gambling/business-name/XWM00000001770")
      val res = controller.getBusinessName("XWM00000001770")(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)
      contentAsJson(res) mustBe Json.toJson(name)

      verify(mockService).getBusinessName(eqTo("XWM00000001770"))(any())
      verifyNoMoreInteractions(mockService)
    }

    "allows request through AuthAction" in new Setup {
      val dateTime: Some[LocalDate] = Some(LocalDate.of(2026, 4, 20))
      val name = BusinessName("XWM00000001770", "fooBar", "fooBar", "fooBar", "fooBar", "fooBar", "fooBar", "fooBar", dateTime)

      when(mockService.getBusinessName(any())(any()))
        .thenReturn(Future.successful(Right(name)))

      val req = FakeRequest(GET, "/gambling/business-name/XWM00000001770")
      val res = controller.getBusinessName("XWM00000001770")(req)

      status(res) mustBe OK

      verify(mockService).getBusinessName(eqTo("XWM00000001770"))(any())
    }
  }
  "returns 400 when InvalidMgdRegNumber" in new Setup {
    when(mockService.getBusinessName(any())(any()))
      .thenReturn(Future.successful(Left(InvalidMgdRegNumber)))

    val req = FakeRequest(GET, "/gambling/business-name/jhrfdshgksdhg")
    val res = controller.getBusinessName(" ")(req)

    status(res) mustBe BAD_REQUEST
    contentAsJson(res) mustBe Json.obj(
      "code"    -> "INVALID_MGD_REG_NUMBER",
      "message" -> "mgdRegNumber does not exist"
    )

    verify(mockService).getBusinessName(eqTo(" "))(any())
  }

  "returns 500 when UnexpectedError" in new Setup {
    when(mockService.getBusinessName(any())(any()))
      .thenReturn(Future.successful(Left(UnexpectedError)))

    val req = FakeRequest(GET, "/gambling/business-name/ERR00001770")
    val res = controller.getBusinessName("ERR00001770")(req)

    status(res) mustBe INTERNAL_SERVER_ERROR
    contentAsJson(res) mustBe Json.obj(
      "code"    -> "UNEXPECTED_ERROR",
      "message" -> "Unexpected error occurred"
    )

    verify(mockService).getBusinessName(eqTo("ERR00001770"))(any())
  }

  "GamblingController#getBusinessDetails" - {

    "returns 200 when service succeeds for BusinessDetails" in new Setup {
      val summary = BusinessDetails("XWM00000001770", 2, 1, "foo", Some(LocalDate.of(2024, 4, 21)), "bar", Some(LocalDate.of(2024, 4, 21)))

      when(mockService.getBusinessDetails(eqTo("XWM00000001770"))(any()))
        .thenReturn(Future.successful(Right(summary)))

      val req = FakeRequest(GET, "/gambling/business-details/XWM00000001770")
      val res = controller.getBusinessDetails("XWM00000001770")(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)
      contentAsJson(res) mustBe Json.toJson(summary)

      verify(mockService).getBusinessDetails(eqTo("XWM00000001770"))(any())
      verifyNoMoreInteractions(mockService)
    }

    "allows request through AuthAction for BusinessDetails" in new Setup {
      val summary = BusinessDetails("XWM00000001770", 2, 1, "foo", Some(LocalDate.of(2024, 4, 21)), "bar", Some(LocalDate.of(2024, 4, 21)))

      when(mockService.getBusinessDetails(any())(any()))
        .thenReturn(Future.successful(Right(summary)))

      val req = FakeRequest(GET, "/gambling/business-details/XWM00000001770")
      val res = controller.getBusinessDetails("XWM00000001770")(req)

      status(res) mustBe OK

      verify(mockService).getBusinessDetails(eqTo("XWM00000001770"))(any())
    }
  }

  "returns 400 when InvalidMgdRegNumber for BusinessDetails" in new Setup {
    when(mockService.getBusinessDetails(any())(any()))
      .thenReturn(Future.successful(Left(InvalidMgdRegNumber)))

    val req = FakeRequest(GET, "/gambling/business-details/jhrfdshgksdhg")
    val res = controller.getBusinessDetails(" ")(req)

    status(res) mustBe BAD_REQUEST
    contentAsJson(res) mustBe Json.obj(
      "code"    -> "INVALID_MGD_REG_NUMBER",
      "message" -> "mgdRegNumber does not exist"
    )

    verify(mockService).getBusinessDetails(eqTo(" "))(any())
  }

  "returns 500 when UnexpectedError for BusinessDetails" in new Setup {
    when(mockService.getBusinessDetails(any())(any()))
      .thenReturn(Future.successful(Left(UnexpectedError)))

    val req = FakeRequest(GET, "/gambling/business-details/ERR00001770")
    val res = controller.getBusinessDetails("ERR00001770")(req)

    status(res) mustBe INTERNAL_SERVER_ERROR
    contentAsJson(res) mustBe Json.obj(
      "code"    -> "UNEXPECTED_ERROR",
      "message" -> "Unexpected error occurred"
    )

    verify(mockService).getBusinessDetails(eqTo("ERR00001770"))(any())
  }

}
