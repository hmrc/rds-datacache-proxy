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
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.manageClient.ClientsByNameHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.manageClient.ClientsByNameRepository

import scala.concurrent.Future

class ClientsByNameServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar with ClientsByNameHelper {

  private trait Setup {

    val mockRepo: ClientsByNameRepository = mock[ClientsByNameRepository]

    val service = new ClientsByNameService(mockRepo)

  }

  "ClientsByNameServiceSpec" - {
    "must return clients by name" in new Setup {
      when(mockRepo.getClientsByName(any[String], any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(clientsByName))

      val result = service.getClientsByName("irAgentId1", "credentialId1", "name1", 0, -1, 0, "ASC").futureValue

      result mustBe clientsByName

      verify(mockRepo, times(1)).getClientsByName("irAgentId1", "credentialId1", "name1", 0, -1, 0, "ASC")
    }

    "must return clients by name with multiple items of client list" in new Setup {
      when(mockRepo.getClientsByName(any[String], any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(multipleClientListClientsByName))

      val result = service.getClientsByName("irAgentId2", "credentialId2", "name2", 0, -1, 0, "ASC").futureValue

      result mustBe multipleClientListClientsByName

      verify(mockRepo, times(1)).getClientsByName("irAgentId2", "credentialId2", "name2", 0, -1, 0, "ASC")

    }

    "must return clients by name with multiple items of client name chars" in new Setup {
      when(mockRepo.getClientsByName(any[String], any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(multipleItemsClientNameClientsByName))

      val result = service.getClientsByName("irAgentId3", "credentialId3", "name3", 0, -1, 0, "ASC").futureValue

      result mustBe multipleItemsClientNameClientsByName

      verify(mockRepo, times(1)).getClientsByName("irAgentId3", "credentialId3", "name3", 0, -1, 0, "ASC")
    }

    "must propagate failure from repository" in new Setup {
      val exception = new RuntimeException("Error")

      when(mockRepo.getClientsByName(any[String], any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.failed(exception))

      val result = service.getClientsByName("irAgentIdNA", "credentialIdNA", "nameNA", 0, -1, 0, "ASC").failed.futureValue

      result mustBe exception

      verify(mockRepo, times(1)).getClientsByName("irAgentIdNA", "credentialIdNA", "nameNA", 0, -1, 0, "ASC")
    }
  }

}