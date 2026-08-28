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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.rdsdatacacheproxy.actions.IdentifiedUserAction
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.cis.models.CisTaxpayer
import uk.gov.hmrc.rdsdatacacheproxy.cis.repositories.CisDatacacheRepository
import uk.gov.hmrc.rdsdatacacheproxy.cis.services.CisTaxpayerService

import scala.concurrent.Future

class CisTaxpayerControllerSpec extends SpecBase {
  "CisTaxpayerController#getInstanceIdByTaxReference" - {

    "returns 200 and instanceId wrapper when" - {
      val taxpayer = mkTaxpayer()
      "Organisation user requests their own employer ref" in new Setup {
        val givenTon = "754"
        val givenTor = "EZ10800"
        mockOrganisationUser(givenTon, givenTor)
        mockGetTaxpayerFromService(Future.successful(taxpayer))

        val req: FakeRequest[JsValue] = requestWithErJson(givenTon, givenTor)
        val res: Future[Result] = controller.getCisTaxpayerByTaxReference(req)

        status(res) mustBe OK
        contentType(res) mustBe Some(JSON)
        contentAsJson(res) mustBe Json.toJson(taxpayer)

        verify(mockService).getCisTaxpayerByTaxReference(givenTon, givenTor)
        verifyNoMoreInteractions(mockService)
      }
      "Agent user requests one of their client's employer ref" in new Setup {
        val givenAgentRef = "1234"
        val givenCredId = "2345"
        val givenClientTon = "754"
        val givenClientTor = "EZ10800"
        mockAgentUser(givenAgentRef, givenCredId)
        mockGetClientFromRepo(Some(taxpayer))

        val req: FakeRequest[JsValue] = requestWithErJson(givenClientTon, givenClientTor)
        val res: Future[Result] = controller.getCisTaxpayerByTaxReference(req)

        status(res) mustBe OK
        contentType(res) mustBe Some(JSON)
        contentAsJson(res) mustBe Json.toJson(taxpayer)

        verify(mockRepo).getClientByEmployerRef(givenAgentRef, givenCredId, s"$givenClientTon/$givenClientTor")
        verifyNoMoreInteractions(mockRepo)
      }
    }

    "returns 400 when" - {
      "JSON is an empty object" in new Setup {
        mockOrganisationUser()
        val req = makeJsonRequest(Json.obj())
        val res = controller.getCisTaxpayerByTaxReference(req)

        status(res) mustBe BAD_REQUEST
        (contentAsJson(res) \ "message").as[String] mustBe "Invalid JSON body"
      }

      "taxOfficeNumber is missing" in new Setup {
        mockOrganisationUser()
        val req = makeJsonRequest(Json.obj("taxOfficeReference" -> "test111"))
        val res = controller.getCisTaxpayerByTaxReference(req)

        status(res) mustBe BAD_REQUEST
        (contentAsJson(res) \ "message").as[String] mustBe "Invalid JSON body"
      }

      "taxOfficeReference is missing" in new Setup {
        mockOrganisationUser()
        val req = makeJsonRequest(Json.obj("taxOfficeNumber" -> "111"))
        val res = controller.getCisTaxpayerByTaxReference(req)

        status(res) mustBe BAD_REQUEST
        (contentAsJson(res) \ "message").as[String] mustBe "Invalid JSON body"
      }
    }

    "returns 403 when" - {
      "an Individual user attempts to access the API" in new Setup {
        mockIndividualUser()
        val req = requestWithErJson()
        val res = controller.getCisTaxpayerByTaxReference(req)

        status(res) mustBe FORBIDDEN
        (contentAsJson(res) \ "message").as[String] mustBe "User must be Organisation or Agent"
      }
    }

    "returns 404 when" - {
      "service throws NoSuchElementException" in new Setup {
        val givenTon = "754"
        val givenTor = "EZ10800"
        mockOrganisationUser(givenTon, givenTor)
        mockGetTaxpayerFromService(Future.failed(new NoSuchElementException("not found")))

        val req = requestWithErJson(givenTon, givenTor)
        val res = controller.getCisTaxpayerByTaxReference(req)

        status(res) mustBe NOT_FOUND
        (contentAsJson(res) \ "message").as[String] mustBe s"CIS taxpayer not found for TON=$givenTon, TOR=$givenTor"
      }

      "Organisation user's requested employer ref doesn't match the one retrieved from Auth" in new Setup {
        val requestedTon = "754"
        val requestedTor = "EZ10800"
        mockOrganisationUser("123", "AB12345") // The user's real employer ref is 123/AB12345

        val req = requestWithErJson(requestedTon, requestedTor)
        val res = controller.getCisTaxpayerByTaxReference(req)

        status(res) mustBe NOT_FOUND
        (contentAsJson(res) \ "message").as[String] mustBe s"CIS taxpayer not found for TON=$requestedTon, TOR=$requestedTor"
      }

      "Agent user's requested client isn't found in the database" in new Setup {
        val givenAgentRef = "1234"
        val givenCredId = "2345"
        val givenClientTon = "123"
        val givenClientTor = "AB12345"
        mockAgentUser(givenAgentRef, givenCredId)
        mockGetClientFromRepo(taxPayerOpt = None)

        val req = requestWithErJson(givenClientTon, givenClientTor)
        val res = controller.getCisTaxpayerByTaxReference(req)

        status(res) mustBe NOT_FOUND
        (contentAsJson(res) \ "message").as[String] mustBe s"CIS taxpayer not found for TON=$givenClientTon, TOR=$givenClientTor"

        verify(mockRepo).getClientByEmployerRef(givenAgentRef, givenCredId, s"$givenClientTon/$givenClientTor")
        verifyNoMoreInteractions(mockRepo)
      }
    }

    "propagates UpstreamErrorResponse status and message from service" in new Setup {
      val givenTon = "754"
      val givenTor = "EZ10800"
      mockOrganisationUser(givenTon, givenTor)

      val err = UpstreamErrorResponse("rds-datacache exploded", BAD_GATEWAY, BAD_GATEWAY)
      mockGetTaxpayerFromService(Future.failed(err))

      val req = requestWithErJson(givenTon, givenTor)
      val res = controller.getCisTaxpayerByTaxReference(req)

      status(res) mustBe BAD_GATEWAY
      (contentAsJson(res) \ "message").as[String] must include("rds-datacache exploded")
    }

    "returns 500 with generic message on unexpected exceptions" in new Setup {
      val givenTon = "754"
      val givenTor = "EZ10800"
      mockOrganisationUser(givenTon, givenTor)
      mockGetTaxpayerFromService(Future.failed(new RuntimeException("boom")))

      val req = requestWithErJson(givenTon, givenTor)
      val res = controller.getCisTaxpayerByTaxReference(req)

      status(res) mustBe INTERNAL_SERVER_ERROR
      (contentAsJson(res) \ "message").as[String] mustBe "Unexpected error"
    }
  }

  private trait Setup {
    private val mockAuthConnector = mock[AuthConnector]
    protected val mockRepo: CisDatacacheRepository = mock[CisDatacacheRepository]
    val mockService: CisTaxpayerService = mock[CisTaxpayerService]
    val controller = new CisTaxpayerController(new IdentifiedUserAction(mockAuthConnector), mockService, mockRepo, cc)

    def makeJsonRequest(body: JsValue) =
      FakeRequest(POST, "/cis-taxpayer")
        .withHeaders(CONTENT_TYPE -> JSON, ACCEPT -> JSON)
        .withBody(body)

    def requestWithErJson(ton: String = "111", tor: String = "test111") =
      makeJsonRequest(Json.obj("taxOfficeNumber" -> ton, "taxOfficeReference" -> tor))

    protected def mockIndividualUser() = mockAuthRetrieval(Individual)

    protected def mockOrganisationUser(ton: String = "111", tor: String = "test111") = mockAuthRetrieval(
      affinityGroup = Organisation,
      enrolments = Map(
        "HMRC-CIS-ORG" -> Map(
          "TaxOfficeNumber"    -> ton,
          "TaxOfficeReference" -> tor
        )
      )
    )

    protected def mockAgentUser(agentRef: String, credId: String): Unit = mockAuthRetrieval(
      affinityGroup = Agent,
      enrolments    = Map("IR-PAYE-AGENT" -> Map("IRAgentReference" -> agentRef)),
      credId        = credId
    )

    protected def mockGetTaxpayerFromService(taxPayerFut: Future[CisTaxpayer]): Unit =
      when(mockService.getCisTaxpayerByTaxReference(any, any)) thenReturn taxPayerFut

    protected def mockGetClientFromRepo(taxPayerOpt: Option[CisTaxpayer]): Unit =
      when(mockRepo.getClientByEmployerRef(any, any, any)) thenReturn Future.successful(taxPayerOpt)

    private def mockAuthRetrieval(
      affinityGroup: AffinityGroup,
      enrolments: Map[String, Map[String, String]] = Map.empty,
      credId: String = "1234"
    ): Unit =
      val mockRetrievedUserInfo = Some(affinityGroup) ~ makeEnrolments(enrolments.toSeq*) ~ Some(credId)
      when(mockAuthConnector.authorise(any, any)(any, any)) thenReturn Future.successful(mockRetrievedUserInfo)
  }
}
