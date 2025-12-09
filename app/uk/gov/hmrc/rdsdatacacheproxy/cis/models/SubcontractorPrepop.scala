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

package uk.gov.hmrc.rdsdatacacheproxy.cis.models

import play.api.libs.json.{Json, OFormat}

final case class SubcontractorPrepopRecord(
  subcontractorType: String,
  subcontractorUtr: String,
  verificationNumber: String,
  verificationSuffix: Option[String],
  title: Option[String],
  firstName: Option[String],
  secondName: Option[String],
  surname: Option[String],
  tradingName: Option[String]
)

object SubcontractorPrepopRecord {
  implicit val format: OFormat[SubcontractorPrepopRecord] = Json.format[SubcontractorPrepopRecord]
}

final case class PrePopSubcontractor(
  subcontractorType: String,
  utr: String,
  verificationNumber: String,
  verificationSuffix: String,
  title: String,
  firstName: String,
  secondName: String,
  surname: String
)

object PrePopSubcontractor {
  implicit val format: OFormat[PrePopSubcontractor] = Json.format[PrePopSubcontractor]
}

final case class PrePopSubcontractorsBody(
  response: Int,
  subcontractors: Seq[PrePopSubcontractor]
)

object PrePopSubcontractorsBody {
  implicit val format: OFormat[PrePopSubcontractorsBody] = Json.format[PrePopSubcontractorsBody]
}

final case class PrePopSubcontractorsResponse(
  knownfacts: PrepopKnownFacts,
  prePopSubcontractors: PrePopSubcontractorsBody
)

object PrePopSubcontractorsResponse {
  implicit val format: OFormat[PrePopSubcontractorsResponse] = Json.format[PrePopSubcontractorsResponse]
}
