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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.models
import play.api.libs.json.{Json, OFormat}
import java.sql.Date

case class BusinessName(
  mgdRegNumber: String,
  solePropType: String,
  solePropFirstName: String,
  solePropMidName: String,
  solePropLastName: String,
  businessName: String,
  businessType: String,
  tradingName: String,
  systemDate: Date
)

object BusinessName {
  implicit val format: OFormat[BusinessName] = Json.format[BusinessName]
}
