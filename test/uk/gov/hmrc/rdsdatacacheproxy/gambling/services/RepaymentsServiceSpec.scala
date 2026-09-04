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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.services

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.{reset, verify, verifyNoMoreInteractions, when}
import org.scalatest.matchers.must.Matchers.mustBe
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.Regime
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError.{InvalidRegNumber, InvalidRegimeCode, UnexpectedError}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepaymentsDataSource
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.{validResponseActualRepayments, validResponseRepaymentsSummary}

import scala.concurrent.Future

final class RepaymentsServiceSpec extends SpecBase {

  private val repository = mock[RepaymentsDataSource]
  private val service = new RepaymentsService(repository)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(repository)
  }

  private val validRegime = Regime.values.head
  private val lowercaseRegNumber = "xgm00003122200 "
  private val normalisedRegNumber = "XGM00003122200"
  private val paginationStart = 1
  private val paginationMaxRows = 100

  "RepaymentsService#getRepaymentsSummary" - {

    "return validResponseRepaymentsSummary when repository succeeds AND normalise input (trim + uppercase) before calling repository" in {
      when(repository.getRepaymentsSummary(eqTo(validRegime), eqTo(normalisedRegNumber)))
        .thenReturn(Future.successful(validResponseRepaymentsSummary))

      val result = service.getRepaymentsSummary(validRegime.toString, lowercaseRegNumber).futureValue

      result mustBe Right(validResponseRepaymentsSummary)
      verify(repository).getRepaymentsSummary(eqTo(validRegime), eqTo(normalisedRegNumber))
      verifyNoMoreInteractions(repository)
    }

    "return InvalidRegimeError and not call repository when Regime input is invalid" in {
      val result = service.getRepaymentsSummary("INVALID", lowercaseRegNumber).futureValue
      result mustBe Left(InvalidRegimeCode)
      verifyNoMoreInteractions(repository)
    }

    "return InvalidRegNumber and not call repository when RegNumber input is invalid" in {
      val invalidRegNumber = "xwm12345678"
      val result = service.getRepaymentsSummary(validRegime.toString, invalidRegNumber).futureValue
      result mustBe Left(InvalidRegNumber)
      verifyNoMoreInteractions(repository)
    }

    "return UnexpectedError when repository throws exception" in {
      when(repository.getRepaymentsSummary(eqTo(validRegime), eqTo(normalisedRegNumber)))
        .thenReturn(Future.failed(new RuntimeException("DB failure when calling repo")))
      val result = service.getRepaymentsSummary(validRegime.toString, lowercaseRegNumber).futureValue
      result mustBe Left(UnexpectedError)
      verify(repository).getRepaymentsSummary(eqTo(validRegime), eqTo(normalisedRegNumber))
      verifyNoMoreInteractions(repository)
    }
  }

  "RepaymentsService#getActualRepayments" - {

    "return validResponseActualRepayments when repository succeeds AND normalise input (trim + uppercase) before calling repository" in {
      when(repository.getActualRepayments(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(paginationStart), eqTo(paginationMaxRows)))
        .thenReturn(Future.successful(validResponseActualRepayments))

      val result = service.getActualRepayments(validRegime.toString, lowercaseRegNumber, paginationStart, paginationMaxRows).futureValue

      result mustBe Right(validResponseActualRepayments)
      verify(repository).getActualRepayments(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(paginationStart), eqTo(paginationMaxRows))
      verifyNoMoreInteractions(repository)
    }

    "return InvalidRegimeError and not call repository when Regime input is invalid" in {
      val result = service.getActualRepayments("INVALID", lowercaseRegNumber, paginationStart, paginationMaxRows).futureValue
      result mustBe Left(InvalidRegimeCode)
      verifyNoMoreInteractions(repository)
    }

    "return InvalidRegNumber and not call repository when RegNumber input is invalid" in {
      val invalidRegNumber = "xwm12345678"
      val result = service.getActualRepayments(validRegime.toString, invalidRegNumber, paginationStart, paginationMaxRows).futureValue
      result mustBe Left(InvalidRegNumber)
      verifyNoMoreInteractions(repository)
    }

    "return UnexpectedError when repository throws exception" in {
      when(repository.getActualRepayments(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(paginationStart), eqTo(paginationMaxRows)))
        .thenReturn(Future.failed(new RuntimeException("DB failure when calling repo")))
      val result = service.getActualRepayments(validRegime.toString, lowercaseRegNumber, paginationStart, paginationMaxRows).futureValue
      result mustBe Left(UnexpectedError)
      verify(repository).getActualRepayments(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(paginationStart), eqTo(paginationMaxRows))
      verifyNoMoreInteractions(repository)
    }
  }
}
