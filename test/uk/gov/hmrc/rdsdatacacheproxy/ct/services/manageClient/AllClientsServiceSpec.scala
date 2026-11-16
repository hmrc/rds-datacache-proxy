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
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.manageClient.AllClientsRepository

import scala.concurrent.Future

class AllClientsServiceSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with MockitoSugar
    with AllClientsHelper {

  private trait Setup {

    val mockRepo: AllClientsRepository = mock[AllClientsRepository]

    val service = new AllClientsService(mockRepo)

  }

  "AllClientsServiceSpec" - {
    "must return all clients" in new Setup {
      when(mockRepo.getAllClients(any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(allClients))

      val result = service.getAllClients("irAgentId1", "credentialId1", 0, -1, 0, "ASC").futureValue

      result mustBe allClients

      verify(mockRepo, times(1)).getAllClients("irAgentId1", "credentialId1", 0, -1, 0, "ASC")
    }

    "must return all clients with multiple items of client list" in new Setup {
      when(mockRepo.getAllClients(any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(multipleClientListAllClients))

      val result = service.getAllClients("irAgentId2", "credentialId2", 0, -1, 0, "ASC").futureValue

      result mustBe multipleClientListAllClients

      verify(mockRepo, times(1)).getAllClients("irAgentId2", "credentialId2", 0, -1, 0, "ASC")

    }

    "must return payment allocation details with minimal details" in new Setup {
      when(mockRepo.getAllClients(any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(multipleItemsClientNameAllClients))

      val result = service.getAllClients("irAgentId3", "credentialId3", 0, -1, 0, "ASC").futureValue

      result mustBe multipleItemsClientNameAllClients

      verify(mockRepo, times(1)).getAllClients("irAgentId3", "credentialId3", 0, -1, 0, "ASC")
    }

    "must propagate failure from repository" in new Setup {
      val exception = new RuntimeException("Error")

      when(mockRepo.getAllClients(any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.failed(exception)

      val result = service.getAllClients("irAgentIdNA", "credentialIdNA", 0, -1, 0, "ASC").futureValue

      result mustBe exception

      verify(mockRepo, times(1)).getAllClients("irAgentIdNA", "credentialIdNA", 0, -1, 0, "ASC")
    }
  }

}
