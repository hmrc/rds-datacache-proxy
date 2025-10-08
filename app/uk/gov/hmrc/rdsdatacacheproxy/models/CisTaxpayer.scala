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

package uk.gov.hmrc.rdsdatacacheproxy.models

import play.api.libs.json.{Json, OFormat}

case class CisTaxpayer(
  uniqueId: String,
  taxOfficeNumber: String,
  taxOfficeRef: String,
  aoDistrict: Option[String],
  aoPayType: Option[String],
  aoCheckCode: Option[String],
  aoReference: Option[String],
  validBusinessAddr: Option[String],
  correlation: Option[String],
  ggAgentId: Option[String],
  employerName1: Option[String],
  employerName2: Option[String],
  agentOwnRef: Option[String],
  schemeName: Option[String],
  utr: Option[String],
  enrolledSig: Option[String]
)

object CisTaxpayer {
  implicit val format: OFormat[CisTaxpayer] = Json.format[CisTaxpayer]
}
