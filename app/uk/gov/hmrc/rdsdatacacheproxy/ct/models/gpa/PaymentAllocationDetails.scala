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

package uk.gov.hmrc.rdsdatacacheproxy.ct.models.gpa

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class PaymentAllocationDetails(
  gppEndDate: LocalDate,
  gppTotalGroupPayment: Option[BigDecimal],
  gppTotalGroupTax: Option[BigDecimal],
  gppStatus: String,
  gppApportionmentMethod: Option[String],
  participatingCompanyDesc: String,
  participatorAccPeriodEnd: LocalDate,
  participatorTaxCharge: BigDecimal,
  participatorAllocPayments: BigDecimal,
  gpaUtr: BigDecimal,
  gppContractVersionOut: BigDecimal,
  allocationDetails: List[AllocationDetails],
  totalNumOfRecords: BigDecimal
)

object PaymentAllocationDetails {
  implicit val format: OFormat[PaymentAllocationDetails] = Json.format[PaymentAllocationDetails]
}

case class AllocationDetails(
  effectivePaymentDate: LocalDate,
  paymentAmount: BigDecimal
)

object AllocationDetails {
  implicit val format: OFormat[AllocationDetails] = Json.format[AllocationDetails]
}
