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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers.manageClient

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, contentType, status}
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.manageClient.{AllClients, ClientList}
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.manageClient.GetClientByCrnService

import scala.concurrent.Future

class GetClientByCrnControllerSpec extends SpecBase {

  private trait Setup {
    val mockService: GetClientByCrnService = mock[GetClientByCrnService]

    val irAgentReference = "STN006"
    val credentialId = "78366363"
    val companyRegNo = "REG-67-63"

    val controller = new GetClientByCrnController(cc, fakeAuthAction, mockService)

    val emptyResponse: AllClients = AllClients(
      clientCount          = 0,
      clientList           = List.empty,
      clientNameStartChars = List.empty
    )

    val multipleClients: List[ClientList] = List(
      ClientList("1234567890", "AB123456", "Acme Trading Ltd", "AGT-REF-001"),
      ClientList("2345678901", "CD234567", "Brightwater Holdings", "AGT-REF-002"),
      ClientList("3456789012", "EF345678", "Bluefield Logistics", "AGT-REF-003"),
      ClientList("4567890123", "GH456789", "Zenith Consulting Ltd", "AGT-REF-004")
    )

    val multipleObjectsResponse: AllClients = AllClients(
      clientCount          = multipleClients.size,
      clientList           = multipleClients,
      clientNameStartChars = multipleClients.map(_.companyName.take(1).toUpperCase).distinct.sorted
    )
  }

  "getClientByCrn" - {

    "should return 200 OK with empty list of AllClients " in new Setup {

      when(mockService.getClientByCrn(any(), any(), any())).thenReturn(Future.successful(emptyResponse))

      val result: Future[Result] = controller.getClientByCrn(irAgentReference, credentialId, companyRegNo)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(emptyResponse)

      verify(mockService).getClientByCrn(any(), any(), any())
      verify(mockService, times(1)).getClientByCrn(any(), any(), any())

    }
    "should return 200 OK with AllClients containing multiple Clients " in new Setup {

      when(mockService.getClientByCrn(any(), any(), any())).thenReturn(Future.successful(multipleObjectsResponse))

      val result: Future[Result] = controller.getClientByCrn(irAgentReference, credentialId, companyRegNo)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(multipleObjectsResponse)

      verify(mockService).getClientByCrn(any(), any(), any())
      verify(mockService, times(1)).getClientByCrn(any(), any(), any())

    }
    "should return 500 when there is  an exception returned from the service " in new Setup {

      when(mockService.getClientByCrn(any(), any(), any())).thenReturn(Future.failed(new RuntimeException("Boom")))

      val result: Future[Result] = controller.getClientByCrn(irAgentReference, credentialId, companyRegNo)(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService).getClientByCrn(any(), any(), any())
      verify(mockService, times(1)).getClientByCrn(any(), any(), any())
    }
  }

}
