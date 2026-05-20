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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

final case class AssessmentsInAbsenceItem(
  periodStartDate: Option[LocalDate],
  periodEndDate: Option[LocalDate],
  dateRaised: Option[LocalDate],
  amount: Option[BigDecimal]
)

object AssessmentsInAbsenceItem {
  implicit val format: OFormat[AssessmentsInAbsenceItem] = Json.format[AssessmentsInAbsenceItem]
}

final case class AssessmentsInAbsence(
  periodStartDate: Option[LocalDate],
  periodEndDate: Option[LocalDate],
  total: Option[BigDecimal],
  totalRecords: Option[Int],
  items: Seq[AssessmentsInAbsenceItem]
)

object AssessmentsInAbsence {
  implicit val format: OFormat[AssessmentsInAbsence] = Json.format[AssessmentsInAbsence]
}
