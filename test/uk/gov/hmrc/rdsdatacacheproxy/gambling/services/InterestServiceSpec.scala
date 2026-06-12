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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.InterestDataSource
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.{validResponseInterest, validResponseInterestDetails}

import scala.concurrent.Future

final class InterestServiceSpec extends SpecBase {

  private val repository = mock[InterestDataSource]
  private val service = new InterestService(repository)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(repository)
  }

  private val validRegime = Regime.values.head
  private val lowercaseRegNumber = "xwm12345678901 "
  private val normalisedRegNumber = "XWM12345678901"
  private val interestId = "INT001"

  "InterestService#getInterestDetails" - {

    "return validResponseInterestDetails when repository succeeds AND normalise input (trim + uppercase) before calling repository" in {
      when(repository.getInterestDetails(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(1), eqTo(10)))
        .thenReturn(Future.successful(validResponseInterestDetails))

      val result = service.getInterestDetails(validRegime.toString, lowercaseRegNumber, 1, 10).futureValue

      result mustBe Right(validResponseInterestDetails)
      verify(repository).getInterestDetails(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(1), eqTo(10))
      verifyNoMoreInteractions(repository)
    }

    "return InvalidRegimeError and not call repository when Regime input is invalid" in {
      val result = service.getInterestDetails("INVALID", lowercaseRegNumber, 1, 10).futureValue
      result mustBe Left(InvalidRegimeCode)
      verifyNoMoreInteractions(repository)
    }

    "return InvalidRegNumber and not call repository when RegNumber input is invalid" in {
      val invalidRegNumber = "xwm12345678"
      val result = service.getInterestDetails(validRegime.toString, invalidRegNumber, 1, 10).futureValue
      result mustBe Left(InvalidRegNumber)
      verifyNoMoreInteractions(repository)
    }

    "return UnexpectedError when repository throws exception" in {
      when(repository.getInterestDetails(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(1), eqTo(10)))
        .thenReturn(Future.failed(new RuntimeException("DB failure when calling repo")))
      val result = service.getInterestDetails(validRegime.toString, lowercaseRegNumber, 1, 10).futureValue
      result mustBe Left(UnexpectedError)
      verify(repository).getInterestDetails(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(1), eqTo(10))
      verifyNoMoreInteractions(repository)
    }
  }

  "InterestService#getInterestDrilldown" - {

    "return validResponseInterest when repository succeeds AND normalise input (trim + uppercase) before calling repository" in {
      when(repository.getInterestDrilldown(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(interestId), eqTo(1), eqTo(10)))
        .thenReturn(Future.successful(validResponseInterest))

      val result = service.getInterestDrilldown(validRegime.toString, lowercaseRegNumber, interestId, 1, 10).futureValue

      result mustBe Right(validResponseInterest)
      verify(repository).getInterestDrilldown(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(interestId), eqTo(1), eqTo(10))
      verifyNoMoreInteractions(repository)
    }

    "return InvalidRegimeError and not call repository when Regime input is invalid" in {
      val result = service.getInterestDrilldown("INVALID", lowercaseRegNumber, interestId, 1, 10).futureValue
      result mustBe Left(InvalidRegimeCode)
      verifyNoMoreInteractions(repository)
    }

    "return InvalidRegNumber and not call repository when RegNumber input is invalid" in {
      val invalidRegNumber = "xwm12345678"
      val result = service.getInterestDrilldown(validRegime.toString, invalidRegNumber, interestId, 1, 10).futureValue
      result mustBe Left(InvalidRegNumber)
      verifyNoMoreInteractions(repository)
    }

    "return UnexpectedError when repository throws exception" in {
      when(repository.getInterestDrilldown(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(interestId), eqTo(1), eqTo(10)))
        .thenReturn(Future.failed(new RuntimeException("DB failure when calling repo")))

      val result = service.getInterestDrilldown(validRegime.toString, lowercaseRegNumber, interestId, 1, 10).futureValue

      result mustBe Left(UnexpectedError)
      verify(repository).getInterestDrilldown(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(interestId), eqTo(1), eqTo(10))
      verifyNoMoreInteractions(repository)
    }
  }
}
