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

case class PaymentSearchResultResponse(paymentSearchResult: List[PaymentSearchResult])

object PaymentSearchResultResponse {
  implicit val format: OFormat[PaymentSearchResultResponse] = Json.format[PaymentSearchResultResponse]
}

case class PaymentSearchResult(accountingPeriod: Option[BigDecimal],
                               payslipId: Option[BigDecimal],
                               amount: Option[BigDecimal],
                               effectiveDateOfPayment: Option[LocalDate],
                               paymentType: Option[String],
                               bankSortCode: Option[BigDecimal],
                               bankAccountNumber: Option[BigDecimal],
                               chequeNumber: Option[BigDecimal],
                               paymentAmount: Option[BigDecimal],
                               chequeId: Option[BigDecimal]
                              )

object PaymentSearchResult {
  implicit val format: OFormat[PaymentSearchResult] = Json.format[PaymentSearchResult]
}
