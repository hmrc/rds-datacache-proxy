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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{ReallocationFromAccDetails, ReallocationFromAccPeriod}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.ReallocationFromAccPeriodRepository

import java.time.LocalDate
import scala.concurrent.Future

class ReallocationFromAccPeriodServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar {

  private trait Setup {

    val mockRepo: ReallocationFromAccPeriodRepository = mock[ReallocationFromAccPeriodRepository]

    val service = new ReallocationFromAccPeriodService(mockRepo)

    val taxPayerReference: Long = 98765L
    val accountingPeriod: Long = 562763L

    val reallocationFromAccPeriod: ReallocationFromAccPeriod = ReallocationFromAccPeriod(
      List(
        ReallocationFromAccDetails(
          Some(BigDecimal(12390)),
          Some(LocalDate.of(2026, 12, 27)),
          Some(LocalDate.of(204, 2, 2)),
          Some("18969779586")
        ),
        ReallocationFromAccDetails(
          Some(BigDecimal(12345)),
          Some(LocalDate.of(2026, 12, 27)),
          Some(LocalDate.of(204, 2, 2)),
          Some("18969779586")
        )
      )
    )
  }

  "ReallocationFromAccPeriodService must return list of reallocationFromAccPeriod" in new Setup {
    when(mockRepo.getReallocationFromAccPeriod(any(), any()))
      .thenReturn(Future.successful(reallocationFromAccPeriod))

    val result: ReallocationFromAccPeriod = service.getReallocationFromAccPeriod(taxPayerReference, accountingPeriod).futureValue

    result mustBe reallocationFromAccPeriod

    verify(mockRepo, times(1)).getReallocationFromAccPeriod(taxPayerReference, accountingPeriod)

  }
  "ReallocationFromAccPeriodService must propagate failure from repository" in new Setup {

    val exception = new RuntimeException("Failure from Repo")

    when(mockRepo.getReallocationFromAccPeriod(any(), any()))
      .thenReturn(Future.failed(exception))

    val result: Throwable = service.getReallocationFromAccPeriod(taxPayerReference, accountingPeriod).failed.futureValue

    result mustBe exception

    verify(mockRepo, times(1)).getReallocationFromAccPeriod(taxPayerReference, accountingPeriod)

  }

}
