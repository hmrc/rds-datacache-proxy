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

package uk.gov.hmrc.rdsdatacacheproxy.ct.services.manageClient

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.manageClient.AllClientsHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.manageClient.ClientsByAgentOwnRefRepository

import scala.concurrent.Future

class ClientsByAgentOwnRefServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar with AllClientsHelper {

  private trait Setup {

    val mockRepo: ClientsByAgentOwnRefRepository = mock[ClientsByAgentOwnRefRepository]

    val service = new ClientsByAgentOwnRefService(mockRepo)

  }

  "ClientsByAgentOwnRefServiceSpec" - {
    "must return clients by agent own ref" in new Setup {
      when(mockRepo.getClientsByAgentOwnRef(any[String], any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(allClients))

      val result = service.getClientsByAgentOwnRef("irAgentId1", "credentialId1", "clientId", 0, -1, 0, "ASC").futureValue

      result mustBe allClients

      verify(mockRepo, times(1)).getClientsByAgentOwnRef("irAgentId1", "credentialId1", "clientId", 0, -1, 0, "ASC")
    }

    "must return clients by agent own ref with multiple items of client list" in new Setup {
      when(mockRepo.getClientsByAgentOwnRef(any[String], any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(multipleClientListAllClients))

      val result = service.getClientsByAgentOwnRef("irAgentId2", "credentialId2", "clientId", 0, -1, 0, "ASC").futureValue

      result mustBe multipleClientListAllClients

      verify(mockRepo, times(1)).getClientsByAgentOwnRef("irAgentId2", "credentialId2", "clientId", 0, -1, 0, "ASC")

    }

    "must return clients by agent own ref with multiple items of client name chars" in new Setup {
      when(mockRepo.getClientsByAgentOwnRef(any[String], any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(multipleItemsClientNameAllClients))

      val result = service.getClientsByAgentOwnRef("irAgentId3", "credentialId3", "clientId", 0, -1, 0, "ASC").futureValue

      result mustBe multipleItemsClientNameAllClients

      verify(mockRepo, times(1)).getClientsByAgentOwnRef("irAgentId3", "credentialId3", "clientId", 0, -1, 0, "ASC")
    }

    "must return clients by agent own ref with a different combo of params" in new Setup {
      when(mockRepo.getClientsByAgentOwnRef(any[String], any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(multipleItemsClientNameAllClients))

      val result = service.getClientsByAgentOwnRef("irAgentId4", "credentialId4", "clientId4", 0, -1, 3, "DESC").futureValue

      result mustBe multipleItemsClientNameAllClients

      verify(mockRepo, times(1)).getClientsByAgentOwnRef("irAgentId4", "credentialId4", "clientId4", 0, -1, 3, "DESC")
    }

    "must propagate failure from repository" in new Setup {
      val exception = new RuntimeException("Error")

      when(mockRepo.getClientsByAgentOwnRef(any[String], any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.failed(exception))

      val result = service.getClientsByAgentOwnRef("irAgentIdNA", "credentialIdNA", "clientId", 0, -1, 0, "ASC").failed.futureValue

      result mustBe exception

      verify(mockRepo, times(1)).getClientsByAgentOwnRef("irAgentIdNA", "credentialIdNA", "clientId", 0, -1, 0, "ASC")
    }
  }

}
