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

package uk.gov.hmrc.rdsdatacacheproxy.pas.stub

import uk.gov.hmrc.rdsdatacacheproxy.pas.models.*

import java.time.LocalDate
object PasStubData {
  
  val createAppealSuccessResponse = CreateAppealResponse(created = true)

  val appealablePenalties: GetAppealablePenaltiesResponse =
    GetAppealablePenaltiesResponse(
      total = 2,
      appealablePenalties = List(
        GetAppealablePenaltyResponse(
          uniqueId = Some(10001),
          issueDate = Some(LocalDate.of(2023, 1, 1)),
          taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
          penaltyType = Some("F"),
          penaltyAmount = Some(BigDecimal(999.23))
        ),
        GetAppealablePenaltyResponse(
          uniqueId = Some(10002),
          issueDate = Some(LocalDate.of(2023, 1, 1)),
          taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
          penaltyType = Some("F"),
          penaltyAmount = Some(BigDecimal(999.23))
        )
      )
    )

  val appealablePenaltiesEmpty =
    GetAppealablePenaltiesResponse(
      total = 0,
      appealablePenalties = List.empty
    )

  val getAppealablePenaltySuccessResponse = GetAppealablePenaltyResponse(
    uniqueId = None,
    issueDate        = Some(LocalDate.of(2023, 1, 1)),
    taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
    penaltyType      = Some("CIS003"),
    penaltyAmount    = Some(BigDecimal(999.23))
  )

  val payeTaxpayer: GetPayeTaxpayerResponse = GetPayeTaxpayerResponse(
    uniqueId       = 1L,
    instanceId     = 1L,
    districtNumber = "123",
    employerRef    = "AB456",
    employerName1  = Some("TEST EMPLOYER"),
    employerName2  = Some("LTD"),
    agentOwnRef    = Some("AGENTREF")
  )

  def makeCreateAppealResponse(createAppealRequest: CreateAppealRequest): CreateAppealResponse = {
    createAppealRequest.penaltyId match {
      case 1 => createAppealSuccessResponse
      case _ => throw new RuntimeException("Error from downstream")
    }
  }

  def makeGetAppealablePenaltiesResponse(request: GetAppealablePenaltiesRequest): GetAppealablePenaltiesResponse = {
    request.hmrcOfficeNumber match {
      case "123" => appealablePenalties
      case "456" => appealablePenaltiesEmpty
      case _     => throw new Error("Simulated downstream failure")
    }
  }

  def makeGetAppealablePenaltyResponse(getAppealablePenaltyRequest: GetAppealablePenaltyRequest): GetAppealablePenaltyResponse = {
    getAppealablePenaltyRequest.penaltyId match {
      case 1 => getAppealablePenaltySuccessResponse
      case _ => throw new RuntimeException("Error from downstream")
    }
  }

  def makeGetPayeTaxpayerResponse(request: GetTaxpayerRequest): Option[GetPayeTaxpayerResponse] = {
    request.hmrcOfficeNumber match {
      case "123" => Some(payeTaxpayer)
      case "456" => None
      case _     => throw new Error("Error from downstream")
    }
  }
}
