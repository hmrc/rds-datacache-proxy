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

package uk.gov.hmrc.rdsdatacacheproxy.nova.models

import play.api.libs.json.{Format, Json}

case class VehicleCalculationData(
  exchangeRate: Option[BigDecimal],
  vatRateEffectiveDate: Option[String],
  vatRate: Option[BigDecimal],
  minLimitEffDate: Option[String],
  minLimitAmount: Option[BigDecimal],
  thresholdDaysEffDate: Option[String],
  thresholdDays: Option[Int],
  rateEffDate: Option[String],
  rateAmount: Option[BigDecimal],
  maxNoOfDaysEffDate: Option[String],
  maxNoOfDays: Option[Int],
  altAmtEffDate: Option[String],
  altAmt: Option[BigDecimal]
)

object VehicleCalculationData {
  implicit val format: Format[VehicleCalculationData] = Json.format[VehicleCalculationData]
}
