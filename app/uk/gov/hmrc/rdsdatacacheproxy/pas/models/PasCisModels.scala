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

case class CreateCisAppealRequest(
  penaltyId: Long
)

object CreateCisAppealRequest {
  implicit val format: OFormat[CreateCisAppealRequest] = Json.format[CreateCisAppealRequest]
}

case class CreateCisAppealResponse(
  created: Boolean
)

object CreateCisAppealResponse {
  implicit val format: OFormat[CreateCisAppealResponse] = Json.format[CreateCisAppealResponse]
}

case class GetCisAppealablePenaltiesRequest(
  hmrcOfficeNumber: String,
  schemeReferenceNumber: String,
  startRow: Long,
  endRow: Long
)

object GetCisAppealablePenaltiesRequest {
  implicit val format: OFormat[GetCisAppealablePenaltiesRequest] = Json.format[GetCisAppealablePenaltiesRequest]
}

case class GetCisAppealablePenaltiesResponse(
  total: Long,
  appealablePenalties: Seq[GetCisAppealablePenaltyResponse]
)

object GetCisAppealablePenaltiesResponse {
  implicit val format: OFormat[GetCisAppealablePenaltiesResponse] = Json.format[GetCisAppealablePenaltiesResponse]
}

case class GetCisAppealablePenaltyRequest(
  penaltyId: Long,
  hmrcOfficeNumber: String,
  schemeReferenceNumber: String
)

object GetCisAppealablePenaltyRequest {
  implicit val format: OFormat[GetCisAppealablePenaltyRequest] = Json.format[GetCisAppealablePenaltyRequest]
}

case class GetCisAppealablePenaltyResponse(
  uniqueId: Option[Long],
  issueDate: Option[LocalDate],
  taxPeriodEndDate: Option[LocalDate],
  penaltyType: Option[String],
  penaltyAmount: Option[BigDecimal]
)

object GetCisAppealablePenaltyResponse {
  implicit val format: OFormat[GetCisAppealablePenaltyResponse] = Json.format[GetCisAppealablePenaltyResponse]
}

case class GetCisPenaltyAllowedRequest(
                                           districtNumber: Long,
                                           employerReference: String,
                                           uniqueId: Long
                                         )

object GetCisPenaltyAllowedRequest {
  implicit val format: OFormat[GetCisPenaltyAllowedRequest] = Json.format[GetCisPenaltyAllowedRequest]
}

case class GetCisPenaltyAllowedResponse(
                                              allowed: Boolean
                                            )

object GetCisPenaltyAllowedResponse {
  implicit val format: OFormat[GetCisPenaltyAllowedResponse] = Json.format[GetCisPenaltyAllowedResponse]
}
