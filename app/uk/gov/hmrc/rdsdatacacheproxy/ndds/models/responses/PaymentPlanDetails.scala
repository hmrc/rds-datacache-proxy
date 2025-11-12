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

package uk.gov.hmrc.rdsdatacacheproxy.ndds.models.responses

import play.api.libs.functional.syntax.*
import play.api.libs.json.{Format, __}

import java.time.{LocalDate, LocalDateTime}

case class DirectDebitDetail(bankSortCode: Option[String],
                             bankAccountNumber: Option[String],
                             bankAccountName: Option[String],
                             auDdisFlag: Boolean,
                             submissionDateTime: LocalDateTime
                            )

object DirectDebitDetail:
  implicit val format: Format[DirectDebitDetail] = (
    (__ \ "bankSortCode").formatNullable[String] and
      (__ \ "bankAccountNumber").formatNullable[String] and
      (__ \ "bankAccountName").formatNullable[String] and
      (__ \ "auDdisFlag").format[Boolean] and
      (__ \ "submissionDateTime").format[LocalDateTime]
  )(DirectDebitDetail.apply, o => Tuple.fromProductTyped(o))

case class PaymentPlanDetail(hodService: String,
                             planType: String,
                             paymentReference: String,
                             submissionDateTime: LocalDateTime,
                             scheduledPaymentAmount: Option[BigDecimal],
                             scheduledPaymentStartDate: Option[LocalDate],
                             initialPaymentStartDate: Option[LocalDate],
                             initialPaymentAmount: Option[BigDecimal],
                             scheduledPaymentEndDate: Option[LocalDate],
                             scheduledPaymentFrequency: Option[Int],
                             suspensionStartDate: Option[LocalDate],
                             suspensionEndDate: Option[LocalDate],
                             balancingPaymentAmount: Option[BigDecimal],
                             balancingPaymentDate: Option[LocalDate],
                             totalLiability: Option[BigDecimal],
                             paymentPlanEditable: Boolean
                            )

object PaymentPlanDetail:
  implicit val format: Format[PaymentPlanDetail] = (
    (__ \ "hodService").format[String] and
      (__ \ "planType").format[String] and
      (__ \ "paymentReference").format[String] and
      (__ \ "submissionDateTime").format[LocalDateTime] and
      (__ \ "scheduledPaymentAmount").formatNullable[BigDecimal] and
      (__ \ "scheduledPaymentStartDate").formatNullable[LocalDate] and
      (__ \ "initialPaymentStartDate").formatNullable[LocalDate] and
      (__ \ "initialPaymentAmount").formatNullable[BigDecimal] and
      (__ \ "scheduledPaymentEndDate").formatNullable[LocalDate] and
      (__ \ "scheduledPaymentFrequency").formatNullable[Int] and
      (__ \ "suspensionStartDate").formatNullable[LocalDate] and
      (__ \ "suspensionEndDate").formatNullable[LocalDate] and
      (__ \ "balancingPaymentAmount").formatNullable[BigDecimal] and
      (__ \ "balancingPaymentDate").formatNullable[LocalDate] and
      (__ \ "totalLiability").formatNullable[BigDecimal] and
      (__ \ "paymentPlanEditable").format[Boolean]
  )(PaymentPlanDetail.apply, o => Tuple.fromProductTyped(o))

case class PaymentPlanDetails(directDebitDetails: DirectDebitDetail, paymentPlanDetails: PaymentPlanDetail)

object PaymentPlanDetails:

  implicit val format: Format[PaymentPlanDetails] = (
    (__ \ "directDebitDetails").format[DirectDebitDetail] and
      (__ \ "paymentPlanDetails").format[PaymentPlanDetail]
  )(PaymentPlanDetails.apply, o => Tuple.fromProductTyped(o))
