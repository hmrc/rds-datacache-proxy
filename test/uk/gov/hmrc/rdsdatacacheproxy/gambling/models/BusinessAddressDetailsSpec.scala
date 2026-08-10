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

class BusinessAddressDetailsSpec extends AnyFreeSpec with Matchers {

  "BusinessAddressDetails JSON format" - {

    "serialises to JSON correctly" in {

      val model = BusinessAddressDetails(
        mgdRegNumber      = "XWM00000001770",
        adi               = Some("none"),
        address1          = Some("random street"),
        address2          = Some("bar"),
        address3          = Some("bar"),
        address4          = Some("bar"),
        postcode          = Some("SR1 4DE"),
        country           = Some("Ingerland!"),
        iomOrCiFlag       = Some("true"),
        systemDate        = Some(LocalDate.of(2026, 5, 13))
      )

      val json: JsValue = Json.toJson(model)

      (json \ "mgdRegNumber").as[String] mustBe "XWM00000001770"
      (json \ "adi").as[String] mustBe "none"
      (json \ "address1").as[String] mustBe "random street"
      (json \ "address2").as[String] mustBe "bar"
      (json \ "address3").as[String] mustBe "bar"
      (json \ "address4").as[String] mustBe "bar"
      (json \ "postcode").as[String] mustBe "SR1 4DE"
      (json \ "country").as[String] mustBe "Ingerland!"
      (json \ "iomOrCiFlag").as[String] mustBe "true"
      (json \ "systemDate").as[String] mustBe "2026-05-13"
    }

    "deserialises from JSON correctly" in {

      val json = Json.parse(
        """
          {
            "mgdRegNumber": "XWM00000001770",
            "adi": "none",
            "address1": "random street",
            "address2": "bar",
            "address3": "bar",
            "address4": "bar",
            "postcode": "SR1 4DE",
            "country": "Ingerland!",
            "iomOrCiFlag": "true",
            "systemDate": "2026-05-13"


          }
        """
      )

      val result = json.as[BusinessAddressDetails]

      result mustBe BusinessAddressDetails(
        mgdRegNumber      = "XWM00000001770",
        adi               = Some("none"),
        address1          = Some("random street"),
        address2          = Some("bar"),
        address3          = Some("bar"),
        address4          = Some("bar"),
        postcode          = Some("SR1 4DE"),
        country           = Some("Ingerland!"),
        iomOrCiFlag       = Some("true"),
        systemDate        = Some(LocalDate.of(2026, 5, 13))
      )
    }

    "supports round-trip conversion" in {

      val model = BusinessAddressDetails(
        mgdRegNumber      = "XWM00000001770",
        adi               = Some("none"),
        address1          = Some("random street"),
        address2          = Some("bar"),
        address3          = Some("bar"),
        address4          = Some("bar"),
        postcode          = Some("SR1 4DE"),
        country           = Some("Ingerland!"),
        iomOrCiFlag       = Some("true"),
        systemDate        = Some(LocalDate.of(2026, 5, 13))
      )

      val json = Json.toJson(model)
      val back = json.as[BusinessAddressDetails]

      back mustBe model
    }
  }
}
