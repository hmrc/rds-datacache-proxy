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
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.models.CisTaxpayer
import uk.gov.hmrc.rdsdatacacheproxy.services.CisTaxpayerService

import scala.concurrent.Future

class CisTaxpayerControllerSpec extends SpecBase with MockitoSugar{
  "CisTaxpayerController#getInstanceIdByTaxReference" - {

    "returns 200 and instanceId wrapper when service succeeds" in new Setup {
      val taxpayer = mkTaxpayer()
      when(mockService.getCisTaxpayerByTaxReference(eqTo("111"), eqTo("test111")))
        .thenReturn(Future.successful(taxpayer))

      val req: FakeRequest[JsValue] = requestWithErJson("111", "test111")
      val res: Future[Result]       = controller.getCisTaxpayerByTaxReference(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)
      contentAsJson(res) mustBe Json.toJson(taxpayer)
      verify(mockService).getCisTaxpayerByTaxReference(eqTo("111"), eqTo("test111"))
      verifyNoMoreInteractions(mockService)
    }

    "return 404 with NOT FOUND message when service throw NoSuchElementException" in new Setup {
      when(mockService.getCisTaxpayerByTaxReference(any[String], any[String]))
        .thenReturn(Future.failed(new NoSuchElementException("not found")))

      val req = requestWithErJson()
      val res = controller.getCisTaxpayerByTaxReference(req)

      status(res) mustBe NOT_FOUND
      (contentAsJson(res) \ "message").as[String] mustBe "CIS taxpayer not found for TON=111, TOR=test111"
    }

    "returns 400 when JSON is an empty object" in new Setup {
      val req  = makeJsonRequest(Json.obj())
      val res  = controller.getCisTaxpayerByTaxReference(req)
      status(res) mustBe BAD_REQUEST
      (contentAsJson(res) \ "message").as[String] mustBe "Invalid JSON body"
      verifyNoInteractions(mockService)
    }

    "returns 400 when taxOfficeNumber is missing" in new Setup {
      val req = makeJsonRequest(Json.obj("taxOfficeReference" -> "test111"))
      val res = controller.getCisTaxpayerByTaxReference(req)
      status(res) mustBe BAD_REQUEST
      (contentAsJson(res) \ "message").as[String] mustBe "Invalid JSON body"
      verifyNoInteractions(mockService)
    }

    "returns 400 when taxOfficeReference is missing" in new Setup {
      val req = makeJsonRequest(Json.obj("taxOfficeNumber" -> "111"))
      val res = controller.getCisTaxpayerByTaxReference(req)
      status(res) mustBe BAD_REQUEST
      (contentAsJson(res) \ "message").as[String] mustBe "Invalid JSON body"
      verifyNoInteractions(mockService)
    }

    "propagates UpstreamErrorResponse status and message from service" in new Setup {
      val err = UpstreamErrorResponse("rds-datacache exploded", BAD_GATEWAY, BAD_GATEWAY)
      when(mockService.getCisTaxpayerByTaxReference(any[String], any[String]))
        .thenReturn(Future.failed(err))

      val req = requestWithErJson()
      val res = controller.getCisTaxpayerByTaxReference(req)

      status(res) mustBe BAD_GATEWAY
      (contentAsJson(res) \ "message").as[String] must include ("rds-datacache exploded")
    }

    "returns 500 with generic message on unexpected exceptions" in new Setup {
      when(mockService.getCisTaxpayerByTaxReference(any[String], any[String]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val req = requestWithErJson()
      val res = controller.getCisTaxpayerByTaxReference(req)

      status(res) mustBe INTERNAL_SERVER_ERROR
      (contentAsJson(res) \ "message").as[String] mustBe "Unexpected error"
    }
  }

  private trait Setup {
    val mockService: CisTaxpayerService = mock[CisTaxpayerService]
    val controller = new CisTaxpayerController(fakeAuthAction, mockService, cc)

    def makeJsonRequest(body: JsValue) =
      FakeRequest(POST, "/cis-taxpayer")
        .withHeaders(CONTENT_TYPE -> JSON, ACCEPT -> JSON)
        .withBody(body)

    def requestWithErJson(ton: String = "111", tor: String = "test111") =
      makeJsonRequest(Json.obj("taxOfficeNumber" -> ton, "taxOfficeReference" -> tor))
  }
}