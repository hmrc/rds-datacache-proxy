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

import play.api.Logging
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.*

import java.time.LocalDate
import scala.concurrent.Future
object PasStubData extends Logging {
  
  val createAppealSuccessResponse = CreateAppealResponse(created = true)

  val appealablePenalties: GetAppealablePenaltiesResponse =
    GetAppealablePenaltiesResponse(
      total = 2,
      appealablePenalties = List(
        GetAppealablePenaltyResponse(
          penaltyId = Some(10001),
          issueDate = Some(LocalDate.of(2023, 1, 1)),
          taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
          penaltyType = Some("F"),
          penaltyAmount = Some(BigDecimal(999.23))
        ),
        GetAppealablePenaltyResponse(
          penaltyId = Some(10002),
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
    penaltyId = None,
    issueDate        = Some(LocalDate.of(2023, 1, 1)),
    taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
    penaltyType      = Some("CIS003"),
    penaltyAmount    = Some(BigDecimal(999.23))
  )

  val getPenaltyAllowedSuccessResponse = GetPenaltyAllowedResponse(
    allowed = true
  )

  val payeTaxpayer: GetPayeTaxpayerResponse = GetPayeTaxpayerResponse(
    penaltyId       = 1L,
    instanceId     = 1L,
    hmrcOfficeNumber = "123",
    schemeReferenceNumber    = "AB456",
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

  def makeGetPenaltyAllowedResponse(request: GetPenaltyAllowedRequest): GetPenaltyAllowedResponse = {
    request.penaltyId match {
      case 1 => getPenaltyAllowedSuccessResponse
      case _ => throw new Error("Simulated downstream failure")
    }
  }

  def getClientListDownloadStatusResponse(credentialId: String, serviceName: String, gracePeriod: Int): Future[Int] = {
    val credentialIdExists = Option(credentialId).exists(_.trim.nonEmpty)
    val serviceNameExists = Option(serviceName).exists(_.trim.nonEmpty)

    if (credentialIdExists && serviceNameExists) {
      logger.info(
        s"[PAS-STUB] getClientListDownloadStatus -> CREDENTIAL_ID=${Option(credentialId).map(_.trim).getOrElse("")}, SERVICE_NAME=${Option(serviceName).map(_.trim).getOrElse("")} => status=1"
      )
      Future.successful(1)
    } else {
      logger.warn(
        s"[PAS-STUB] getClientListDownloadStatus -> missing/blank CREDENTIAL_ID/SERVICE_NAME: CREDENTIAL_ID=${Option(credentialId).map(_.trim).getOrElse("")}, SERVICE_NAME=${Option(serviceName).map(_.trim).getOrElse("")} "
      )
      Future.successful(2)
    }
  }
}
