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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.ReallocationsOut.Reallocation

import java.time.LocalDate

case class ReallocationsOut(
  gtrPeriodStartDate: Option[LocalDate],
  gtrPeriodEndDate: Option[LocalDate],
  total: BigDecimal,
  totalRecords: Int,
  reallocationsOut: Seq[Reallocation]
)

object ReallocationsOut {
  implicit val format: OFormat[ReallocationsOut] = Json.format[ReallocationsOut]

  case class Reallocation(dateProcessed: LocalDate, amount: BigDecimal)

  object Reallocation {
    implicit val format: OFormat[Reallocation] = Json.format[Reallocation]
  }

  val empty = ReallocationsOut(None, None, BigDecimal(0), 0, Seq.empty)
}
