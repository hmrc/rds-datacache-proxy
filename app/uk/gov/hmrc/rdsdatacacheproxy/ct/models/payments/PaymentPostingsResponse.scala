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

package uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class PaymentPostingsResponse(paymentPostings: List[PaymentPostingRow])

object PaymentPostingsResponse {
  implicit val format: OFormat[PaymentPostingsResponse] = Json.format[PaymentPostingsResponse]
}

case class PaymentPostingRow(
  accountingPeriod: Long,
  settlementSequenceNo: Long,
  payslipId: Long,
  postingAmount: BigDecimal,
  effectiveDateOfPayment: LocalDate,
  paymentType: String
)

object PaymentPostingRow {
  implicit val format: OFormat[PaymentPostingRow] = Json.format[PaymentPostingRow]
}
