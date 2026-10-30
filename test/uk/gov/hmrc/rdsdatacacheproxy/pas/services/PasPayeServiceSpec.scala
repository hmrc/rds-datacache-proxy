/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy.pas.services

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.*
import org.scalatest.matchers.must.Matchers.mustBe
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.*
import uk.gov.hmrc.rdsdatacacheproxy.pas.repositories.PasPayeSource

import java.time.LocalDate
import scala.concurrent.Future

final class PasPayeServiceSpec extends SpecBase {

  private val source = mock[PasPayeSource]
  private val service = new PasPayeService(source)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(source)
  }

  "PasPayeService.createPayeAppeal" - {

    "return CreateAppealResponse when the repository returns CreateAppealResponse" in {
      val createPayeAppealRequest = CreateAppealRequest(1L)
      val createPayeAppealResponse = CreateAppealResponse(true)

      when(source.createPayeAppeal(createPayeAppealRequest)).thenReturn(Future.successful(createPayeAppealResponse))

      val out = service.createPayeAppeal(createPayeAppealRequest).futureValue

      out mustBe createPayeAppealResponse

      verify(source).createPayeAppeal(eqTo(createPayeAppealRequest))
      verifyNoMoreInteractions(source)
    }

    "propagate upstream failures from the repository" in {
      val createPayeAppealRequest = CreateAppealRequest(1L)
      val boom = UpstreamErrorResponse("database connection failed", 500)

      when(source.createPayeAppeal(createPayeAppealRequest)).thenReturn(Future.failed(boom))

      val ex = service
        .createPayeAppeal(createPayeAppealRequest)
        .failed
        .futureValue

      ex mustBe boom
      verify(source).createPayeAppeal(createPayeAppealRequest)
      verifyNoMoreInteractions(source)
    }
  }

  "PasPayeService.getPayeAppealablePenalties" - {

    val testRequest: GetAppealablePenaltiesRequest = GetAppealablePenaltiesRequest(
      hmrcOfficeNumber      = "123",
      schemeReferenceNumber = "AB456",
      startRow              = 1L,
      endRow                = 10L
    )
    val testResponse: GetAppealablePenaltiesResponse = GetAppealablePenaltiesResponse(
      total = 2,
      appealablePenalties = List(
        GetAppealablePenaltyResponse(
          uniqueId         = Some(10001),
          issueDate        = Some(LocalDate.of(2023, 1, 1)),
          taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
          penaltyType      = Some("F"),
          penaltyAmount    = Some(BigDecimal(999.23))
        ),
        GetAppealablePenaltyResponse(
          uniqueId         = Some(10002),
          issueDate        = Some(LocalDate.of(2026, 2, 20)),
          taxPeriodEndDate = Some(LocalDate.of(2026, 1, 5)),
          penaltyType      = Some("P"),
          penaltyAmount    = Some(BigDecimal(928.23))
        )
      )
    )

    "return GetAppealablePenaltiesResponse from the repository" in {
      when(source.getPayeAppealablePenalties(testRequest)).thenReturn(Future.successful(testResponse))

      val out = service.getPayeAppealablePenalties(testRequest).futureValue

      out mustBe testResponse

      verify(source).getPayeAppealablePenalties(eqTo(testRequest))
      verifyNoMoreInteractions(source)
    }

    "propagate upstream failures from the repository" in {
      val boom = UpstreamErrorResponse("database connection failed", 500)

      when(source.getPayeAppealablePenalties(testRequest)).thenReturn(Future.failed(boom))

      val ex = service
        .getPayeAppealablePenalties(testRequest)
        .failed
        .futureValue

      ex mustBe boom
      verify(source).getPayeAppealablePenalties(testRequest)
      verifyNoMoreInteractions(source)
    }
  }

  "PasPayeService.getPayeAppealablePenalty" - {

    val testRequest: GetAppealablePenaltyRequest = GetAppealablePenaltyRequest(
      penaltyId             = 1L,
      hmrcOfficeNumber      = "840123",
      schemeReferenceNumber = "098345"
    )

    val testResponse: GetAppealablePenaltyResponse = GetAppealablePenaltyResponse(
      uniqueId         = None,
      issueDate        = Some(LocalDate.of(2023, 1, 1)),
      taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
      penaltyType      = Some("CIS003"),
      penaltyAmount    = Some(BigDecimal(999.23))
    )

    "return GetAppealablePenaltyResponse from the repository" in {
      when(source.getPayeAppealablePenalty(testRequest)).thenReturn(Future.successful(testResponse))

      val out = service.getPayeAppealablePenalty(testRequest).futureValue

      out mustBe testResponse

      verify(source).getPayeAppealablePenalty(eqTo(testRequest))
      verifyNoMoreInteractions(source)
    }

    "propagate upstream failures from the repository" in {
      val boom = UpstreamErrorResponse("database connection failed", 500)

      when(source.getPayeAppealablePenalty(testRequest)).thenReturn(Future.failed(boom))

      val ex = service
        .getPayeAppealablePenalty(testRequest)
        .failed
        .futureValue

      ex mustBe boom
      verify(source).getPayeAppealablePenalty(testRequest)
      verifyNoMoreInteractions(source)
    }
  }

  "PasPayeService.getPayeTaxpayer" - {

    val testRequest: GetTaxpayerRequest = GetTaxpayerRequest(
      hmrcOfficeNumber      = "123",
      schemeReferenceNumber = "AB456"
    )
    val testResponse: GetPayeTaxpayerResponse = GetPayeTaxpayerResponse(
      uniqueId       = 1L,
      instanceId     = 2L,
      districtNumber = "123",
      employerRef    = "AB456",
      employerName1  = Some("TEST EMPLOYER"),
      employerName2  = Some("LTD"),
      agentOwnRef    = Some("AGENTREF")
    )

    "return Some(GetPayeTaxpayerResponse) from the repository" in {
      when(source.getPayeTaxpayer(testRequest)).thenReturn(Future.successful(Some(testResponse)))

      val out = service.getPayeTaxpayer(testRequest).futureValue

      out mustBe Some(testResponse)

      verify(source).getPayeTaxpayer(eqTo(testRequest))
      verifyNoMoreInteractions(source)
    }

    "return None when the repository finds no taxpayer" in {
      when(source.getPayeTaxpayer(testRequest)).thenReturn(Future.successful(None))

      val out = service.getPayeTaxpayer(testRequest).futureValue

      out mustBe None

      verify(source).getPayeTaxpayer(eqTo(testRequest))
      verifyNoMoreInteractions(source)
    }

    "propagate upstream failures from the repository" in {
      val boom = UpstreamErrorResponse("database connection failed", 500)

      when(source.getPayeTaxpayer(testRequest)).thenReturn(Future.failed(boom))

      val ex = service
        .getPayeTaxpayer(testRequest)
        .failed
        .futureValue

      ex mustBe boom
      verify(source).getPayeTaxpayer(testRequest)
      verifyNoMoreInteractions(source)
    }
  }

  "PasCisService.getPayePenaltyAllowed" - {

    val testRequest: GetPenaltyAllowedRequest = GetPenaltyAllowedRequest(
      districtNumber    = 1L,
      employerReference = "357",
      uniqueId          = 1L
    )

    val testResponse: GetPenaltyAllowedResponse = GetPenaltyAllowedResponse(
      allowed = true
    )

    "return GetPenaltyAllowedResponse from the repository" in {
      when(source.getPayePenaltyAllowed(testRequest)).thenReturn(Future.successful(testResponse))

      val out = service.getPayePenaltyAllowed(testRequest).futureValue

      out mustBe testResponse

      verify(source).getPayePenaltyAllowed(eqTo(testRequest))
      verifyNoMoreInteractions(source)
    }

    "propagate upstream failures from the repository" in {
      val boom = UpstreamErrorResponse("database connection failed", 500)

      when(source.getPayePenaltyAllowed(testRequest)).thenReturn(Future.failed(boom))

      val ex = service
        .getPayePenaltyAllowed(testRequest)
        .failed
        .futureValue

      ex mustBe boom
      verify(source).getPayePenaltyAllowed(testRequest)
      verifyNoMoreInteractions(source)
    }
  }
}
