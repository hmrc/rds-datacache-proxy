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
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.payments.PaymentSearchResultStubData
import uk.gov.hmrc.rdsdatacacheproxy.ct.queryParams.payments.PaymentSearchResultQueryParams
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.payments.PaymentSearchResultRepositoryImpl

import java.time.LocalDate
import scala.concurrent.Future

class PaymentSearchResultControllerSpec extends SpecBase with MockitoSugar with PaymentSearchResultStubData {

  private class SetUp {
    val mockPaymentSearchResultRepository: PaymentSearchResultRepositoryImpl = mock[PaymentSearchResultRepositoryImpl]
    val controller: PaymentSearchResultController =
      new PaymentSearchResultController(cc, fakeAuthAction, mockPaymentSearchResultRepository)
  }

  "PaymentSearchResultController.getPaymentSearchResult should" - {
    "return 200 and a successful response when repository return default record" in new SetUp {
      when(mockPaymentSearchResultRepository.getPaymentSearchResult(any[Long], Some(any[LocalDate]), Some(any[LocalDate])))
        .thenReturn(Future.successful(singlePaymentSearchResultResponse))

      val result: Future[Result] =
        controller.getPaymentSearchResult(10L,
                                          PaymentSearchResultQueryParams(Some(LocalDate.parse("2006-01-01")), Some(LocalDate.parse("2006-12-31")))
                                         )(fakeRequest)

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(singlePaymentSearchResultResponse)
      contentType(result)   shouldBe Some("application/json")
      verify(mockPaymentSearchResultRepository).getPaymentSearchResult(10L, Some(LocalDate.parse("2006-01-01")), Some(LocalDate.parse("2006-12-31")))
    }

    "return 200 and a successful response when repository return empty record" in new SetUp {
      when(mockPaymentSearchResultRepository.getPaymentSearchResult(any[Long], Some(any[LocalDate]), Some(any[LocalDate])))
        .thenReturn(Future.successful(emptyPaymentSearchResultResponse))

      val result: Future[Result] =
        controller.getPaymentSearchResult(2L,
                                          PaymentSearchResultQueryParams(Some(LocalDate.parse("2006-01-01")), Some(LocalDate.parse("2006-12-31")))
                                         )(fakeRequest)

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(emptyPaymentSearchResultResponse)
      contentType(result)   shouldBe Some("application/json")
      verify(mockPaymentSearchResultRepository).getPaymentSearchResult(2L, Some(LocalDate.parse("2006-01-01")), Some(LocalDate.parse("2006-12-31")))
    }

    "return 500 and when repository call fails" in new SetUp {
      when(mockPaymentSearchResultRepository.getPaymentSearchResult(any[Long], Some(any[LocalDate]), Some(any[LocalDate])))
        .thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] =
        controller.getPaymentSearchResult(200L,
                                          PaymentSearchResultQueryParams(Some(LocalDate.parse("2006-01-01")), Some(LocalDate.parse("2006-12-31")))
                                         )(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      verify(mockPaymentSearchResultRepository).getPaymentSearchResult(200L, Some(LocalDate.parse("2006-01-01")), Some(LocalDate.parse("2006-12-31")))
    }

  }

}
