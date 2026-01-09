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

package uk.gov.hmrc.rdsdatacacheproxy.cis.controllers

import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.{anyString, eq as eqTo}
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.cis.models.{SchemePrepop, SubcontractorPrepopRecord}
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.cis.services.PrepopService

import scala.concurrent.Future

class PrepopControllerSpec extends SpecBase with MockitoSugar {

  "PrepopController.getSchemePrepopByKnownFacts" - {

    "returns 200 and wrapped contractor pre-pop details when service succeeds" in new Setup {
      val scheme = SchemePrepop(
        taxOfficeNumber        = "123",
        taxOfficeReference     = "AB456",
        accountOfficeReference = "123PA12345678",
        utr                    = Some("1123456789"),
        schemeName             = "PAL-355 Scheme"
      )

      when(
        mockService.getSchemePrepopByKnownFacts(
          eqTo("123"),
          eqTo("AB456"),
          eqTo("123PA12345678")
        )
      ).thenReturn(Future.successful(scheme))

      val req: FakeRequest[JsValue] =
        requestWithKnownFactsJson("123", "AB456", "123PA12345678")

      val res: Future[Result] = controller.getSchemePrepopByKnownFacts(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)

      val json = contentAsJson(res)

      (json \ "knownfacts" \ "taxOfficeNumber").as[String] mustBe "123"
      (json \ "knownfacts" \ "taxOfficeReference").as[String] mustBe "AB456"
      (json \ "knownfacts" \ "accountOfficeReference").as[String] mustBe "123PA12345678"

      (json \ "prePopContractor" \ "schemeName").as[String] mustBe "PAL-355 Scheme"
      (json \ "prePopContractor" \ "utr").as[String] mustBe "1123456789"
      (json \ "prePopContractor" \ "response").as[Int] mustBe 0

      verify(mockService).getSchemePrepopByKnownFacts(
        eqTo("123"),
        eqTo("AB456"),
        eqTo("123PA12345678")
      )
      verifyNoMoreInteractions(mockService)
    }

    "uses empty string when utr is None" in new Setup {
      val scheme = SchemePrepop(
        taxOfficeNumber        = "123",
        taxOfficeReference     = "AB456",
        accountOfficeReference = "123PA12345678",
        utr                    = None,
        schemeName             = "PAL-355 Scheme"
      )

      when(
        mockService.getSchemePrepopByKnownFacts(
          eqTo("123"),
          eqTo("AB456"),
          eqTo("123PA12345678")
        )
      ).thenReturn(Future.successful(scheme))

      val req = requestWithKnownFactsJson("123", "AB456", "123PA12345678")
      val res = controller.getSchemePrepopByKnownFacts(req)

      status(res) mustBe OK

      val json = contentAsJson(res)
      (json \ "prePopContractor" \ "utr").as[String] mustBe ""
    }

    "returns 404 with NOT FOUND message when service throws NoSuchElementException" in new Setup {
      when(mockService.getSchemePrepopByKnownFacts(anyString(), anyString(), anyString()))
        .thenReturn(Future.failed(new NoSuchElementException("not found")))

      val req = requestWithKnownFactsJson()
      val res = controller.getSchemePrepopByKnownFacts(req)

      status(res) mustBe NOT_FOUND
      (contentAsJson(res) \ "message").as[String] mustBe
        "No CIS scheme pre-pop data found for TON=123, TOR=AB456, AO=123PA12345678"
    }

    "returns 400 when JSON is an empty object" in new Setup {
      val req = makeSchemeJsonRequest(Json.obj())
      val res = controller.getSchemePrepopByKnownFacts(req)

      status(res) mustBe BAD_REQUEST
      (contentAsJson(res) \ "message").as[String] mustBe "Invalid JSON body"
      verifyNoInteractions(mockService)
    }

    "returns 400 when taxOfficeNumber is missing" in new Setup {
      val req = makeSchemeJsonRequest(
        Json.obj(
          "taxOfficeReference"     -> "AB456",
          "accountOfficeReference" -> "123PA12345678"
        )
      )
      val res = controller.getSchemePrepopByKnownFacts(req)

      status(res) mustBe BAD_REQUEST
      (contentAsJson(res) \ "message").as[String] mustBe "Invalid JSON body"
      verifyNoInteractions(mockService)
    }

    "returns 400 when taxOfficeReference is missing" in new Setup {
      val req = makeSchemeJsonRequest(
        Json.obj(
          "taxOfficeNumber"        -> "123",
          "accountOfficeReference" -> "123PA12345678"
        )
      )
      val res = controller.getSchemePrepopByKnownFacts(req)

      status(res) mustBe BAD_REQUEST
      (contentAsJson(res) \ "message").as[String] mustBe "Invalid JSON body"
      verifyNoInteractions(mockService)
    }

    "returns 400 when accountOfficeReference is missing" in new Setup {
      val req = makeSchemeJsonRequest(
        Json.obj(
          "taxOfficeNumber"    -> "123",
          "taxOfficeReference" -> "AB456"
        )
      )
      val res = controller.getSchemePrepopByKnownFacts(req)

      status(res) mustBe BAD_REQUEST
      (contentAsJson(res) \ "message").as[String] mustBe "Invalid JSON body"
      verifyNoInteractions(mockService)
    }

    "returns 500 with generic message on unexpected exceptions" in new Setup {
      when(mockService.getSchemePrepopByKnownFacts(anyString(), anyString(), anyString()))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val req = requestWithKnownFactsJson()
      val res = controller.getSchemePrepopByKnownFacts(req)

      status(res) mustBe INTERNAL_SERVER_ERROR
      (contentAsJson(res) \ "message").as[String] mustBe "Unexpected error"
    }
  }

  "PrepopController.getSubcontractorsPrepopByKnownFacts" - {

    "returns 200 and wrapped subcontractor pre-pop details when service succeeds" in new Setup {
      val sub = SubcontractorPrepopRecord(
        subcontractorType  = "I",
        subcontractorUtr   = "1123456789",
        verificationNumber = "12345678901",
        verificationSuffix = Some("AB"),
        title              = Some("Mr"),
        firstName          = Some("Bob"),
        secondName         = None,
        surname            = Some("Builder"),
        tradingName        = Some("Bob Builder Ltd")
      )

      when(
        mockService.getSubcontractorsPrepopByKnownFacts(
          eqTo("123"),
          eqTo("AB456"),
          eqTo("123PA12345678")
        )
      ).thenReturn(Future.successful(Seq(sub)))

      val req: FakeRequest[JsValue] =
        requestWithKnownFactsJson("123", "AB456", "123PA12345678")

      val res: Future[Result] = controller.getSubcontractorsPrepopByKnownFacts(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)

      val json = contentAsJson(res)

      (json \ "knownfacts" \ "taxOfficeNumber").as[String] mustBe "123"
      (json \ "knownfacts" \ "taxOfficeReference").as[String] mustBe "AB456"
      (json \ "knownfacts" \ "accountOfficeReference").as[String] mustBe "123PA12345678"

      (json \ "prePopSubcontractors" \ "response").as[Int] mustBe 0

      val subJson = (json \ "prePopSubcontractors" \ "subcontractors")(0)

      (subJson \ "subcontractorType").as[String] mustBe "I"
      (subJson \ "utr").as[String] mustBe "1123456789"
      (subJson \ "verificationNumber").as[String] mustBe "12345678901"
      (subJson \ "verificationSuffix").as[String] mustBe "AB"
      (subJson \ "title").as[String] mustBe "Mr"
      (subJson \ "firstName").as[String] mustBe "Bob"
      (subJson \ "secondName").as[String] mustBe ""
      (subJson \ "surname").as[String] mustBe "Builder"

      verify(mockService).getSubcontractorsPrepopByKnownFacts(
        eqTo("123"),
        eqTo("AB456"),
        eqTo("123PA12345678")
      )
      verifyNoMoreInteractions(mockService)
    }

    "returns 404 with NOT FOUND message when service throws NoSuchElementException" in new Setup {
      when(mockService.getSubcontractorsPrepopByKnownFacts(anyString(), anyString(), anyString()))
        .thenReturn(Future.failed(new NoSuchElementException("not found")))

      val req = requestWithKnownFactsJson()
      val res = controller.getSubcontractorsPrepopByKnownFacts(req)

      status(res) mustBe NOT_FOUND
      (contentAsJson(res) \ "message").as[String] mustBe
        "No CIS subcontractor pre-pop data found for TON=123, TOR=AB456, AO=123PA12345678"
    }

    "returns 400 when JSON is an empty object" in new Setup {
      val req = makeSubcontractorJsonRequest(Json.obj())
      val res = controller.getSubcontractorsPrepopByKnownFacts(req)

      status(res) mustBe BAD_REQUEST
      (contentAsJson(res) \ "message").as[String] mustBe "Invalid JSON body"
      verifyNoInteractions(mockService)
    }

    "returns 400 when taxOfficeNumber is missing" in new Setup {
      val req = makeSubcontractorJsonRequest(
        Json.obj(
          "taxOfficeReference"     -> "AB456",
          "accountOfficeReference" -> "123PA12345678"
        )
      )
      val res = controller.getSubcontractorsPrepopByKnownFacts(req)

      status(res) mustBe BAD_REQUEST
      (contentAsJson(res) \ "message").as[String] mustBe "Invalid JSON body"
      verifyNoInteractions(mockService)
    }

    "returns 400 when taxOfficeReference is missing" in new Setup {
      val req = makeSubcontractorJsonRequest(
        Json.obj(
          "taxOfficeNumber"        -> "123",
          "accountOfficeReference" -> "123PA12345678"
        )
      )
      val res = controller.getSubcontractorsPrepopByKnownFacts(req)

      status(res) mustBe BAD_REQUEST
      (contentAsJson(res) \ "message").as[String] mustBe "Invalid JSON body"
      verifyNoInteractions(mockService)
    }

    "returns 400 when accountOfficeReference is missing" in new Setup {
      val req = makeSubcontractorJsonRequest(
        Json.obj(
          "taxOfficeNumber"    -> "123",
          "taxOfficeReference" -> "AB456"
        )
      )
      val res = controller.getSubcontractorsPrepopByKnownFacts(req)

      status(res) mustBe BAD_REQUEST
      (contentAsJson(res) \ "message").as[String] mustBe "Invalid JSON body"
      verifyNoInteractions(mockService)
    }

    "returns 500 with generic message on unexpected exceptions" in new Setup {
      when(mockService.getSubcontractorsPrepopByKnownFacts(anyString(), anyString(), anyString()))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val req = requestWithKnownFactsJson()
      val res = controller.getSubcontractorsPrepopByKnownFacts(req)

      status(res) mustBe INTERNAL_SERVER_ERROR
      (contentAsJson(res) \ "message").as[String] mustBe "Unexpected error"
    }
  }

  private trait Setup {
    val mockService: PrepopService =
      mock[PrepopService]

    val controller =
      new PrepopController(fakeAuthAction, mockService, cc)

    def makeSchemeJsonRequest(body: JsValue): FakeRequest[JsValue] =
      FakeRequest(POST, "/cis/prepop-contractor")
        .withHeaders(CONTENT_TYPE -> JSON, ACCEPT -> JSON)
        .withBody(body)

    def makeSubcontractorJsonRequest(body: JsValue): FakeRequest[JsValue] =
      FakeRequest(POST, "/cis/prepop-subcontractor")
        .withHeaders(CONTENT_TYPE -> JSON, ACCEPT -> JSON)
        .withBody(body)

    def requestWithKnownFactsJson(
      taxOfficeNumber: String = "123",
      taxOfficeReference: String = "AB456",
      accountOfficeReference: String = "123PA12345678"
    ): FakeRequest[JsValue] =
      makeSchemeJsonRequest(
        Json.obj(
          "taxOfficeNumber"        -> taxOfficeNumber,
          "taxOfficeReference"     -> taxOfficeReference,
          "accountOfficeReference" -> accountOfficeReference
        )
      )
  }
}
