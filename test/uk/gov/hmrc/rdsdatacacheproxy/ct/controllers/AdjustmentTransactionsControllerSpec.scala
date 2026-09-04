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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers

import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import play.api.libs.json.Json

import scala.concurrent.Future
import play.api.mvc.Result
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{AdjustmentTransactions, AdjustmentTransactionsList}
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.AdjustmentTransactionsService

class AdjustmentTransactionsControllerSpec extends SpecBase with MockitoSugar {

  private class Setup {
    val mockAdjustmentTransactionsService: AdjustmentTransactionsService = mock[AdjustmentTransactionsService]
    val controller: AdjustmentTransactionsController = new AdjustmentTransactionsController(fakeAuthAction, mockAdjustmentTransactionsService, cc)

    val emptyAdjustmentTransactionsList: List[AdjustmentTransactions] = List[AdjustmentTransactions]()
    val adjustmentTransactionsList: List[AdjustmentTransactions] =
      List(
        AdjustmentTransactions(amount = BigDecimal(50.00), `type`  = "A"),
        AdjustmentTransactions(amount = BigDecimal(100.00), `type` = "B")
      )
  }

  "AdjustmentTransactionsControllerSpec" - {
    "return a 200 and a successful response when retrieving adjustment transactions list" in new Setup {
      when(mockAdjustmentTransactionsService.getAdjustmentTransactions(any[Long], any[Long]))
        .thenReturn(Future.successful(emptyAdjustmentTransactionsList))

      val result: Future[Result] = controller.getAdjustmentTransactions(1L, 2L)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockAdjustmentTransactionsService).getAdjustmentTransactions(1L, 2L)
    }

    "return 200 and a successful response when repository return adjustment transactions with two items " in new Setup {
      when(mockAdjustmentTransactionsService.getAdjustmentTransactions(any[Long], any[Long]))
        .thenReturn(Future.successful(adjustmentTransactionsList))

      val result: Future[Result] = controller.getAdjustmentTransactions(1L, 2L)(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(AdjustmentTransactionsList(adjustmentTransactionsList = adjustmentTransactionsList))
      verify(mockAdjustmentTransactionsService).getAdjustmentTransactions(1L, 2L)
    }

    "return 500 and when repository call fails" in new Setup {
      when(mockAdjustmentTransactionsService.getAdjustmentTransactions(any[Long], any[Long]))
        .thenReturn(Future.failed(new RuntimeException("Error")))

      val result: Future[Result] = controller.getAdjustmentTransactions(1L, 10L)(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      verify(mockAdjustmentTransactionsService).getAdjustmentTransactions(1L, 10L)
    }

  }
}
