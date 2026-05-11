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

import org.mockito.Mockito.{reset, verify, verifyNoMoreInteractions, when}
import org.scalactic.Prettifier.default
import org.scalatest.matchers.must.Matchers.mustBe
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.GamblingError.{InvalidRegNumber, InvalidRegimeCode, UnexpectedError}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{ReallocationsOut, Regime}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.GamblingReallocationsDataSource

import scala.concurrent.Future

final class GamblingReallocationsServiceSpec extends SpecBase {

  private val repository = mock[GamblingReallocationsDataSource]
  private val service = new GamblingReallocationsService(repository)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(repository)
  }

  private val validRegime = Regime.values.head
  private val lowercaseRegNumber = "xwm12345678901 "
  private val normalisedRegNumber = "XWM12345678901"

  "getReallocationsOut" - {

    "return ReallocationsOut when repository succeeds AND normalise input (trim + uppercase) before calling repository" in {
      when(repository.getReallocationsOut(validRegime, normalisedRegNumber, 1, 10))
        .thenReturn(Future.successful(ReallocationsOut.empty))

      val result = service.getReallocationsOut(validRegime.toString, lowercaseRegNumber, 1, 10).futureValue

      result mustBe Right(ReallocationsOut.empty)
      verify(repository).getReallocationsOut(validRegime, normalisedRegNumber, 1, 10)
      verifyNoMoreInteractions(repository)
    }

    "return InvalidRegimeError and not call repository when Regime input is invalid" in {
      val result = service.getReallocationsOut("INVALID", lowercaseRegNumber, 1, 10).futureValue
      result mustBe Left(InvalidRegimeCode)
      verifyNoMoreInteractions(repository)
    }

    "return InvalidRegNumber and not call repository when RegNumber input is invalid" in {
      val invalidRegNumber = "xwm12345678"
      val result = service.getReallocationsOut(validRegime.toString, invalidRegNumber, 1, 10).futureValue
      result mustBe Left(InvalidRegNumber)
      verifyNoMoreInteractions(repository)
    }

    "return UnexpectedError when repository throws exception" in {
      when(repository.getReallocationsOut(validRegime, normalisedRegNumber, 1, 10))
        .thenReturn(Future.failed(new RuntimeException("DB failure when calling repo")))
      val result = service.getReallocationsOut(validRegime.toString, lowercaseRegNumber, 1, 10).futureValue
      result mustBe Left(UnexpectedError)
      verify(repository).getReallocationsOut(validRegime, normalisedRegNumber, 1, 10)
      verifyNoMoreInteractions(repository)
    }
  }
}
