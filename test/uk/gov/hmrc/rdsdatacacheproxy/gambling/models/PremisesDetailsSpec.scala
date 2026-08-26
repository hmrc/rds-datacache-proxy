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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsValue, Json}

import java.time.LocalDate

class PremisesDetailsSpec extends AnyFreeSpec with Matchers {

  " PremisesDetails JSON format" - {

    "serialises to JSON correctly" in {

      val model = PremisesDetails(
        mgdRegNumber = "XRM00000000574",
        address1     = Some("address1"),
        address2     = Some("address2"),
        address3     = Some("address3"),
        address4     = Some("address4"),
        postcode     = Some("L1 8YL"),
        systemDate   = Some(LocalDate.of(2026, 5, 13))
      )

      val json: JsValue = Json.toJson(model)

      (json \ "mgdRegNumber").as[String] mustBe "XRM00000000574"
      (json \ "address1").as[String] mustBe "address1"
      (json \ "address2").as[String] mustBe "address2"
      (json \ "address3").as[String] mustBe "address3"
      (json \ "address4").as[String] mustBe "address4"
      (json \ "postcode").as[String] mustBe "L1 8YL"
      (json \ "systemDate").as[String] mustBe "2026-05-13"
    }

    "deserialises from JSON correctly" in {

      val json = Json.parse(
        """
          {
            "mgdRegNumber": "XRM00000000574",
            "address1": "address1",
            "address2": "address2",
            "address3": "address3",
            "address4": "address4",
            "postcode": "L1 8YL",
            "systemDate": "2026-05-13"


          }
        """
      )

      val result = json.as[PremisesDetails]

      result mustBe PremisesDetails(
        mgdRegNumber = "XRM00000000574",
        address1     = Some("address1"),
        address2     = Some("address2"),
        address3     = Some("address3"),
        address4     = Some("address4"),
        postcode     = Some("L1 8YL"),
        systemDate   = Some(LocalDate.of(2026, 5, 13))
      )
    }

    "supports round-trip conversion" in {

      val model = PremisesDetails(
        mgdRegNumber = "XRM00000000574",
        address1     = Some("address1"),
        address2     = Some("address2"),
        address3     = Some("address3"),
        address4     = Some("address4"),
        postcode     = Some("L1 8YL"),
        systemDate   = Some(LocalDate.of(2026, 5, 13))
      )

      val json = Json.toJson(model)
      val back = json.as[PremisesDetails]

      back mustBe model
    }
  }
}
