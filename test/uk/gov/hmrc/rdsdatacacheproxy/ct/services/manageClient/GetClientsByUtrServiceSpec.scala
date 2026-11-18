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
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.manageClient.GetClientsByUtrHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.manageClient.GetClientsByUtrRepository

import scala.concurrent.Future

class GetClientsByUtrServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar with GetClientsByUtrHelper {

  private trait Setup {

    val mockRepo: GetClientsByUtrRepository = mock[GetClientsByUtrRepository]

    val service = new GetClientsByUtrService(mockRepo)

  }

  "GetClientsByUtrServiceSpec" - {

    "must return an empty client list" in new Setup {
      when(mockRepo.getClientsByUtr(any[String], any[String], any[String]))
        .thenReturn(Future.successful(emptyClients))

      val result =
        service.getClientsByUtr("IR1001", "CR1001", "1234567890").futureValue

      result mustBe emptyClients

      verify(mockRepo, times(1))
        .getClientsByUtr("IR1001", "CR1001", "1234567890")
    }

    "must return a single client" in new Setup {
      when(mockRepo.getClientsByUtr(any[String], any[String], any[String]))
        .thenReturn(Future.successful(singleClient))

      val result = service.getClientsByUtr("IR2002", "CR2002", "1234567890").futureValue

      result mustBe singleClient

      verify(mockRepo, times(1))
        .getClientsByUtr("IR2002", "CR2002", "1234567890")
    }

    "must return multiple clients" in new Setup {
      when(mockRepo.getClientsByUtr(any[String], any[String], any[String]))
        .thenReturn(Future.successful(multipleClients))

      val result = service.getClientsByUtr("IR3003", "CR3003", "2345678901").futureValue

      result mustBe multipleClients

      verify(mockRepo, times(1)).getClientsByUtr("IR3003", "CR3003", "2345678901")
    }

    "must return multiple client name starting characters" in new Setup {
      when(mockRepo.getClientsByUtr(any[String], any[String], any[String]))
        .thenReturn(Future.successful(multipleNameStartChars))

      val result = service.getClientsByUtr("IR4004", "CR4004", "4567890123").futureValue

      result mustBe multipleNameStartChars

      verify(mockRepo, times(1)).getClientsByUtr("IR4004", "CR4004", "4567890123")
    }

    "must propagate failure from repository" in new Setup {
      val exception = new RuntimeException("Error")

      when(mockRepo.getClientsByUtr(any[String], any[String], any[String])).thenReturn(Future.failed(exception))

      val result = service.getClientsByUtr("IR9999", "CR9999", "9999999999").failed.futureValue
      result mustBe exception

      verify(mockRepo, times(1)).getClientsByUtr("IR9999", "CR9999", "9999999999")
    }
  }
}
