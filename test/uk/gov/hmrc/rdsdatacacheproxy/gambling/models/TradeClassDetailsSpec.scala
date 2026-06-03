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

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json

import java.time.LocalDate

class TradeClassDetailsSpec extends PlaySpec {

  "TradeClassDetails JSON format" should {

    "serialize correctly when all fields are present" in {

      val model = TradeClassDetails(
        mgdRegNumber         = "XMM00000001025",
        businessTradeClass   = Some(3),
        businessActivityDesc = "Casino",
        systemDate           = Some(LocalDate.of(2026, 6, 3))
      )

      val json = Json.toJson(model)

      (json \ "mgdRegNumber").as[String] mustBe "XMM00000001025"
      (json \ "businessTradeClass").as[Int] mustBe 3
      (json \ "businessActivityDesc").as[String] mustBe "Casino"
      (json \ "systemDate").as[String] mustBe "2026-06-03"
    }

    "serialize correctly when optional fields are None" in {

      val model = TradeClassDetails(
        mgdRegNumber         = "XMM00000001025",
        businessTradeClass   = None,
        businessActivityDesc = "",
        systemDate           = None
      )

      val json = Json.toJson(model)

      (json \ "mgdRegNumber").as[String] mustBe "XMM00000001025"

      (json \ "businessTradeClass").toOption mustBe None
      (json \ "systemDate").toOption mustBe None

      (json \ "businessActivityDesc").as[String] mustBe ""
    }

    "deserialize correctly from JSON with all fields" in {

      val json = Json.parse(
        """
          |{
          |  "mgdRegNumber": "XMM00000001025",
          |  "businessTradeClass": 3,
          |  "businessActivityDesc": "Casino",
          |  "systemDate": "2026-06-03"
          |}
          |""".stripMargin
      )

      val model = json.as[TradeClassDetails]

      model.mgdRegNumber mustBe "XMM00000001025"
      model.businessTradeClass mustBe Some(3)
      model.businessActivityDesc mustBe "Casino"
      model.systemDate mustBe Some(LocalDate.of(2026, 6, 3))
    }

    "deserialize correctly when optional fields are missing" in {

      val json = Json.parse(
        """
          |{
          |  "mgdRegNumber": "XMM00000001025",
          |  "businessActivityDesc": ""
          |}
          |""".stripMargin
      )

      val model = json.as[TradeClassDetails]

      model.mgdRegNumber mustBe "XMM00000001025"
      model.businessTradeClass mustBe None
      model.systemDate mustBe None
      model.businessActivityDesc mustBe ""
    }
  }
}
