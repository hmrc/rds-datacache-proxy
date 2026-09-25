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

case class GroupSummaryDetailsItem(
  contractEndDate: LocalDate,
  groupTaxCharge: Option[BigDecimal],
  groupPayment: Option[BigDecimal],
  groupPaymentRecordCount: Int,
  contractStatus: String,
  contractVersion: Int
)

object GroupSummaryDetailsItem {
  implicit val format: OFormat[GroupSummaryDetailsItem] = Json.format[GroupSummaryDetailsItem]
}

case class GroupReferenceNumberLstItem(
  taxpayerReference: Long
)

object GroupReferenceNumberLstItem {
  implicit val format: OFormat[GroupReferenceNumberLstItem] = Json.format[GroupReferenceNumberLstItem]
}

case class GroupSummaryDetails(
  gpaGrpSummaryDetails: List[GroupSummaryDetailsItem],
  gpaReferenceNumberLst: List[GroupReferenceNumberLstItem],
  nominatedCompanyName: String
)

object GroupSummaryDetails {
  implicit val format: OFormat[GroupSummaryDetails] = Json.format[GroupSummaryDetails]
}
