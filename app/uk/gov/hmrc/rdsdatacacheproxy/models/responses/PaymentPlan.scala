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

import java.time.LocalDateTime

case class PaymentPlan(scheduledPayAmount: Double,
                       planType: String,
                       payReference: String,
                       planHoldService: String,
                       submissionDateTime: LocalDateTime)

object PaymentPlan:
  implicit val format: OFormat[PaymentPlan] = Json.format[PaymentPlan]

case class DDPaymentPlans(bankSortCode: String,
                          bankAccountNumber: String,
                          bankAccountName: String,
                          paymentPlanCount: Int,
                          paymentPlanList: Seq[PaymentPlan])

object DDPaymentPlans:
  import PaymentPlan.format
  implicit val format: OFormat[DDPaymentPlans] = Json.format[DDPaymentPlans]
  val empty: DDPaymentPlans = DDPaymentPlans("", "", "", 0, Seq())
