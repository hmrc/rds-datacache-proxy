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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError.{InvalidRegNumber, UnexpectedError}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.services.SubmittedReturnsService
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseSubmittedReturns

import scala.concurrent.Future

class SubmittedReturnsControllerSpec extends SpecBase with MockitoSugar {

  private trait Setup {
    val mockService: SubmittedReturnsService = mock[SubmittedReturnsService]
    val controller = new SubmittedReturnsController(fakeAuthAction, mockService, cc)
  }

  private val ASC = Some("ASC")

  "SubmittedReturnsController#getSubmittedReturns" - {

    "returns 200 when service succeeds" in new Setup {

      when(mockService.getSubmittedReturns(eqTo("XWM00000001770"), eqTo(Some(1)), eqTo(ASC))(any()))
        .thenReturn(Future.successful(Right(validResponseSubmittedReturns)))

      val req = FakeRequest(GET, s"/gambling/submitted-returns/XWM00000001770?sortBy=1&orderBy=ASC")
      val res: Future[Result] = controller.getSubmittedReturns("XWM00000001770", Some(1), ASC)(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)
      contentAsJson(res) mustBe Json.toJson(validResponseSubmittedReturns)

      verify(mockService).getSubmittedReturns(eqTo("XWM00000001770"), eqTo(Some(1)), eqTo(ASC))(any())
      verifyNoMoreInteractions(mockService)
    }

    "returns 400 when InvalidRegNumber" in new Setup {
      when(mockService.getSubmittedReturns(any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(InvalidRegNumber)))

      val req = FakeRequest(GET, s"/gambling/submitted-returns/InvalidRegNo")
      val res: Future[Result] = controller.getSubmittedReturns(" ", Some(1), ASC)(req)

      status(res) mustBe BAD_REQUEST
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "INVALID_REG_NUMBER",
        "message" -> "regNumber has invalid format"
      )

      verify(mockService).getSubmittedReturns(eqTo(" "), eqTo(Some(1)), eqTo(ASC))(any())
    }

    "returns 500 when UnexpectedError" in new Setup {
      when(mockService.getSubmittedReturns(any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(UnexpectedError)))

      val req = FakeRequest(GET, s"/gambling/submitted-returns/ERR00001770")
      val res: Future[Result] = controller.getSubmittedReturns("ERR00001770", Some(1), ASC)(req)

      status(res) mustBe INTERNAL_SERVER_ERROR
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "UNEXPECTED_ERROR",
        "message" -> "Unexpected error occurred"
      )

      verify(mockService).getSubmittedReturns(eqTo("ERR00001770"), eqTo(Some(1)), eqTo(ASC))(any())
    }
  }
}
