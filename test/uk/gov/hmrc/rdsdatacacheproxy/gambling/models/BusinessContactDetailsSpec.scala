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

class BusinessContactDetailsSpec extends AnyFreeSpec with Matchers {

  "BusinessContactDetails JSON format" - {

    "serialises to JSON correctly" in {

      val model = BusinessContactDetails(
        mgdRegNumber      = "XWM00000001770",
        phoneNumber       = Some("0555 666111"),
        mobilePhoneNumber = Some("0555 666112"),
        faxNumber         = Some("0555 666113"),
        emailAddr         = Some("aaaaa@bbbb.com"),
        systemDate        = Some(LocalDate.of(2026, 5, 13))
      )

      val json: JsValue = Json.toJson(model)

      (json \ "mgdRegNumber").as[String] mustBe "XWM00000001770"
      (json \ "phoneNumber").as[String] mustBe "0555 666111"
      (json \ "mobilePhoneNumber").as[String] mustBe "0555 666112"
      (json \ "faxNumber").as[String] mustBe "0555 666113"
      (json \ "emailAddr").as[String] mustBe "aaaaa@bbbb.com"
      (json \ "systemDate").as[String] mustBe "2026-05-13"
    }

    "deserialises from JSON correctly" in {

      val json = Json.parse(
        """
          {
            "mgdRegNumber": "XWM00000001770",
            "phoneNumber": "0555 666111",
            "mobilePhoneNumber": "0555 666112",
            "faxNumber": "0555 666113",
            "emailAddr": "aaaaa@bbbb.com",
            "systemDate": "2026-05-13"
          }
        """
      )

      val result = json.as[BusinessContactDetails]

      result mustBe BusinessContactDetails(
        mgdRegNumber      = "XWM00000001770",
        phoneNumber       = Some("0555 666111"),
        mobilePhoneNumber = Some("0555 666112"),
        faxNumber         = Some("0555 666113"),
        emailAddr         = Some("aaaaa@bbbb.com"),
        systemDate        = Some(LocalDate.of(2026, 5, 13))
      )
    }

    "supports round-trip conversion" in {

      val model = BusinessContactDetails(
        mgdRegNumber      = "XWM00000001770",
        phoneNumber       = Some("0555 666111"),
        mobilePhoneNumber = Some("0555 666112"),
        faxNumber         = Some("0555 666113"),
        emailAddr         = Some("aaaaa@bbbb.com"),
        systemDate        = Some(LocalDate.of(2026, 5, 13))
      )

      val json = Json.toJson(model)
      val back = json.as[BusinessContactDetails]

      back mustBe model
    }
  }
}
