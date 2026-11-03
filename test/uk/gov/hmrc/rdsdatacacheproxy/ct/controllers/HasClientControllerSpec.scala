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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers

import org.mockito.Mockito.{verify, when}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, contentType, status}
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.HasClient
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.HasClientService

import scala.concurrent.Future

class HasClientControllerSpec extends SpecBase {

  private trait Setup {
    val mockService: HasClientService = mock[HasClientService]

    val controller = new HasClientController(fakeAuthAction, mockService, cc)

    val utr: Long = 1234567890L
    val irAgentId: String = "AGENT123"
    val credentialId: String = "CREDENTIAL123"

    val clientExists: HasClient = HasClient(status = 1)
    val clientDoesNotExist: HasClient = HasClient(status = 0)
  }

  "getHasClient" - {

    "should return 200 OK when client exists" in new Setup {

      when(mockService.hasClient(utr, irAgentId, credentialId))
        .thenReturn(Future.successful(clientExists))

      val result: Future[Result] =
        controller.getHasClient(utr, irAgentId, credentialId)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(clientExists)

      verify(mockService).hasClient(utr, irAgentId, credentialId)
    }

    "should return 200 OK when client does not exist" in new Setup {

      when(mockService.hasClient(utr, irAgentId, credentialId))
        .thenReturn(Future.successful(clientDoesNotExist))

      val result: Future[Result] =
        controller.getHasClient(utr, irAgentId, credentialId)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(clientDoesNotExist)

      verify(mockService).hasClient(utr, irAgentId, credentialId)
    }

    "should return 500 when there is an exception returned from the service" in new Setup {

      when(mockService.hasClient(utr, irAgentId, credentialId))
        .thenReturn(Future.failed(new RuntimeException("Boom")))

      val result: Future[Result] =
        controller.getHasClient(utr, irAgentId, credentialId)(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService).hasClient(utr, irAgentId, credentialId)
    }
  }
}
