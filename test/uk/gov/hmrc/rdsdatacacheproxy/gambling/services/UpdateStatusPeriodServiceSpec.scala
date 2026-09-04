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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError.{InvalidRegNumber, InvalidRegimeCode, InvalidStatus, UnexpectedError}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.UpdateStatusPeriodDataSource

import scala.concurrent.Future

final class UpdateStatusPeriodServiceSpec extends SpecBase {

  private val repository = mock[UpdateStatusPeriodDataSource]
  private val service = new UpdateStatusPeriodService(repository)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(repository)
  }

  private val validRegime = "mgd"
  private val lowercaseRegNumber = "xgm00003122200 "
  private val normalisedRegNumber = "XGM00003122200"
  private val consecNo1 = 1

  "UpdateStatusPeriodService#updateStatusPeriod" - {

    "return unit when repository succeeds AND normalise input (trim + uppercase) before calling repository" in {
      when(repository.updateStatusPeriod(eqTo(normalisedRegNumber), eqTo(consecNo1), eqTo(1)))
        .thenReturn(Future.successful(()))

      val result = service.updateStatusPeriod(validRegime, lowercaseRegNumber, consecNo1, 1).futureValue

      result mustBe Right(())
      verify(repository).updateStatusPeriod(eqTo(normalisedRegNumber), eqTo(consecNo1), eqTo(1))
      verifyNoMoreInteractions(repository)
    }

    "return InvalidRegimeCode and not call repository when regime input is invalid" in {
      val result = service.updateStatusPeriod("INVALID", lowercaseRegNumber, consecNo1, 1).futureValue
      result mustBe Left(InvalidRegimeCode)
      verifyNoMoreInteractions(repository)
    }

    "return InvalidRegNumber and not call repository when regNumber input is invalid" in {
      val invalidRegNumber = "xwm12345678"
      val result = service.updateStatusPeriod(validRegime, invalidRegNumber, consecNo1, 1).futureValue
      result mustBe Left(InvalidRegNumber)
      verifyNoMoreInteractions(repository)
    }

    List(-1, 2, 99).foreach { invalidStatus =>
      s"return InvalidStatus and not call repository when status is $invalidStatus" in {
        val result = service.updateStatusPeriod(validRegime, lowercaseRegNumber, consecNo1, invalidStatus).futureValue
        result mustBe Left(InvalidStatus)
        verifyNoMoreInteractions(repository)
      }
    }

    "return UnexpectedError when repository throws exception" in {
      when(repository.updateStatusPeriod(eqTo(normalisedRegNumber), eqTo(consecNo1), eqTo(1)))
        .thenReturn(Future.failed(new RuntimeException("DB failure when calling repo")))
      val result = service.updateStatusPeriod(validRegime, lowercaseRegNumber, consecNo1, 1).futureValue
      result mustBe Left(UnexpectedError)
      verify(repository).updateStatusPeriod(eqTo(normalisedRegNumber), eqTo(consecNo1), eqTo(1))
      verifyNoMoreInteractions(repository)
    }
  }
}
