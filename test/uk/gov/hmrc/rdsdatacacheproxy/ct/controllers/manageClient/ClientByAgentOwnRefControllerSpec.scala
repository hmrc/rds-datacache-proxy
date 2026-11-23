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
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.*
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.manageClient.AllClientsHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.manageClient.ClientsByAgentOwnRefService

import scala.concurrent.Future

class ClientByAgentOwnRefControllerSpec extends SpecBase with MockitoSugar with AllClientsHelper {

  private class Setup {
    val mockService: ClientsByAgentOwnRefService = mock[ClientsByAgentOwnRefService]
    val controller: ClientsByAgentOwnRefController = new ClientsByAgentOwnRefController(fakeAuthAction, mockService, cc)
  }

  "ClientByAgentOwnRefControllerSpec" - {
    "return a 200 and a successful response for clients by agent own ref" in new Setup {
      when(mockService.getClientsByAgentOwnRef(any[String], any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(allClients))

      val result: Future[Result] =
        controller.getClientsByAgentOwnRef("irAgentId1", "credentialId1", "clientId1", 0, -1, 0, "ASC")(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(allClients)
      verify(mockService).getClientsByAgentOwnRef("irAgentId1", "credentialId1", "clientId1", 0, -1, 0, "ASC")
    }

    "return a 200 and a successful response for multiple items of client list in clients by agent-own-ref" in new Setup {
      when(mockService.getClientsByAgentOwnRef(any[String], any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(multipleClientListAllClients))

      val result: Future[Result] =
        controller.getClientsByAgentOwnRef("irAgentId2", "credentialId2", "clientId2", 0, -1, 0, "ASC")(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(multipleClientListAllClients)
      verify(mockService).getClientsByAgentOwnRef("irAgentId2", "credentialId2", "clientId2", 0, -1, 0, "ASC")
    }

    "return a 200 and a successful response for multiple items of client name chars in clients by agent-own-ref" in new Setup {
      when(mockService.getClientsByAgentOwnRef(any[String], any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(multipleItemsClientNameAllClients))

      val result: Future[Result] =
        controller.getClientsByAgentOwnRef("irAgentId3", "credentialId3", "clientId3", 0, -1, 0, "ASC")(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(multipleItemsClientNameAllClients)
      verify(mockService).getClientsByAgentOwnRef("irAgentId3", "credentialId3", "clientId3", 0, -1, 0, "ASC")
    }

    "return 500 and when repository call fails" in new Setup {
      when(mockService.getClientsByAgentOwnRef(any[String], any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.failed(new RuntimeException("Error")))

      val result: Future[Result] =
        controller.getClientsByAgentOwnRef("irAgentIdNA", "credentialIdNA", "clientIdNA", 0, -1, 0, "ASC")(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getClientsByAgentOwnRef("irAgentIdNA", "credentialIdNA", "clientIdNA", 0, -1, 0, "ASC")
    }

  }
}
