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
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{Repayments, RepaymentsDetails}
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.RepaymentsService

import java.time.LocalDate
import scala.concurrent.Future

class RepaymentsControllerSpec extends SpecBase with MockitoSugar {

  private class Setup {
    val mockService: RepaymentsService = mock[RepaymentsService]
    val controller: RepaymentsController = new RepaymentsController(fakeAuthAction, mockService, cc)

    val emptyRepayments: Repayments = Repayments(List.empty)
    val repaymentsWithOneItem: Repayments = Repayments(
      List(
        RepaymentsDetails(
          amount        = Some(BigDecimal(10)),
          repaymentType = "S",
          repaymentDate = LocalDate.of(2026, 7, 24)
        )
      )
    )

    val repaymentsWithMultipleItems: Repayments = Repayments(
      List(
        RepaymentsDetails(
          amount        = Some(BigDecimal(20)),
          repaymentType = "S",
          repaymentDate = LocalDate.of(2027, 7, 24)
        ),
        RepaymentsDetails(
          amount        = Some(BigDecimal(30)),
          repaymentType = "T",
          repaymentDate = LocalDate.of(2028, 7, 24)
        )
      )
    )
  }

  "RepaymentsControllerSpec" - {
    "return a 200 and a successful response when retrieving repayments with one item" in new Setup {
      when(mockService.getRepayments(any[Long], any[Long]))
        .thenReturn(Future.successful(repaymentsWithOneItem))

      val result: Future[Result] = controller.getRepayments(6212811176L, 2L)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getRepayments(6212811176L, 2L)
    }

    "return a 200 and a successful response when retrieving repayments with multiple items" in new Setup {
      when(mockService.getRepayments(any[Long], any[Long]))
        .thenReturn(Future.successful(repaymentsWithMultipleItems))

      val result: Future[Result] = controller.getRepayments(6212811176L, 2L)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getRepayments(6212811176L, 2L)
    }

    "return a 200 and a successful response when retrieving empty repayment" in new Setup {
      when(mockService.getRepayments(any[Long], any[Long]))
        .thenReturn(Future.successful(emptyRepayments))

      val result: Future[Result] = controller.getRepayments(1L, 2L)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getRepayments(1L, 2L)
    }

    "return 500 and when repository call fails" in new Setup {
      when(mockService.getRepayments(any[Long], any[Long]))
        .thenReturn(Future.failed(new RuntimeException("Error")))

      val result: Future[Result] = controller.getRepayments(1L, 10L)(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getRepayments(1L, 10L)
    }

  }
}
