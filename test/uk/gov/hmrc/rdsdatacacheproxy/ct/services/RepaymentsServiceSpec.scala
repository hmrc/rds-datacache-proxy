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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{Repayments, RepaymentsDetails}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.RepaymentsRepository

import java.math.BigDecimal
import java.time.LocalDate
import scala.concurrent.Future

class RepaymentsServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar {

  private trait Setup {

    val mockRepo: RepaymentsRepository = mock[RepaymentsRepository]

    val service = new RepaymentsService(mockRepo)

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

  "RepaymentsServiceServiceSpec" - {
    "must return repayments with one item" in new Setup {
      when(mockRepo.getRepayments(any[Long], any[Long]))
        .thenReturn(Future.successful(repaymentsWithOneItem))

      val result = service.getRepayments(6212811176L, 2L).futureValue

      result mustBe repaymentsWithOneItem

      verify(mockRepo, times(1)).getRepayments(6212811176L, 2L)
    }

    "must return repayments with multiple items" in new Setup {
      when(mockRepo.getRepayments(any[Long], any[Long]))
        .thenReturn(Future.successful(repaymentsWithMultipleItems))

      val result = service.getRepayments(6212811176L, 2L).futureValue

      result mustBe repaymentsWithMultipleItems

      verify(mockRepo, times(1)).getRepayments(6212811176L, 2L)

    }

    "must return empty repayments" in new Setup {
      when(mockRepo.getRepayments(any[Long], any[Long]))
        .thenReturn(Future.successful(emptyRepayments))

      val result = service.getRepayments(1L, 2L).futureValue

      result mustBe emptyRepayments

      verify(mockRepo, times(1)).getRepayments(1L, 2L)
    }

    "must propagate failure from repository" in new Setup {
      val exception = new RuntimeException("Error")

      when(mockRepo.getRepayments(any[Long], any[Long]))
        .thenReturn(Future.failed(exception))

      val result = service.getRepayments(1L, 2L).failed.futureValue

      result mustBe exception

      verify(mockRepo, times(1)).getRepayments(1L, 2L)
    }
  }

}
