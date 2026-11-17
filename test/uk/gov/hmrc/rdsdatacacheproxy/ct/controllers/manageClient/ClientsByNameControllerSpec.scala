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
import play.api.mvc.Result
import play.api.test.Helpers.*
import play.api.libs.json.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.manageClient.ClientsByNameHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.manageClient.ClientsByNameService

import scala.concurrent.Future

class ClientsByNameControllerSpec extends SpecBase with MockitoSugar with ClientsByNameHelper {

  private class Setup {
    val mockService: ClientsByNameService = mock[ClientsByNameService]
    val controller: ClientsByNameController = new ClientsByNameController(fakeAuthAction, mockService, cc)
  }

  "ClientsByNameControllerSpec" - {
    "return a 200 and a successful response for clients by name" in new Setup {
      when(mockService.getClientsByName(any[String], any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(clientsByName))

      val result: Future[Result] =
        controller.getClientsByName("irAgentId1", "credentialId1", "name1", 0, -1, 0, "ASC")(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(clientsByName)
      verify(mockService).getClientsByName("irAgentId1", "credentialId1", "name1", 0, -1, 0, "ASC")
    }

    "return a 200 and a successful response for multiple items of client list in clients by name" in new Setup {
      when(mockService.getClientsByName(any[String], any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(multipleClientListClientsByName))

      val result: Future[Result] =
        controller.getClientsByName("irAgentId2", "credentialId2", "name2", 0, -1, 0, "ASC")(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(multipleClientListClientsByName)
      verify(mockService).getClientsByName("irAgentId2", "credentialId2", "name2", 0, -1, 0, "ASC")
    }

    "return a 200 and a successful response for multiple items of client name chars in clients by name" in new Setup {
      when(mockService.getClientsByName(any[String], any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(multipleItemsClientNameClientsByName))

      val result: Future[Result] =
        controller.getClientsByName("irAgentId3", "credentialId3", "name3", 0, -1, 0, "ASC")(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(multipleItemsClientNameClientsByName)
      verify(mockService).getClientsByName("irAgentId3", "credentialId3", "name3", 0, -1, 0, "ASC")
    }

    "return 500 and when repository call fails" in new Setup {
      when(mockService.getClientsByName(any[String], any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.failed(new RuntimeException("Error")))

      val result: Future[Result] =
        controller.getClientsByName("irAgentIdNA", "credentialIdNA", "nameNA", 0, -1, 0, "ASC")(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getClientsByName("irAgentIdNA", "credentialIdNA", "nameNA", 0, -1, 0, "ASC")
    }

  }
}