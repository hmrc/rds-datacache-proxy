/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy.controllers

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.any
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.models.{MonthlyReturn, UserMonthlyReturns}
import uk.gov.hmrc.rdsdatacacheproxy.services.MonthlyReturnService

import java.time.LocalDateTime
import scala.concurrent.Future

class MonthlyReturnControllerSpec extends SpecBase with MockitoSugar{
  "MonthlyReturnController" - {

    "retrieveMonthlyReturns" - {

      "return 200 and a successful response when JSON body is present" in new SetUp {
        when(mockMonthlyReturnService.retrieveMonthlyReturns(any[String], any[String]))
          .thenReturn(Future.successful(nonEmptyResponse))

        val req: Request[JsValue] = requestWithErJson()
        val result: Future[Result] = controller.retrieveMonthlyReturns(req)

        status(result) shouldBe OK
        contentType(result) shouldBe Some("application/json")
        contentAsJson(result) shouldBe Json.toJson(nonEmptyResponse)
      }

      "return 200 and an empty monthly returns when service returns none" in new SetUp {
        when(mockMonthlyReturnService.retrieveMonthlyReturns(any[String], any[String]))
          .thenReturn(Future.successful(emptyResponse))

        val req: Request[JsValue] = requestWithErJson()
        val result: Future[Result] = controller.retrieveMonthlyReturns(req)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(emptyResponse)
      }

      "return 400 when JSON is missing required fields (missing taxOfficeNumber)" in new SetUp {
        val reqMissingTon: Request[JsValue] = makeJsonRequest(Json.obj("taxOfficeReference" -> "ABC"))
        val res: Future[Result] = controller.retrieveMonthlyReturns(reqMissingTon)

        status(res) shouldBe BAD_REQUEST
        (contentAsJson(res) \ "message").as[String] shouldBe "Invalid JSON body"
        verifyNoInteractions(mockMonthlyReturnService)
      }

      "return 400 when JSON is missing required fields (missing taxOfficeReference)" in new SetUp {
        val reqMissingTor: Request[JsValue] = makeJsonRequest(Json.obj("taxOfficeNumber" -> "123"))
        val res: Future[Result] = controller.retrieveMonthlyReturns(reqMissingTor)

        status(res) shouldBe BAD_REQUEST
        (contentAsJson(res) \ "message").as[String] shouldBe "Invalid JSON body"
        verifyNoInteractions(mockMonthlyReturnService)
      }

      "return 400 when JSON body is an empty object" in new SetUp {
        val reqEmpty: Request[JsValue] = makeJsonRequest(Json.obj())
        val res: Future[Result] = controller.retrieveMonthlyReturns(reqEmpty)

        status(res) shouldBe BAD_REQUEST
        (contentAsJson(res) \ "message").as[String] shouldBe "Invalid JSON body"
        verifyNoInteractions(mockMonthlyReturnService)
      }

      "propagate external service error status and message" in new SetUp {
        val err: UpstreamErrorResponse = UpstreamErrorResponse("External service failure", BAD_GATEWAY, BAD_GATEWAY)

        when(mockMonthlyReturnService.retrieveMonthlyReturns(any[String], any[String]))
          .thenReturn(Future.failed(err))

        val req: Request[JsValue] = requestWithErJson()
        val result: Future[Result] = controller.retrieveMonthlyReturns(req)

        status(result) shouldBe BAD_GATEWAY
        (contentAsJson(result) \ "message").as[String] shouldBe "External service failure"
      }

      "return 500 with a generic message on unexpected exceptions" in new SetUp {
        when(mockMonthlyReturnService.retrieveMonthlyReturns(any[String], any[String]))
          .thenReturn(Future.failed(new RuntimeException("boom")))

        val req: Request[JsValue] = requestWithErJson()
        val result: Future[Result] = controller.retrieveMonthlyReturns(req)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        (contentAsJson(result) \ "message").as[String] shouldBe "Unexpected error"
      }
    }
  }


  private class SetUp {
    val mockMonthlyReturnService: MonthlyReturnService = mock[MonthlyReturnService]

    def requestWithErJson(ton: String = "111", tor: String = "test111"): Request[JsValue] =
      makeJsonRequest(Json.obj("taxOfficeNumber" -> ton, "taxOfficeReference" -> tor))

    def makeJsonRequest(body: JsValue): Request[JsValue] =
      FakeRequest(POST, "/rds-datacache-proxy/monthly-returns")
        .withHeaders(CONTENT_TYPE -> JSON)
        .withBody[JsValue](body)

    private def mkReturn(id: Long, month: Int, year: Int = 2025): MonthlyReturn =
      MonthlyReturn(
        monthlyReturnId = id,
        taxYear = year,
        taxMonth = month,
        nilReturnIndicator = Some("N"),
        decEmpStatusConsidered = Some("Y"),
        decAllSubsVerified = Some("Y"),
        decInformationCorrect = Some("Y"),
        decNoMoreSubPayments = Some("N"),
        decNilReturnNoPayments = Some("N"),
        status = Some("SUBMITTED"),
        lastUpdate = Some(LocalDateTime.parse("2025-01-01T00:00:00")),
        amendment = Some("N"),
        supersededBy = None
      )

    val emptyResponse: UserMonthlyReturns =
      UserMonthlyReturns(monthlyReturnList = Seq.empty)

    val nonEmptyResponse: UserMonthlyReturns =
      UserMonthlyReturns(monthlyReturnList = Seq(
        mkReturn(66666L, 1),
        mkReturn(66667L, 7)
      ))

    val controller =
      new MonthlyReturnController(fakeAuthAction, mockMonthlyReturnService, cc)
  }
}