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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.GamblingError.{InvalidMgdRegNumber, UnexpectedError}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.*
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.GamblingDataSource

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

      val summary = ReturnSummary(validMgdRegNumber, 3, 1)

      when(repository.getReturnSummary(eqTo(validMgdRegNumber)))
        .thenReturn(Future.successful(summary))

      val result = service.getReturnSummary(validMgdRegNumber).futureValue

      result mustBe Right(summary)
      verify(repository).getReturnSummary(eqTo(validMgdRegNumber))
      verifyNoMoreInteractions(repository)
    }

    "normalise input (trim + uppercase) before calling repository" in {

      val rawInput = "  xwm12345678901  "

      val summary = ReturnSummary(normalisedMgdRegNumber, 2, 1)

      when(repository.getReturnSummary(eqTo(normalisedMgdRegNumber)))
        .thenReturn(Future.successful(summary))

      val result = service.getReturnSummary(rawInput).futureValue

      result mustBe Right(summary)
      verify(repository).getReturnSummary(eqTo(normalisedMgdRegNumber))
      verifyNoMoreInteractions(repository)
    }

    "return InvalidMgdRegNumber and not call repository when input is invalid" in {

      val result = service.getReturnSummary("xwm12345678").futureValue

      result mustBe Left(InvalidMgdRegNumber)
      verifyNoMoreInteractions(repository)
    }

    "return UnexpectedError when repository fails" in {

      when(repository.getReturnSummary(eqTo(validMgdRegNumber)))
        .thenReturn(Future.failed(new RuntimeException("DB failure")))

      val result = service.getReturnSummary(validMgdRegNumber).futureValue

      result mustBe Left(UnexpectedError)
      verify(repository).getReturnSummary(eqTo(validMgdRegNumber))
      verifyNoMoreInteractions(repository)
    }
  }

  "GamblingService#getMgdCertificate" - {

    "return Right(certificate) when repository succeeds" in {

      val certificate = MgdCertificate(
        mgdRegNumber         = validMgdRegNumber,
        registrationDate     = None,
        individualName       = None,
        businessName         = None,
        tradingName          = None,
        repMemName           = None,
        busAddrLine1         = None,
        busAddrLine2         = None,
        busAddrLine3         = None,
        busAddrLine4         = None,
        busPostcode          = None,
        busCountry           = None,
        busAdi               = None,
        repMemLine1          = None,
        repMemLine2          = None,
        repMemLine3          = None,
        repMemLine4          = None,
        repMemPostcode       = None,
        repMemAdi            = None,
        typeOfBusiness       = None,
        businessTradeClass   = None,
        noOfPartners         = None,
        groupReg             = "",
        noOfGroupMems        = None,
        dateCertIssued       = None,
        partMembers          = Seq.empty,
        groupMembers         = Seq.empty,
        returnPeriodEndDates = Seq.empty
      )

      when(repository.getMgdCertificate(eqTo(validMgdRegNumber)))
        .thenReturn(Future.successful(certificate))

      val result = service.getMgdCertificate(validMgdRegNumber).futureValue

      result mustBe Right(certificate)

      verify(repository).getMgdCertificate(eqTo(validMgdRegNumber))
      verifyNoMoreInteractions(repository)
    }

    "return InvalidMgdRegNumber when input is invalid" in {

      val result = service.getMgdCertificate("invalid").futureValue

      result mustBe Left(InvalidMgdRegNumber)
      verifyNoMoreInteractions(repository)
    }

    "return UnexpectedError when repository fails" in {

      when(repository.getMgdCertificate(eqTo(validMgdRegNumber)))
        .thenReturn(Future.failed(new RuntimeException("fail")))

      val result = service.getMgdCertificate(validMgdRegNumber).futureValue

      result mustBe Left(UnexpectedError)
    }
  }

  "GamblingService#getOperatorDetails" - {

    "return Right(details when repository succeeds" in {

      val details =
        GamblingStubData.getOperatorDetails(validMgdRegNumber)

      when(repository.getOperatorDetails(eqTo(validMgdRegNumber)))
        .thenReturn(Future.successful(details))

      val result = service.getOperatorDetails(validMgdRegNumber).futureValue

      result mustBe Right(details)

      verify(repository).getOperatorDetails(eqTo(validMgdRegNumber))
      verifyNoMoreInteractions(repository)
    }

    "normalise input before calling repository" in {

      val raw = "  xwm12345678901  "

      val details =
        GamblingStubData.getOperatorDetails(normalisedMgdRegNumber)

      when(repository.getOperatorDetails(eqTo(normalisedMgdRegNumber)))
        .thenReturn(Future.successful(details))

      val result = service.getOperatorDetails(raw).futureValue

      result mustBe Right(details)

      verify(repository).getOperatorDetails(eqTo(normalisedMgdRegNumber))
      verifyNoMoreInteractions(repository)
    }

    "return InvalidMgdRegNumber when input invalid" in {

      val result = service.getOperatorDetails("bad").futureValue

      result mustBe Left(InvalidMgdRegNumber)
    }

    "return UnexpectedError when repository fails" in {

      when(repository.getOperatorDetails(eqTo(validMgdRegNumber)))
        .thenReturn(Future.failed(new RuntimeException("fail")))

      val result = service.getOperatorDetails(validMgdRegNumber).futureValue

      result mustBe Left(UnexpectedError)
    }
  }

  "GamblingService#getBusinessDetails" - {

    "return Right(details) when repository succeeds" in {

      val details =
        GamblingStubData.getBusinessDetails(validMgdRegNumber)

      when(repository.getBusinessDetails(eqTo(validMgdRegNumber)))
        .thenReturn(Future.successful(details))

      val result = service.getBusinessDetails(validMgdRegNumber).futureValue

      result mustBe Right(details)

      verify(repository).getBusinessDetails(eqTo(validMgdRegNumber))
      verifyNoMoreInteractions(repository)
    }

    "normalise input before calling repository" in {

      val raw = "  xwm12345678901  "

      val details =
        GamblingStubData.getBusinessDetails(normalisedMgdRegNumber)

      when(repository.getBusinessDetails(eqTo(normalisedMgdRegNumber)))
        .thenReturn(Future.successful(details))

      val result = service.getBusinessDetails(raw).futureValue

      result mustBe Right(details)

      verify(repository).getBusinessDetails(eqTo(normalisedMgdRegNumber))
      verifyNoMoreInteractions(repository)
    }

    "return InvalidMgdRegNumber when input invalid" in {

      val result = service.getBusinessDetails("bad").futureValue

      result mustBe Left(InvalidMgdRegNumber)
    }

    "return UnexpectedError when repository fails" in {

      when(repository.getBusinessDetails(eqTo(validMgdRegNumber)))
        .thenReturn(Future.failed(new RuntimeException("fail")))

      val result = service.getBusinessDetails(validMgdRegNumber).futureValue

      result mustBe Left(UnexpectedError)
    }
  }
}
