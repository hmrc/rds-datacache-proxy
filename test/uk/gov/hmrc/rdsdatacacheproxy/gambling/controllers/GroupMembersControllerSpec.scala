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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.GamblingError.{InvalidMgdRegNumber, InvalidRegimeCode, UnexpectedError}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.services.GamblingService
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.{validRegime, validResponseGroupMembers}

import scala.concurrent.Future

class GroupMembersControllerSpec extends SpecBase with MockitoSugar {

  private trait Setup {
    val mockService: GamblingService = mock[GamblingService]
    val controller = new GroupMembersController(fakeAuthAction, mockService, cc)
  }

  "GroupMembersController#getGroupMembers" - {

    "returns 200 when service succeeds" in new Setup {

      when(mockService.getGroupMembers(eqTo(validRegime), eqTo("XWM00000001770"))(any()))
        .thenReturn(Future.successful(Right(validResponseGroupMembers)))

      val req = FakeRequest(GET, s"/gambling/group-members/$validRegime/XWM00000001770")
      val res: Future[Result] = controller.getGroupMembers(validRegime, "XWM00000001770")(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)
      contentAsJson(res) mustBe Json.toJson(validResponseGroupMembers)

      verify(mockService).getGroupMembers(eqTo(validRegime), eqTo("XWM00000001770"))(any())
      verifyNoMoreInteractions(mockService)
    }

    "returns 400 when InvalidRegimeError" in new Setup {
      when(mockService.getGroupMembers(any(), any())(any()))
        .thenReturn(Future.successful(Left(InvalidRegimeCode)))

      val req = FakeRequest(GET, "/gambling/group-members/INVALID_REGIME/XWM00000001770")
      val res: Future[Result] = controller.getGroupMembers(" ", " ")(req)

      status(res) mustBe BAD_REQUEST
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "INVALID_REGIME_CODE",
        "message" -> "Invalid Regime Code"
      )

      verify(mockService).getGroupMembers(eqTo(" "), eqTo(" "))(any())
    }

    "returns 400 when InvalidMgdRegNumber" in new Setup {
      when(mockService.getGroupMembers(any(), any())(any()))
        .thenReturn(Future.successful(Left(InvalidMgdRegNumber)))

      val req = FakeRequest(GET, s"/gambling/group-members/$validRegime/InvalidRegNo")
      val res: Future[Result] = controller.getGroupMembers(" ", " ")(req)

      status(res) mustBe BAD_REQUEST
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "INVALID_MGD_REG_NUMBER",
        "message" -> "mgdRegNumber does not exist"
      )

      verify(mockService).getGroupMembers(eqTo(" "), eqTo(" "))(any())
    }

    "returns 500 when UnexpectedError" in new Setup {
      when(mockService.getGroupMembers(any(), any())(any()))
        .thenReturn(Future.successful(Left(UnexpectedError)))

      val req = FakeRequest(GET, s"/gambling/group-members/$validRegime/ERR00001770")
      val res: Future[Result] = controller.getGroupMembers(validRegime, "ERR00001770")(req)

      status(res) mustBe INTERNAL_SERVER_ERROR
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "UNEXPECTED_ERROR",
        "message" -> "Unexpected error occurred"
      )

      verify(mockService).getGroupMembers(eqTo(validRegime), eqTo("ERR00001770"))(any())
    }
  }
}
