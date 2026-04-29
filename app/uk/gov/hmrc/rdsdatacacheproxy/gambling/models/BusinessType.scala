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

import play.api.libs.json.*

sealed trait BusinessType {
  def code: Int
}

object BusinessType {

  case object SoleProprietor              extends BusinessType { val code = 1 }
  case object CorporateBody               extends BusinessType { val code = 2 }
  case object UnincorporatedBody          extends BusinessType { val code = 3 }
  case object Partnership                 extends BusinessType { val code = 4 }
  case object LimitedLiabilityPartnership extends BusinessType { val code = 5 }

  val values: List[BusinessType] = List(
    SoleProprietor,
    CorporateBody,
    UnincorporatedBody,
    Partnership,
    LimitedLiabilityPartnership
  )

  def fromCode(code: Int): Option[BusinessType] =
    values.find(_.code == code)

  implicit val reads: Reads[BusinessType] =
    Reads { json =>
      json.validate[Int].flatMap { code =>
        fromCode(code)
          .map(JsSuccess(_))
          .getOrElse(JsError("Invalid business type"))
      }
    }

  implicit val writes: Writes[BusinessType] =
    Writes(bt => JsNumber(bt.code))

  implicit val format: Format[BusinessType] =
    Format(reads, writes)
}
