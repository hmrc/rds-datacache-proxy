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

package uk.gov.hmrc.rdsdatacacheproxy.pas.models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class CreateAppealRequest(
  penaltyId: Long
)

object CreateAppealRequest {
  implicit val format: OFormat[CreateAppealRequest] = Json.format[CreateAppealRequest]
}

case class CreateAppealResponse(
  created: Boolean
)

object CreateAppealResponse {
  implicit val format: OFormat[CreateAppealResponse] = Json.format[CreateAppealResponse]
}

case class GetAppealablePenaltiesRequest(
  hmrcOfficeNumber: String,
  schemeReferenceNumber: String,
  startRow: Long,
  endRow: Long
)

object GetAppealablePenaltiesRequest {
  implicit val format: OFormat[GetAppealablePenaltiesRequest] = Json.format[GetAppealablePenaltiesRequest]
}

case class GetAppealablePenaltiesResponse(
  total: Long,
  appealablePenalties: Seq[GetAppealablePenaltyResponse]
)

object GetAppealablePenaltiesResponse {
  implicit val format: OFormat[GetAppealablePenaltiesResponse] = Json.format[GetAppealablePenaltiesResponse]
}

case class GetAppealablePenaltyRequest(
  penaltyId: Long,
  hmrcOfficeNumber: String,
  schemeReferenceNumber: String
)

object GetAppealablePenaltyRequest {
  implicit val format: OFormat[GetAppealablePenaltyRequest] = Json.format[GetAppealablePenaltyRequest]
}

case class GetAppealablePenaltyResponse(
  uniqueId: Option[Long],
  issueDate: Option[LocalDate],
  taxPeriodEndDate: Option[LocalDate],
  penaltyType: Option[String],
  penaltyAmount: Option[BigDecimal]
)

object GetAppealablePenaltyResponse {
  implicit val format: OFormat[GetAppealablePenaltyResponse] = Json.format[GetAppealablePenaltyResponse]
}

case class GetTaxpayerRequest(
  hmrcOfficeNumber: String,
  schemeReferenceNumber: String
)

object GetTaxpayerRequest {
  implicit val format: OFormat[GetTaxpayerRequest] = Json.format[GetTaxpayerRequest]
}

case class GetPayeTaxpayerResponse(
  uniqueId: Long,
  instanceId: Long,
  districtNumber: String,
  employerRef: String,
  employerName1: Option[String],
  employerName2: Option[String],
  agentOwnRef: Option[String]
)

object GetPayeTaxpayerResponse {
  implicit val format: OFormat[GetPayeTaxpayerResponse] = Json.format[GetPayeTaxpayerResponse]
}
