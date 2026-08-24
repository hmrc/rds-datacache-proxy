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

package uk.gov.hmrc.rdsdatacacheproxy.ct.services

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.DisplayNeededHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.DisplayNeededItem
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.DisplayNeededRepository

import scala.concurrent.Future

class DisplayNeededServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar with DisplayNeededHelper {

  private trait Setup {

    val mockRepo: DisplayNeededRepository = mock[DisplayNeededRepository]

    val service = new DisplayNeededService(mockRepo)
  }

  "DisplayNeededServiceSpec" - {
    "must return Display Needed with all flags set to false" in new Setup {
      when(mockRepo.getDisplayNeeded(any[Long], any[Long]))
        .thenReturn(Future.successful(displayNeededItemAllFalse))

      val result = service.getDisplayNeeded(10L, 1L).futureValue

      result mustBe displayNeededItemAllFalse

      verify(mockRepo, times(1)).getDisplayNeeded(10L, 1L)

    }

    "must return Display Needed with all flags set to true" in new Setup {
      when(mockRepo.getDisplayNeeded(any[Long], any[Long]))
        .thenReturn(Future.successful(displayNeededItemAllTrue))

      val result = service.getDisplayNeeded(20L, 1L).futureValue

      result mustBe displayNeededItemAllTrue

      verify(mockRepo, times(1)).getDisplayNeeded(20L, 1L)
    }

    "must return Display Needed with some flags set to false and true" in new Setup {
      when(mockRepo.getDisplayNeeded(any[Long], any[Long]))
        .thenReturn(Future.successful(displayNeededItemMixed))

      val result = service.getDisplayNeeded(30L, 1L).futureValue

      result mustBe displayNeededItemMixed

      verify(mockRepo, times(1)).getDisplayNeeded(30L, 1L)
    }

    "must propagate failure from repository" in new Setup {

      val exception = new RuntimeException("Error from downstream")

      when(mockRepo.getDisplayNeeded(any[Long], any[Long]))
        .thenReturn(Future.failed(exception))

      val result = service.getDisplayNeeded(999L, 1L).failed.futureValue

      result mustBe exception

      verify(mockRepo, times(1)).getDisplayNeeded(999L, 1L)

    }
  }

}
