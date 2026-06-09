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
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError.{InvalidRegNumber, InvalidRegimeCode, UnexpectedError}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.services.AccountOverviewService
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.{validRegime, validResponseAccountOverview}

import scala.concurrent.Future

class AccountOverviewControllerSpec extends SpecBase with MockitoSugar {

  private trait Setup {
    val mockService: AccountOverviewService = mock[AccountOverviewService]
    val controller = new AccountOverviewController(fakeAuthAction, mockService, cc)
  }

  "AccountOverviewController#getAccountOverview" - {

    "returns 200 with account overview JSON when service succeeds" in new Setup {
      when(mockService.getAccountOverview(eqTo(validRegime), eqTo("XWM00000001770"))(any()))
        .thenReturn(Future.successful(Right(validResponseAccountOverview)))

      val req = FakeRequest(GET, s"/gambling/account-overview/$validRegime/XWM00000001770")
      val res: Future[Result] = controller.getAccountOverview(validRegime, "XWM00000001770")(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)
      contentAsJson(res) mustBe Json.toJson(validResponseAccountOverview)

      verify(mockService).getAccountOverview(eqTo(validRegime), eqTo("XWM00000001770"))(any())
      verifyNoMoreInteractions(mockService)
    }

    "returns 400 when InvalidRegimeCode" in new Setup {
      when(mockService.getAccountOverview(any(), any())(any()))
        .thenReturn(Future.successful(Left(InvalidRegimeCode)))

      val req = FakeRequest(GET, "/gambling/account-overview/INVALID/XWM00000001770")
      val res: Future[Result] = controller.getAccountOverview("INVALID", "XWM00000001770")(req)

      status(res) mustBe BAD_REQUEST
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "INVALID_REGIME_CODE",
        "message" -> "Invalid Regime Code"
      )

      verify(mockService).getAccountOverview(eqTo("INVALID"), eqTo("XWM00000001770"))(any())
    }

    "returns 400 when InvalidRegNumber" in new Setup {
      when(mockService.getAccountOverview(any(), any())(any()))
        .thenReturn(Future.successful(Left(InvalidRegNumber)))

      val req = FakeRequest(GET, s"/gambling/account-overview/$validRegime/BADREGNO")
      val res: Future[Result] = controller.getAccountOverview(validRegime, "BADREGNO")(req)

      status(res) mustBe BAD_REQUEST
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "INVALID_REG_NUMBER",
        "message" -> "regNumber has invalid format"
      )

      verify(mockService).getAccountOverview(eqTo(validRegime), eqTo("BADREGNO"))(any())
    }

    "returns 500 when UnexpectedError" in new Setup {
      when(mockService.getAccountOverview(any(), any())(any()))
        .thenReturn(Future.successful(Left(UnexpectedError)))

      val req = FakeRequest(GET, s"/gambling/account-overview/$validRegime/XWM00000001770")
      val res: Future[Result] = controller.getAccountOverview(validRegime, "XWM00000001770")(req)

      status(res) mustBe INTERNAL_SERVER_ERROR
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "UNEXPECTED_ERROR",
        "message" -> "Unexpected error occurred"
      )

      verify(mockService).getAccountOverview(eqTo(validRegime), eqTo("XWM00000001770"))(any())
    }
  }
}
