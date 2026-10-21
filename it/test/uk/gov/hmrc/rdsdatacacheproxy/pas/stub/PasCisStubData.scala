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
object PasCisStubData {
  
  val createCisAppealSuccessResponse = CreateCisAppealResponse(created = true)

  val appealablePenalties: GetCisAppealablePenaltiesResponse =
    GetCisAppealablePenaltiesResponse(
      total = 2,
      appealablePenalties = List(
        GetCisAppealablePenaltyResponse(
          uniqueId = Some(10001),
          issueDate = Some(LocalDate.of(2023, 1, 1)),
          taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
          penaltyType = Some("CIS03"),
          penaltyAmount = Some(BigDecimal(999.23))
        ),
        GetCisAppealablePenaltyResponse(
          uniqueId = Some(10002),
          issueDate = Some(LocalDate.of(2023, 1, 1)),
          taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
          penaltyType = Some("CIS03"),
          penaltyAmount = Some(BigDecimal(999.23))
        )
      )
    )

  val appealablePenaltiesEmpty =
    GetCisAppealablePenaltiesResponse(
      total = 0,
      appealablePenalties = List.empty
    )

  val getCisAppealablePenaltySuccessResponse = GetCisAppealablePenaltyResponse(
    uniqueId = None,
    issueDate        = Some(LocalDate.of(2023, 1, 1)),
    taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
    penaltyType      = Some("CIS003"),
    penaltyAmount    = Some(BigDecimal(999.23))
  )

  def makeCreateCisAppealResponse(createCisAppealRequest: CreateCisAppealRequest): CreateCisAppealResponse = {
    createCisAppealRequest.penaltyId match {
      case 1 => createCisAppealSuccessResponse
      case _ => throw new RuntimeException("Error from downstream")
    }
  }

  def makeGetCisAppealablePenaltiesResponse(request: GetCisAppealablePenaltiesRequest): GetCisAppealablePenaltiesResponse = {
    request.hmrcOfficeNumber match {
      case "123" => appealablePenalties
      case "456" => appealablePenaltiesEmpty
      case _     => throw new Error("Simulated downstream failure")
    }
  }

  def makeGetCisAppealablePenaltyResponse(getCisAppealablePenaltyRequest: GetCisAppealablePenaltyRequest): GetCisAppealablePenaltyResponse = {
    getCisAppealablePenaltyRequest.penaltyId match {
      case 1 => getCisAppealablePenaltySuccessResponse
      case _ => throw new RuntimeException("Error from downstream")
    }
  }
}
