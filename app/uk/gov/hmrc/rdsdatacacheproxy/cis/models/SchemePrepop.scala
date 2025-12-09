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

final case class SchemePrepop(
  taxOfficeNumber: String,
  taxOfficeReference: String,
  agentOwnReference: String,
  utr: Option[String],
  schemeName: String
)

object SchemePrepop {
  implicit val format: OFormat[SchemePrepop] = Json.format[SchemePrepop]
}

final case class PrePopContractorBody(
  schemeName: String,
  utr: String,
  response: Int
)

object PrePopContractorBody {
  implicit val format: OFormat[PrePopContractorBody] = Json.format[PrePopContractorBody]
}

final case class PrePopContractorResponse(
  knownfacts: PrepopKnownFacts,
  prePopContractor: PrePopContractorBody
)

object PrePopContractorResponse {
  implicit val format: OFormat[PrePopContractorResponse] = Json.format[PrePopContractorResponse]
}
