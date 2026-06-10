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

package uk.gov.hmrc.rdsdatacacheproxy.euvat.models.responses

import play.api.libs.functional.syntax.*
import play.api.libs.json.{Format, __}

import java.time.LocalDateTime

case class TradersKnownFacts(
  vatRegNumber: Int,
  traderName: String,
  addressLine1: String,
  addressLine2: String,
  addressLine3: String,
  addressLine4: String,
  addressLine5: String,
  postCode: String,
  tradeClass: String,
  dateOfRegistration: LocalDateTime,
  dateOfDeregistration: LocalDateTime,
  missingTraderIndicator: String,
  singleMarketIndicator: Int
)

object TradersKnownFacts:
  implicit val format: Format[TradersKnownFacts] =
    (
      (__ \ "vatRegNumber").format[Int] and
        (__ \ "traderName").format[String] and
        (__ \ "addressLine1").format[String] and
        (__ \ "addressLine2").format[String] and
        (__ \ "addressLine3").format[String] and
        (__ \ "addressLine4").format[String] and
        (__ \ "addressLine5").format[String] and
        (__ \ "postCode").format[String] and
        (__ \ "tradeClass").format[String] and
        (__ \ "dateOfRegistration").format[LocalDateTime] and
        (__ \ "dateOfDeregistration").format[LocalDateTime] and
        (__ \ "missingTraderIndicator").format[String] and
        (__ \ "singleMarketIndicator").format[Int]
    )(TradersKnownFacts.apply, o => Tuple.fromProductTyped(o))
