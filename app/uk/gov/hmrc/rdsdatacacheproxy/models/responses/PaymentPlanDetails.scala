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

package uk.gov.hmrc.rdsdatacacheproxy.models.responses

import play.api.libs.json.{Json, OFormat}

import java.time.{LocalDate, LocalDateTime}

case class DirectDebitDetail(bankSortCode: String,
                             bankAccountNumber: String,
                             bankAccountName: String,
                             auDdisFlag: String,
                             submissionDateTime: LocalDateTime)

object DirectDebitDetail:
  implicit val format: OFormat[DirectDebitDetail] = Json.format[DirectDebitDetail]

case class PaymentPlanDetail(hodService: String,
                             planType: String,
                             paymentReference: String,
                             submissionDateTime: LocalDateTime,
                             scheduledPaymentAmount: Double,
                             scheduledPaymentStartDate: LocalDate,
                             initialPaymentStartDate: LocalDate,
                             initialPaymentAmount: Double,
                             scheduledPaymentEndDate: LocalDate,
                             scheduledPaymentFrequency: String,
                             suspensionStartDate: LocalDate,
                             suspensionEndDate: LocalDate,
                             balancingPaymentAmount: Double,
                             balancingPaymentDate: LocalDate,
                             totalLiability: Double,
                             paymentPlanEditable: Boolean)

object PaymentPlanDetail:
  implicit val format: OFormat[PaymentPlanDetail] = Json.format[PaymentPlanDetail]

case class PaymentPlanDetails(directDebitDetails: DirectDebitDetail,
                              paymentPlanDetails: PaymentPlanDetail)

object PaymentPlanDetails:
  implicit val format: OFormat[PaymentPlanDetails] = Json.format[PaymentPlanDetails]