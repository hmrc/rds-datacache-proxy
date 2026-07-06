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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.OpenReturnsDataSource
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseOpenReturnPeriods

import scala.concurrent.Future

final class OpenReturnsServiceSpec extends SpecBase {

  private val repository = mock[OpenReturnsDataSource]
  private val service = new OpenReturnsService(repository)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(repository)
  }

  private val validRegime = Regime.values.head
  private val lowercaseRegNumber = "xwm12345678901 "
  private val normalisedRegNumber = "XWM12345678901"
  private val ASC = "ASC"
  private val DESC = "DESC"
  private val PERIOD = 1
  private val DUE_DATE = 2
  private val STATUS = 3

  "OpenReturnsService#getOpenReturnPeriods" - {

    "return validResponseOpenReturnPeriods when repository succeeds AND normalise input (trim + uppercase) before calling repository" in {
      when(repository.getOpenReturnPeriods(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(STATUS), eqTo(ASC)))
        .thenReturn(Future.successful(validResponseOpenReturnPeriods))

      val result = service.getOpenReturnPeriods(validRegime.toString, lowercaseRegNumber, Some(STATUS), Some("   asc   ")).futureValue

      result mustBe Right(validResponseOpenReturnPeriods)
      verify(repository).getOpenReturnPeriods(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(STATUS), eqTo(ASC))
      verifyNoMoreInteractions(repository)
    }

    "return validResponseOpenReturnPeriods with CORRECT DEFAULTS when sortBy & orderBy are NONE" in {
      when(repository.getOpenReturnPeriods(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(PERIOD), eqTo(ASC)))
        .thenReturn(Future.successful(validResponseOpenReturnPeriods))

      val result = service.getOpenReturnPeriods(validRegime.toString, lowercaseRegNumber, None, None).futureValue

      result mustBe Right(validResponseOpenReturnPeriods)
      verify(repository).getOpenReturnPeriods(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(PERIOD), eqTo(ASC))
      verifyNoMoreInteractions(repository)
    }

    List("DESC", "desc").foreach { orderBy =>
      s"return validResponseOpenReturnPeriods when sortBy is 1 & orderBy is $orderBy" in {
        when(repository.getOpenReturnPeriods(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(PERIOD), eqTo(DESC)))
          .thenReturn(Future.successful(validResponseOpenReturnPeriods))

        val result = service.getOpenReturnPeriods(validRegime.toString, lowercaseRegNumber, Some(1), Some(DESC)).futureValue

        result mustBe Right(validResponseOpenReturnPeriods)
        verify(repository).getOpenReturnPeriods(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(PERIOD), eqTo(DESC))
        verifyNoMoreInteractions(repository)
      }
    }

    List("ASC", "asc").foreach { orderBy =>
      s"return validResponseOpenReturnPeriods when sortBy is 1 & orderBy is $orderBy" in {
        when(repository.getOpenReturnPeriods(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(PERIOD), eqTo(ASC)))
          .thenReturn(Future.successful(validResponseOpenReturnPeriods))

        val result = service.getOpenReturnPeriods(validRegime.toString, lowercaseRegNumber, Some(1), Some(ASC)).futureValue

        result mustBe Right(validResponseOpenReturnPeriods)
        verify(repository).getOpenReturnPeriods(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(PERIOD), eqTo(ASC))
        verifyNoMoreInteractions(repository)
      }
    }

    "return validResponseOpenReturnPeriods when sortBy is 2 & orderBy is RANDOM" in {
      when(repository.getOpenReturnPeriods(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(DUE_DATE), eqTo(ASC)))
        .thenReturn(Future.successful(validResponseOpenReturnPeriods))

      val result = service.getOpenReturnPeriods(validRegime.toString, lowercaseRegNumber, Some(2), Some("oOopS")).futureValue

      result mustBe Right(validResponseOpenReturnPeriods)
      verify(repository).getOpenReturnPeriods(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(DUE_DATE), eqTo(ASC))
      verifyNoMoreInteractions(repository)
    }

    "return InvalidRegimeCode and not call repository when Regime input is invalid" in {
      val result = service.getOpenReturnPeriods("INVALID", lowercaseRegNumber, Some(STATUS), Some(ASC)).futureValue
      result mustBe Left(InvalidRegimeCode)
      verifyNoMoreInteractions(repository)
    }

    "return InvalidRegNumber and not call repository when RegNumber input is invalid" in {
      val invalidRegNumber = "xwm12345678"
      val result = service.getOpenReturnPeriods(validRegime.toString, invalidRegNumber, Some(STATUS), Some(ASC)).futureValue
      result mustBe Left(InvalidRegNumber)
      verifyNoMoreInteractions(repository)
    }

    "return UnexpectedError when repository throws exception" in {
      when(repository.getOpenReturnPeriods(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(STATUS), eqTo(ASC)))
        .thenReturn(Future.failed(new RuntimeException("DB failure when calling repo")))
      val result = service.getOpenReturnPeriods(validRegime.toString, lowercaseRegNumber, Some(STATUS), Some(ASC)).futureValue
      result mustBe Left(UnexpectedError)
      verify(repository).getOpenReturnPeriods(eqTo(validRegime), eqTo(normalisedRegNumber), eqTo(STATUS), eqTo(ASC))
      verifyNoMoreInteractions(repository)
    }
  }
}
