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

package uk.gov.hmrc.rdsdatacacheproxy.ct.models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class AccountingPeriods(accountingPeriods: List[AccountingPeriodsDetails])

object AccountingPeriods {
  implicit val format: OFormat[AccountingPeriods] = Json.format[AccountingPeriods]
}

case class AccountingPeriodsDetails(
  accountingPeriod: BigDecimal,
  apStartDate: LocalDate,
  apEndDate: LocalDate,
  apStatus: String,
  taxChargePresent: Boolean,
  clericalIntSig: Boolean,
  creditDebitInterestInd: Boolean,
  taxTotal: Option[BigDecimal],
  interestTotal: Option[BigDecimal],
  penaltyTotal: Option[BigDecimal],
  payslipTotal: Option[BigDecimal],
  repayReallocTotal: Option[BigDecimal],
  adjustmentTotal: Option[BigDecimal]
)

object AccountingPeriodsDetails {
  implicit val format: OFormat[AccountingPeriodsDetails] = Json.format[AccountingPeriodsDetails]
}
