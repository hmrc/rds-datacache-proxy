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

package uk.gov.hmrc.rdsdatacacheproxy.ctcore.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ctcore.models.{TaxTransactions, TaxTransactionsItem}
import uk.gov.hmrc.rdsdatacacheproxy.ctcore.repositories.TaxTransactionsDataCacheRepository

import java.time.LocalDate
import scala.concurrent.Future

class TaxTransactionsControllerSpec extends SpecBase with MockitoSugar {

  private trait Setup {
    val mockRepository: TaxTransactionsDataCacheRepository = mock[TaxTransactionsDataCacheRepository]
    val controller = new TaxTransactionsController(fakeAuthAction, mockRepository, cc)
  }

  val taxTransactions: List[TaxTransactionsItem] = List(
    TaxTransactionsItem(currentAmount = BigDecimal(123.44), assessmentType = "A", taxDate = LocalDate.of(2026, 1, 1), correctionClaimSignal = Some("0")),
    TaxTransactionsItem(currentAmount = BigDecimal(123.44), assessmentType = "D", taxDate = LocalDate.of(2026, 2, 1), correctionClaimSignal = Some("2")),
    TaxTransactionsItem(currentAmount = BigDecimal(123.44), assessmentType = "E", taxDate = LocalDate.of(2026, 3, 1), correctionClaimSignal = None))

  val emptyTaxTransactions: List[TaxTransactionsItem] = List.empty

  "TaxTransactionsController getTaxTransactions" - {

    "returns 200 when " in new Setup {

      when(mockRepository.getTaxTransactions(any[Long], any[Long]))
        .thenReturn(Future.successful((taxTransactions)))

      val result: Future[Result] = controller.getTaxTransactions(1, 1)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some(JSON)
      contentAsJson(result) mustBe Json.toJson(TaxTransactions(taxTransactions))

      verify(mockRepository).getTaxTransactions(1,1)
    }

    "returns 200 when empty" in new Setup {

      when(mockRepository.getTaxTransactions(any[Long], any[Long]))
        .thenReturn(Future.successful((emptyTaxTransactions)))

      val result: Future[Result] = controller.getTaxTransactions(1, 1)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some(JSON)
      contentAsJson(result) mustBe Json.toJson(TaxTransactions(emptyTaxTransactions))

      verify(mockRepository).getTaxTransactions(1, 1)
    }
  }
}
