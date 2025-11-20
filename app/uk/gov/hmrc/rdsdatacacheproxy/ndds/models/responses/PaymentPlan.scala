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

import java.time.LocalDateTime

case class PaymentPlan(scheduledPaymentAmount: Option[BigDecimal],
                       planRefNumber: String,
                       planType: String,
                       paymentReference: String,
                       hodService: String,
                       submissionDateTime: LocalDateTime
                      )

object PaymentPlan:
  implicit val format: Format[PaymentPlan] = (
    (__ \ "scheduledPaymentAmount").formatNullable[BigDecimal] and
      (__ \ "planRefNumber").format[String] and
      (__ \ "planType").format[String] and
      (__ \ "paymentReference").format[String] and
      (__ \ "hodService").format[String] and
      (__ \ "submissionDateTime").format[LocalDateTime]
  )(PaymentPlan.apply, o => Tuple.fromProductTyped(o))

case class DDPaymentPlans(bankSortCode: String,
                          bankAccountNumber: String,
                          bankAccountName: String,
                          auDdisFlag: String,
                          paymentPlanCount: Int,
                          paymentPlanList: Seq[PaymentPlan]
                         )

object DDPaymentPlans:
  import PaymentPlan.format
  implicit val format: Format[DDPaymentPlans] = (
    (__ \ "bankSortCode").format[String] and
      (__ \ "bankAccountNumber").format[String] and
      (__ \ "bankAccountName").format[String] and
      (__ \ "auDdisFlag").format[String] and
      (__ \ "paymentPlanCount").format[Int] and
      (__ \ "paymentPlanList").format[Seq[PaymentPlan]]
  )(DDPaymentPlans.apply, o => Tuple.fromProductTyped(o))

  val empty: DDPaymentPlans = DDPaymentPlans("", "", "", "dd", 0, Seq())
