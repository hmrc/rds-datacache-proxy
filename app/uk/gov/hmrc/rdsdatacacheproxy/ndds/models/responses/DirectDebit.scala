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

case class DirectDebit(ddiRefNumber: String,
                       submissionDateTime: LocalDateTime,
                       bankSortCode: String,
                       bankAccountNumber: String,
                       bankAccountName: String,
                       auDdisFlag: Boolean,
                       numberOfPayPlans: Int
                      )

object DirectDebit:
  implicit val format: Format[DirectDebit] =
    (
      (__ \ "ddiRefNumber").format[String] and
        (__ \ "submissionDateTime").format[LocalDateTime] and
        (__ \ "bankSortCode").format[String] and
        (__ \ "bankAccountNumber").format[String] and
        (__ \ "bankAccountName").format[String] and
        (__ \ "auDdisFlag").format[Boolean] and
        (__ \ "numberOfPayPlans").format[Int]
    )(DirectDebit.apply, o => Tuple.fromProductTyped(o))

case class UserDebits(directDebitCount: Int, directDebitList: Seq[DirectDebit])

object UserDebits:
  import DirectDebit.format
  implicit val format: Format[UserDebits] =
    (
      (__ \ "directDebitCount").format[Int] and
        (__ \ "directDebitList").format[Seq[DirectDebit]]
    )(UserDebits.apply, o => Tuple.fromProductTyped(o))

  val empty: UserDebits = UserDebits(0, Seq())
