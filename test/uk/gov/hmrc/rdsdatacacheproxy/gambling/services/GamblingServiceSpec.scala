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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.*
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.GamblingError.{InvalidMgdRegNumber, UnexpectedError}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.GamblingDataSource

import java.time.LocalDate
import scala.concurrent.Future

final class GamblingServiceSpec extends SpecBase {

  private val repository = mock[GamblingDataSource]
  private val service = new GamblingService(repository)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(repository)
  }

  private val validMgdRegNumber = "XWM12345678901"
  private val normalisedMgdRegNumber = "XWM12345678901"

  "GamblingService#getReturnSummary" - {

    "return Right(summary) when repository succeeds" in {

      val summary = ReturnSummary(mgdRegNumber = validMgdRegNumber, returnsDue = 3, returnsOverdue = 1)

      when(repository.getReturnSummary(eqTo(validMgdRegNumber)))
        .thenReturn(Future.successful(summary))

      val result = service.getReturnSummary(validMgdRegNumber).futureValue

      result mustBe Right(summary)
      verify(repository).getReturnSummary(eqTo(validMgdRegNumber))
      verifyNoMoreInteractions(repository)
    }

    "normalise input (trim + uppercase) before calling repository" in {

      val rawInput = "  xwm12345678901  "

      val summary = ReturnSummary(
        mgdRegNumber   = normalisedMgdRegNumber,
        returnsDue     = 2,
        returnsOverdue = 1
      )

      when(repository.getReturnSummary(eqTo(normalisedMgdRegNumber)))
        .thenReturn(Future.successful(summary))

      val result = service.getReturnSummary(rawInput).futureValue
      result mustBe Right(summary)
      verify(repository).getReturnSummary(eqTo(normalisedMgdRegNumber))
      verifyNoMoreInteractions(repository)
    }

    "return InvalidMgdRegNumber and not call repository when input is invalid" in {

      val invalidInput = "xwm12345678"
      val result = service.getReturnSummary(invalidInput).futureValue
      result mustBe Left(InvalidMgdRegNumber)
      verifyNoMoreInteractions(repository)
    }

    "return UnexpectedError when repository throws exception" in {

      when(repository.getReturnSummary(eqTo(validMgdRegNumber)))
        .thenReturn(Future.failed(new RuntimeException("DB failure when calling repo")))
      val result = service.getReturnSummary(validMgdRegNumber).futureValue
      result mustBe Left(UnexpectedError)
      verify(repository).getReturnSummary(eqTo(validMgdRegNumber))
      verifyNoMoreInteractions(repository)
    }
  }
  "GamblingService#getBusinessName" - {

    "return Right(summary) when repository succeeds" in {

      val summary = BusinessName(
        mgdRegNumber      = validMgdRegNumber,
        solePropTitle     = "Mr",
        solePropFirstName = "Foo",
        solePropMidName   = Some("B"),
        solePropLastName  = "Bar",
        businessName      = "FooBar Co.",
        businessType      = 1,
        tradingName       = "Foobar",
        systemDate        = Some(LocalDate.of(1991, 1, 1))
      )

      when(repository.getBusinessName(eqTo(validMgdRegNumber)))
        .thenReturn(Future.successful(summary))

      val result = service.getBusinessName(validMgdRegNumber).futureValue

      result mustBe Right(summary)
      verify(repository).getBusinessName(eqTo(validMgdRegNumber))
      verifyNoMoreInteractions(repository)
    }

    "normalise input (trim + uppercase) before calling repository" in {

      val rawInput = "  xwm12345678901  "

      val summary = BusinessName(
        mgdRegNumber      = normalisedMgdRegNumber,
        solePropTitle     = "Mr",
        solePropFirstName = "John",
        solePropMidName   = Some("C"),
        solePropLastName  = "Doe",
        businessName      = "John Doe Co.",
        businessType      = 1,
        tradingName       = "DoeDoe",
        systemDate        = Some(LocalDate.of(1991, 1, 1))
      )

      when(repository.getBusinessName(eqTo(normalisedMgdRegNumber)))
        .thenReturn(Future.successful(summary))

      val result = service.getBusinessName(rawInput).futureValue
      result mustBe Right(summary)
      verify(repository).getBusinessName(eqTo(normalisedMgdRegNumber))
      verifyNoMoreInteractions(repository)
    }

    "return InvalidMgdRegNumber and not call repository when input is invalid" in {

      val invalidInput = "xwm12345678"
      val result = service.getBusinessName(invalidInput).futureValue
      result mustBe Left(InvalidMgdRegNumber)
      verifyNoMoreInteractions(repository)
    }

    "return UnexpectedError when repository throws exception" in {

      when(repository.getReturnSummary(eqTo(validMgdRegNumber)))
        .thenReturn(Future.failed(new RuntimeException("DB failure when calling repo")))
      val result = service.getReturnSummary(validMgdRegNumber).futureValue
      result mustBe Left(UnexpectedError)
      verify(repository).getReturnSummary(eqTo(validMgdRegNumber))
      verifyNoMoreInteractions(repository)
    }
  }

  "GamblingService#BusinessDetails" - {

    "return Right(summary) when repository succeeds" in {

      val summary = BusinessDetails(
        mgdRegNumber          = normalisedMgdRegNumber,
        businessType          = 1,
        currentlyRegistered   = 2,
        groupReg              = "foo",
        dateOfRegistration    = Some(LocalDate.of(2024, 4, 21)),
        businessPartnerNumber = "bar",
        systemDate            = Some(LocalDate.of(2024, 4, 21))
      )
      when(repository.getBusinessDetails(eqTo(validMgdRegNumber)))
        .thenReturn(Future.successful(summary))

      val result = service.getBusinessDetails(validMgdRegNumber).futureValue

      result mustBe Right(summary)
      verify(repository).getBusinessDetails(eqTo(validMgdRegNumber))
      verifyNoMoreInteractions(repository)
    }

    "normalise input (trim + uppercase) before calling repository" in {

      val rawInput = "  xwm12345678901  "

      val summary = BusinessDetails(
        mgdRegNumber          = normalisedMgdRegNumber,
        businessType          = 1,
        currentlyRegistered   = 2,
        groupReg              = "foo",
        dateOfRegistration    = Some(LocalDate.of(2024, 4, 21)),
        businessPartnerNumber = "bar",
        systemDate            = Some(LocalDate.of(2024, 4, 21))
      )

      when(repository.getBusinessDetails(eqTo(normalisedMgdRegNumber)))
        .thenReturn(Future.successful(summary))

      val result = service.getBusinessDetails(rawInput).futureValue
      result mustBe Right(summary)
      verify(repository).getBusinessDetails(eqTo(normalisedMgdRegNumber))
      verifyNoMoreInteractions(repository)
    }

    "return InvalidMgdRegNumber and not call repository when input is invalid" in {

      val invalidInput = "xwm12345678"
      val result = service.getBusinessDetails(invalidInput).futureValue
      result mustBe Left(InvalidMgdRegNumber)
      verifyNoMoreInteractions(repository)
    }

    "return UnexpectedError when repository throws exception" in {

      when(repository.getBusinessDetails(eqTo(validMgdRegNumber)))
        .thenReturn(Future.failed(new RuntimeException("DB failure when calling repo")))
      val result = service.getBusinessDetails(validMgdRegNumber).futureValue
      result mustBe Left(UnexpectedError)
      verify(repository).getBusinessDetails(eqTo(validMgdRegNumber))
      verifyNoMoreInteractions(repository)
    }
  }
}
