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
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.UpdateStatusPeriodRequest
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError.{InvalidRegNumber, InvalidStatus, UnexpectedError}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.services.UpdateStatusPeriodService

import scala.concurrent.Future

class UpdateStatusPeriodControllerSpec extends SpecBase with MockitoSugar {

  private trait Setup {
    val mockService: UpdateStatusPeriodService = mock[UpdateStatusPeriodService]
    val controller = new UpdateStatusPeriodController(fakeAuthAction, mockService, cc)
  }

  "UpdateStatusPeriodController#updateStatusPeriod" - {

    "returns 204 when service succeeds" in new Setup {
      when(mockService.updateStatusPeriod(eqTo("mgd"), eqTo("XWM00000001770"), eqTo(3), eqTo(1))(any()))
        .thenReturn(Future.successful(Right(())))

      val req: FakeRequest[UpdateStatusPeriodRequest] = FakeRequest().withBody(UpdateStatusPeriodRequest(1))
      val res: Future[Result] = controller.updateStatusPeriod("mgd", "XWM00000001770", 3)(req)

      status(res) mustBe NO_CONTENT

      verify(mockService).updateStatusPeriod(eqTo("mgd"), eqTo("XWM00000001770"), eqTo(3), eqTo(1))(any())
      verifyNoMoreInteractions(mockService)
    }

    "returns 400 when InvalidStatus" in new Setup {
      when(mockService.updateStatusPeriod(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(InvalidStatus)))

      val req: FakeRequest[UpdateStatusPeriodRequest] = FakeRequest().withBody(UpdateStatusPeriodRequest(2))
      val res: Future[Result] = controller.updateStatusPeriod("mgd", "XWM00000001770", 3)(req)

      status(res) mustBe BAD_REQUEST
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "INVALID_STATUS",
        "message" -> "status must be 0 (open) or 1 (closed)"
      )
    }

    "returns 400 when InvalidRegNumber" in new Setup {
      when(mockService.updateStatusPeriod(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(InvalidRegNumber)))

      val req: FakeRequest[UpdateStatusPeriodRequest] = FakeRequest().withBody(UpdateStatusPeriodRequest(1))
      val res: Future[Result] = controller.updateStatusPeriod("mgd", "InvalidRegNo", 1)(req)

      status(res) mustBe BAD_REQUEST
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "INVALID_REG_NUMBER",
        "message" -> "regNumber has invalid format"
      )
    }

    "returns 500 when UnexpectedError" in new Setup {
      when(mockService.updateStatusPeriod(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(UnexpectedError)))

      val req: FakeRequest[UpdateStatusPeriodRequest] = FakeRequest().withBody(UpdateStatusPeriodRequest(1))
      val res: Future[Result] = controller.updateStatusPeriod("mgd", "ERR00001770", 1)(req)

      status(res) mustBe INTERNAL_SERVER_ERROR
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "UNEXPECTED_ERROR",
        "message" -> "Unexpected error occurred"
      )
    }
  }
}
