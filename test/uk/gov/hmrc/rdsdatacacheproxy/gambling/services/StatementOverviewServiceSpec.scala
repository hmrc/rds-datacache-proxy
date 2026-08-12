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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError.{InvalidRegNumber, InvalidRegimeCode, RecordNotFound, UnexpectedError}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.StatementOverviewDataSource
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseStatementOverview

import scala.concurrent.Future

final class StatementOverviewServiceSpec extends SpecBase {

  private val repository = mock[StatementOverviewDataSource]
  private val service = new StatementOverviewService(repository)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(repository)
  }

  private val validRegime = Regime.values.head
  private val lowercaseRegNumber = "xwm12345678901 "
  private val normalisedRegNumber = "XWM12345678901"

  "StatementOverviewService#getStatementOverview" - {

    "return Right(StatementOverview) when repository returns Some and normalise input (trim + uppercase)" in {
      when(repository.getStatementOverview(eqTo(validRegime), eqTo(normalisedRegNumber)))
        .thenReturn(Future.successful(Some(validResponseStatementOverview)))

      val result = service.getStatementOverview(validRegime.toString, lowercaseRegNumber).futureValue

      result mustBe Right(validResponseStatementOverview)
      verify(repository).getStatementOverview(eqTo(validRegime), eqTo(normalisedRegNumber))
      verifyNoMoreInteractions(repository)
    }

    "return Left(StatementNotFound) when repository returns None" in {
      when(repository.getStatementOverview(eqTo(validRegime), eqTo(normalisedRegNumber)))
        .thenReturn(Future.successful(None))

      val result = service.getStatementOverview(validRegime.toString, lowercaseRegNumber).futureValue

      result mustBe Left(RecordNotFound)
      verify(repository).getStatementOverview(eqTo(validRegime), eqTo(normalisedRegNumber))
      verifyNoMoreInteractions(repository)
    }

    "return Left(InvalidRegimeCode) and not call repository when regime is invalid" in {
      val result = service.getStatementOverview("INVALID", lowercaseRegNumber).futureValue

      result mustBe Left(InvalidRegimeCode)
      verifyNoMoreInteractions(repository)
    }

    "return Left(InvalidRegNumber) and not call repository when regNumber has invalid format" in {
      val result = service.getStatementOverview(validRegime.toString, "xwm12345678").futureValue

      result mustBe Left(InvalidRegNumber)
      verifyNoMoreInteractions(repository)
    }

    "return Left(UnexpectedError) when repository throws an exception" in {
      when(repository.getStatementOverview(eqTo(validRegime), eqTo(normalisedRegNumber)))
        .thenReturn(Future.failed(new RuntimeException("DB failure")))

      val result = service.getStatementOverview(validRegime.toString, lowercaseRegNumber).futureValue

      result mustBe Left(UnexpectedError)
      verify(repository).getStatementOverview(eqTo(validRegime), eqTo(normalisedRegNumber))
      verifyNoMoreInteractions(repository)
    }
  }
}
