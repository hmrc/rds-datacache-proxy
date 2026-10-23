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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers.payments

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.payments.PostingSummaryByTypeStubData
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.payments.PostingSummaryByTypeRepositoryImpl

import scala.concurrent.Future

class PostingSummaryByTypeControllerSpec extends SpecBase with MockitoSugar with PostingSummaryByTypeStubData {

  private class SetUp {
    val mockPostingTypeBySummaryRepository: PostingSummaryByTypeRepositoryImpl = mock[PostingSummaryByTypeRepositoryImpl]
    val controller: PostingSummaryByTypeController =
      new PostingSummaryByTypeController(cc, fakeAuthAction, mockPostingTypeBySummaryRepository)
  }

  "PostingSummaryByTypeController.getPostingSummaryByType should" - {

    "return 200 and a successful response when repository returns set response" in new SetUp {
      when(mockPostingTypeBySummaryRepository.getPostingSummaryByType(any[Long], any[String]))
        .thenReturn(Future.successful(postingSummaryByTypeRowMultiple))

      val result: Future[Result] =
        controller.getPostingSummaryByType(1L, "PAF")(fakeRequest)

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(postingSummaryByTypeRowMultiple)
      contentType(result)   shouldBe Some("application/json")
      verify(mockPostingTypeBySummaryRepository).getPostingSummaryByType(1L, "PAF")
    }

    "return 200 and a successful response when repository return empty record" in new SetUp {
      when(mockPostingTypeBySummaryRepository.getPostingSummaryByType(any[Long], any[String]))
        .thenReturn(Future.successful(emptyPostingSummaryByTypeResponses))

      val result: Future[Result] =
        controller.getPostingSummaryByType(3L, "SAM")(fakeRequest)

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(emptyPostingSummaryByTypeResponses)
      contentType(result)   shouldBe Some("application/json")
      verify(mockPostingTypeBySummaryRepository).getPostingSummaryByType(3L, "SAM")
    }

    "return 500 and when repository call fails" in new SetUp {

      when(mockPostingTypeBySummaryRepository.getPostingSummaryByType(any[Long], any[String]))
        .thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] =
        controller.getPostingSummaryByType(4L, "ABC")(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      verify(mockPostingTypeBySummaryRepository).getPostingSummaryByType(4L, "ABC")
    }

  }

}
