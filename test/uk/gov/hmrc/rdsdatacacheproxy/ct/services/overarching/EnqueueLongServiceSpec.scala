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

package uk.gov.hmrc.rdsdatacacheproxy.ct.services.overarching

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.overarching.EnqueueHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.overarching.EnqueueLongRepository

import scala.concurrent.Future

class EnqueueLongServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar with EnqueueHelper {

  private trait Setup {

    val mockRepo: EnqueueLongRepository = mock[EnqueueLongRepository]

    val service = new EnqueueLongService(mockRepo)

  }

  "EnqueueLongServiceSpec" - {
    "must return enqueue long" in new Setup {
      when(mockRepo.getEnqueueLong(any[Long], any[String], any[String], any[String], any[String], any[String], any[String], any[Int], any[Int]))
        .thenReturn(Future.successful(enqueue))

      val result = service.getEnqueueLong(12345, "sender1", "queue1", "replyQueue1", "correlationId1", "filter1", "key1", 1, 1010).futureValue

      result mustBe enqueue

      verify(mockRepo, times(1)).getEnqueueLong(12345, "sender1", "queue1", "replyQueue1", "correlationId1", "filter1", "key1", 1, 1010)
    }

    "must propagate failure from repository" in new Setup {
      val exception = new RuntimeException("Error")

      when(mockRepo.getEnqueueLong(any[Long], any[String], any[String], any[String], any[String], any[String], any[String], any[Int], any[Int]))
        .thenReturn(Future.failed(exception))

      val result =
        service.getEnqueueLong(67890, "senderNA", "queueNA", "replyQueueNA", "correlationIdNA", "filterNA", "keyNA", 2, 2020).failed.futureValue

      result mustBe exception

      verify(mockRepo, times(1)).getEnqueueLong(67890, "senderNA", "queueNA", "replyQueueNA", "correlationIdNA", "filterNA", "keyNA", 2, 2020)
    }
  }

}
