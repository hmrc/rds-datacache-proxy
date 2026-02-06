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

package uk.gov.hmrc.rdsdatacacheproxy.ndds.models.requests

import play.api.libs.functional.syntax.*
import play.api.libs.json.{Format, __}

import java.time.LocalDate

case class PaymentPlanDuplicateCheckRequest(
  directDebitReference: String,
  paymentPlanReference: String,
  planType: String,
  paymentService: String,
  paymentReference: String,
  paymentAmount: Option[BigDecimal],
  totalLiability: Option[BigDecimal],
  paymentFrequency: Option[Int],
  paymentStartDate: Option[LocalDate]
)

object PaymentPlanDuplicateCheckRequest {
  implicit val format: Format[PaymentPlanDuplicateCheckRequest] = (
    (__ \ "directDebitReference").format[String] and
      (__ \ "paymentPlanReference").format[String] and
      (__ \ "planType").format[String] and
      (__ \ "paymentService").format[String] and
      (__ \ "paymentReference").format[String] and
      (__ \ "paymentAmount").formatNullable[BigDecimal] and
      (__ \ "totalLiability").formatNullable[BigDecimal] and
      (__ \ "paymentFrequency").formatNullable[Int] and
      (__ \ "paymentStartDate").formatNullable[LocalDate]
  )(PaymentPlanDuplicateCheckRequest.apply, o => Tuple.fromProductTyped(o))
}
