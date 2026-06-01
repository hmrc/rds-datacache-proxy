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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

import java.time.LocalDate

class MgdDetailsSpec extends AnyWordSpec with Matchers {

  "MgdDetails JSON format" should {

    "serialize correctly to JSON" in {

      val model = MgdDetails(
        mgdRegNumber       = "XWM00000001770",
        isBusinessSeasonal = Some(1),
        previousMgdrn1     = Some("XWM00000001774"),
        previousMgdrn2     = Some("XDM00000001309"),
        previousMgdrn3     = None,
        associatedMgdrn1   = Some("XXM00000000723"),
        associatedMgdrn2   = Some("XQM00000001196"),
        associatedMgdrn3   = None,
        systemDate         = Some(LocalDate.parse("2026-05-31"))
      )

      val json = Json.toJson(model)

      (json \ "mgdRegNumber").as[String] mustBe "XWM00000001770"
      (json \ "isBusinessSeasonal").as[Int] mustBe 1
      (json \ "previousMgdrn1").as[String] mustBe "XWM00000001774"
      (json \ "systemDate").as[String] mustBe "2026-05-31"
    }

    "deserialize correctly from JSON" in {

      val json = Json.parse(
        """
          |{
          |  "mgdRegNumber": "XWM00000001770",
          |  "isBusinessSeasonal": 1,
          |  "previousMgdrn1": "XWM00000001774",
          |  "previousMgdrn2": "XDM00000001309",
          |  "previousMgdrn3": null,
          |  "associatedMgdrn1": "XXM00000000723",
          |  "associatedMgdrn2": "XQM00000001196",
          |  "associatedMgdrn3": null,
          |  "systemDate": "2026-05-31"
          |}
          |""".stripMargin
      )

      val model = json.as[MgdDetails]

      model.mgdRegNumber mustBe "XWM00000001770"
      model.isBusinessSeasonal mustBe Some(1)
      model.previousMgdrn2 mustBe Some("XDM00000001309")
      model.systemDate mustBe Some(LocalDate.parse("2026-05-31"))
    }

    "handle empty optional fields correctly" in {

      val model = MgdDetails(
        mgdRegNumber       = "",
        isBusinessSeasonal = None,
        previousMgdrn1     = None,
        previousMgdrn2     = None,
        previousMgdrn3     = None,
        associatedMgdrn1   = None,
        associatedMgdrn2   = None,
        associatedMgdrn3   = None,
        systemDate         = None
      )

      val json = Json.toJson(model)

      (json \ "mgdRegNumber").as[String] mustBe ""
      (json \ "isBusinessSeasonal").toOption mustBe None
      (json \ "systemDate").toOption mustBe None
    }
  }
}
