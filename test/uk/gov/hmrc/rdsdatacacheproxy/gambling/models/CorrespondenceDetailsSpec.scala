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

class CorrespondenceDetailsSpec extends AnyFreeSpec with Matchers {

  "CorrespondenceDetails JSON format" - {

    "serialises to JSON correctly" in {

      val model = CorrespondenceDetails(
        mgdRegNumber = "XWM00000001770",
        nameLine1 = Some("foo"),
        nameLine2 = Some("foo"),
        phoneNumber = Some("07618728019"),
        mobilePhoneNumber = Some("018937617281"),
        faxNumber = Some("foo"),
        emailAddr = Some("foo@mail.com"),
        adi = Some("none"),
        address1 = Some("random street"),
        address2 = Some("bar"),
        address3 = Some("bar"),
        address4 = Some("bar"),
        postcode = Some("SR1 4DE"),
        country = Some("Ingerland!"),
        iomOrCiFlag = Some("true"),
        systemDate = Some(LocalDate.of(2026, 5, 13))
      )

      val json: JsValue = Json.toJson(model)

      (json \ "mgdRegNumber").as[String] mustBe "XWM00000001770"
      (json \ "nameLine1").as[String] mustBe "foo"
      (json \ "nameLine2").as[String] mustBe "foo"
      (json \ "phoneNumber").as[String] mustBe "07618728019"
      (json \ "mobilePhoneNumber").as[String] mustBe "018937617281"
      (json \ "faxNumber").as[String] mustBe "foo"
      (json \ "emailAddr").as[String] mustBe "foo@mail.com"
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
            "nameLine1": "foo",
            "nameLine2": "foo",
            "phoneNumber": "07618728019",
            "mobilePhoneNumber": "018937617281",
            "faxNumber": "foo",
            "emailAddr": "foo@mail.com",
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

      val result = json.as[CorrespondenceDetails]

      result mustBe CorrespondenceDetails(
        mgdRegNumber = "XWM00000001770",
        nameLine1 = Some("foo"),
        nameLine2 = Some("foo"),
        phoneNumber = Some("07618728019"),
        mobilePhoneNumber = Some("018937617281"),
        faxNumber = Some("foo"),
        emailAddr = Some("foo@mail.com"),
        adi = Some("none"),
        address1 = Some("random street"),
        address2 = Some("bar"),
        address3 = Some("bar"),
        address4 = Some("bar"),
        postcode = Some("SR1 4DE"),
        country = Some("Ingerland!"),
        iomOrCiFlag = Some("true"),
        systemDate = Some(LocalDate.of(2026, 5, 13))
      )
    }

    "supports round-trip conversion" in {

      val model = CorrespondenceDetails(
        mgdRegNumber = "XWM00000001770",
        nameLine1 = Some("foo"),
        nameLine2 = Some("foo"),
        phoneNumber = Some("07618728019"),
        mobilePhoneNumber = Some("018937617281"),
        faxNumber = Some("foo"),
        emailAddr = Some("foo@mail.com"),
        adi = Some("none"),
        address1 = Some("random street"),
        address2 = Some("bar"),
        address3 = Some("bar"),
        address4 = Some("bar"),
        postcode = Some("SR1 4DE"),
        country = Some("Ingerland!"),
        iomOrCiFlag = Some("true"),
        systemDate = Some(LocalDate.of(2026, 5, 13))
      )

      val json = Json.toJson(model)
      val back = json.as[CorrespondenceDetails]

      back mustBe model
    }
  }
}
