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

package uk.gov.hmrc.rdsdatacacheproxy.ct.services.payments

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.payments.PostingSummaryByTypeStubData
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.payments.PostingSummaryByTypeRepository

import scala.concurrent.Future

class PostingSummaryByTypeServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar with PostingSummaryByTypeStubData {

  private trait Setup {
    val mockRepository: PostingSummaryByTypeRepository = mock[PostingSummaryByTypeRepository]
    val service = new PostingSummaryByTypeService(mockRepository)
  }

  "PostingSummaryByTypeServiceSpec" - {
    "must return multiple posting summaries" in new Setup {
      when(mockRepository.getPostingSummaryByType(any[Long], any[String]))
        .thenReturn(Future.successful(postingSummaryByTypeRowMultiple))

      val result = service.getPostingSummaryByType(1, "PAF").futureValue

      result mustBe postingSummaryByTypeRowMultiple

      verify(mockRepository, times(1)).getPostingSummaryByType(1, "PAF")
    }

    "must return posting summary with missing optional" in new Setup {
      when(mockRepository.getPostingSummaryByType(any[Long], any[String]))
        .thenReturn(Future.successful(postingSummaryByTypeRowMissingField))

      val result = service.getPostingSummaryByType(2, "PYT").futureValue

      result mustBe postingSummaryByTypeRowMissingField

      verify(mockRepository, times(1)).getPostingSummaryByType(2, "PYT")

    }

    "must return empty postingSummary list" in new Setup {
      when(mockRepository.getPostingSummaryByType(any[Long], any[String]))
        .thenReturn(Future.successful(emptyPostingSummaryByTypeResponses))

      val result = service.getPostingSummaryByType(3, "SAM").futureValue

      result mustBe emptyPostingSummaryByTypeResponses

      verify(mockRepository, times(1)).getPostingSummaryByType(3L, "SAM")
    }

    "must propagate failure from repository" in new Setup {
      val exception = new RuntimeException("Error")

      when(mockRepository.getPostingSummaryByType(any[Long], any[String]))
        .thenReturn(Future.failed(exception))

      val result = service.getPostingSummaryByType(4L, "ABC").failed.futureValue

      result mustBe exception

      verify(mockRepository, times(1)).getPostingSummaryByType(4L, "ABC")
    }
  }
}
