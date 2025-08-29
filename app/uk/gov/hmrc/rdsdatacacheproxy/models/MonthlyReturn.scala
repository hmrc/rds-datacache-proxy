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

package uk.gov.hmrc.rdsdatacacheproxy.models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

case class MonthlyReturn(monthlyReturnId: Long,
                         taxYear: Int,
                         taxMonth: Int,
                         nilReturnIndicator: Option[String],
                         decEmpStatusConsidered: Option[String],
                         decAllSubsVerified: Option[String],
                         decInformationCorrect: Option[String],
                         decNoMoreSubPayments: Option[String],
                         decNilReturnNoPayments: Option[String],
                         status: Option[String],
                         lastUpdate: Option[LocalDateTime],
                         amendment: Option[String],
                         supersededBy: Option[Long])

object MonthlyReturn:
  implicit val format: OFormat[MonthlyReturn] = Json.format[MonthlyReturn]


case class UserMonthlyReturns(monthlyReturnList: Seq[MonthlyReturn])

object UserMonthlyReturns:
  import MonthlyReturn.format
  implicit val format: OFormat[UserMonthlyReturns] = Json.format[UserMonthlyReturns]
  val empty: UserMonthlyReturns = UserMonthlyReturns(Seq())