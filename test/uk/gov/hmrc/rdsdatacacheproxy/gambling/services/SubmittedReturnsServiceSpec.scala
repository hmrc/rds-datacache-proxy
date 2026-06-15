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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError.{InvalidRegNumber, UnexpectedError}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.SubmittedReturnsDataSource
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseSubmittedReturns

import scala.concurrent.Future

final class SubmittedReturnsServiceSpec extends SpecBase {

  private val repository = mock[SubmittedReturnsDataSource]
  private val service = new SubmittedReturnsService(repository)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(repository)
  }

  private val lowercaseRegNumber = "xwm12345678901 "
  private val normalisedRegNumber = "XWM12345678901"
  private val ASC = "ASC"
  private val DESC = "DESC"
  private val PERIOD_START_DATE = 1
  private val SUBMITTED_DATE = 2
  private val PERIOD_END_DATE = 3

  "SubmittedReturnsService#getSubmittedReturns" - {

    "return validResponseSubmittedReturns when repository succeeds AND normalise input (trim + uppercase) before calling repository" in {
      when(repository.getSubmittedReturns(eqTo(normalisedRegNumber), eqTo(PERIOD_END_DATE), eqTo(ASC)))
        .thenReturn(Future.successful(validResponseSubmittedReturns))

      val result = service.getSubmittedReturns(lowercaseRegNumber, Some(PERIOD_END_DATE), Some("   asc   ")).futureValue

      result mustBe Right(validResponseSubmittedReturns)
      verify(repository).getSubmittedReturns(eqTo(normalisedRegNumber), eqTo(PERIOD_END_DATE), eqTo(ASC))
      verifyNoMoreInteractions(repository)
    }

    "return validResponseSubmittedReturns with CORRECT DEFAULTS when sortBy & orderBy are NONE" in {
      when(repository.getSubmittedReturns(eqTo(normalisedRegNumber), eqTo(PERIOD_END_DATE), eqTo(ASC)))
        .thenReturn(Future.successful(validResponseSubmittedReturns))

      val result = service.getSubmittedReturns(lowercaseRegNumber, None, None).futureValue

      result mustBe Right(validResponseSubmittedReturns)
      verify(repository).getSubmittedReturns(eqTo(normalisedRegNumber), eqTo(PERIOD_END_DATE), eqTo(ASC))
      verifyNoMoreInteractions(repository)
    }

    "return validResponseSubmittedReturns when sortBy is RANDOM & orderBy is DESC" in {
      when(repository.getSubmittedReturns(eqTo(normalisedRegNumber), eqTo(PERIOD_END_DATE), eqTo(DESC)))
        .thenReturn(Future.successful(validResponseSubmittedReturns))

      val result = service.getSubmittedReturns(lowercaseRegNumber, Some(999), Some(DESC)).futureValue

      result mustBe Right(validResponseSubmittedReturns)
      verify(repository).getSubmittedReturns(eqTo(normalisedRegNumber), eqTo(PERIOD_END_DATE), eqTo(DESC))
      verifyNoMoreInteractions(repository)
    }

    "return validResponseSubmittedReturns when sortBy is 1 & orderBy is ASC" in {
      when(repository.getSubmittedReturns(eqTo(normalisedRegNumber), eqTo(PERIOD_START_DATE), eqTo(ASC)))
        .thenReturn(Future.successful(validResponseSubmittedReturns))

      val result = service.getSubmittedReturns(lowercaseRegNumber, Some(1), Some(ASC)).futureValue

      result mustBe Right(validResponseSubmittedReturns)
      verify(repository).getSubmittedReturns(eqTo(normalisedRegNumber), eqTo(PERIOD_START_DATE), eqTo(ASC))
      verifyNoMoreInteractions(repository)
    }

    "return validResponseSubmittedReturns when sortBy is 2 & orderBy is LOWERCASE asc" in {
      when(repository.getSubmittedReturns(eqTo(normalisedRegNumber), eqTo(SUBMITTED_DATE), eqTo(ASC)))
        .thenReturn(Future.successful(validResponseSubmittedReturns))

      val result = service.getSubmittedReturns(lowercaseRegNumber, Some(2), Some("asc")).futureValue

      result mustBe Right(validResponseSubmittedReturns)
      verify(repository).getSubmittedReturns(eqTo(normalisedRegNumber), eqTo(SUBMITTED_DATE), eqTo(ASC))
      verifyNoMoreInteractions(repository)
    }

    "return validResponseSubmittedReturns when sortBy is 2 & orderBy is RANDOM" in {
      when(repository.getSubmittedReturns(eqTo(normalisedRegNumber), eqTo(SUBMITTED_DATE), eqTo(ASC)))
        .thenReturn(Future.successful(validResponseSubmittedReturns))

      val result = service.getSubmittedReturns(lowercaseRegNumber, Some(2), Some("oOopS")).futureValue

      result mustBe Right(validResponseSubmittedReturns)
      verify(repository).getSubmittedReturns(eqTo(normalisedRegNumber), eqTo(SUBMITTED_DATE), eqTo(ASC))
      verifyNoMoreInteractions(repository)
    }

    "return InvalidRegNumber and not call repository when RegNumber input is invalid" in {
      val invalidRegNumber = "xwm12345678"
      val result = service.getSubmittedReturns(invalidRegNumber, Some(PERIOD_END_DATE), Some(ASC)).futureValue
      result mustBe Left(InvalidRegNumber)
      verifyNoMoreInteractions(repository)
    }

    "return UnexpectedError when repository throws exception" in {
      when(repository.getSubmittedReturns(eqTo(normalisedRegNumber), eqTo(PERIOD_END_DATE), eqTo(ASC)))
        .thenReturn(Future.failed(new RuntimeException("DB failure when calling repo")))
      val result = service.getSubmittedReturns(lowercaseRegNumber, Some(PERIOD_END_DATE), Some(ASC)).futureValue
      result mustBe Left(UnexpectedError)
      verify(repository).getSubmittedReturns(eqTo(normalisedRegNumber), eqTo(PERIOD_END_DATE), eqTo(ASC))
      verifyNoMoreInteractions(repository)
    }
  }
}
