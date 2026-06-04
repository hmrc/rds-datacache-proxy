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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDate

class OperatorDetailsSpec extends AnyWordSpec with Matchers {

  "OperatorDetails" should {

    "serialize to JSON" in {

      val model = OperatorDetails(
        mgdRegNumber       = "MGD12345",
        solePropName       = Some("John Smith"),
        solePropTitle      = Some("Mr"),
        solePropFirstName  = Some("John"),
        solePropMiddleName = Some("A"),
        solePropLastName   = Some("Smith"),
        tradingName        = Some("ABC Trading"),
        businessName       = Some("ABC Ltd"),
        businessType       = Some(1),
        adi                = Some("ADI123"),
        address1           = Some("1 High Street"),
        address2           = Some("Town Centre"),
        address3           = None,
        address4           = None,
        postcode           = Some("AB1 2CD"),
        country            = Some("UK"),
        abroadSig          = Some("N"),
        agentOwnRef        = Some("REF123"),
        systemDate         = Some(LocalDate.of(2026, 1, 15))
      )

      val json = Json.toJson(model)

      (json \ "mgdRegNumber").as[String] shouldBe "MGD12345"
      (json \ "solePropName").as[String] shouldBe "John Smith"
      (json \ "businessType").as[Int]    shouldBe 1
      (json \ "systemDate").as[String]   shouldBe "2026-01-15"
    }

    "deserialize from JSON" in {

      val json = Json.parse(
        """
          |{
          |  "mgdRegNumber": "MGD12345",
          |  "solePropName": "John Smith",
          |  "solePropTitle": "Mr",
          |  "solePropFirstName": "John",
          |  "solePropMiddleName": "A",
          |  "solePropLastName": "Smith",
          |  "tradingName": "ABC Trading",
          |  "businessName": "ABC Ltd",
          |  "businessType": 1,
          |  "adi": "ADI123",
          |  "address1": "1 High Street",
          |  "address2": "Town Centre",
          |  "postcode": "AB1 2CD",
          |  "country": "UK",
          |  "abroadSig": "N",
          |  "agentOwnRef": "REF123",
          |  "systemDate": "2026-01-15"
          |}
          |""".stripMargin
      )

      val expected = OperatorDetails(
        mgdRegNumber       = "MGD12345",
        solePropName       = Some("John Smith"),
        solePropTitle      = Some("Mr"),
        solePropFirstName  = Some("John"),
        solePropMiddleName = Some("A"),
        solePropLastName   = Some("Smith"),
        tradingName        = Some("ABC Trading"),
        businessName       = Some("ABC Ltd"),
        businessType       = Some(1),
        adi                = Some("ADI123"),
        address1           = Some("1 High Street"),
        address2           = Some("Town Centre"),
        address3           = None,
        address4           = None,
        postcode           = Some("AB1 2CD"),
        country            = Some("UK"),
        abroadSig          = Some("N"),
        agentOwnRef        = Some("REF123"),
        systemDate         = Some(LocalDate.of(2026, 1, 15))
      )

      json.validate[OperatorDetails] shouldBe JsSuccess(expected)
    }

    "handle optional fields when absent" in {

      val json = Json.parse(
        """
          |{
          |  "mgdRegNumber": "MGD12345"
          |}
          |""".stripMargin
      )

      val result = json.as[OperatorDetails]

      result shouldBe OperatorDetails(
        mgdRegNumber       = "MGD12345",
        solePropName       = None,
        solePropTitle      = None,
        solePropFirstName  = None,
        solePropMiddleName = None,
        solePropLastName   = None,
        tradingName        = None,
        businessName       = None,
        businessType       = None,
        adi                = None,
        address1           = None,
        address2           = None,
        address3           = None,
        address4           = None,
        postcode           = None,
        country            = None,
        abroadSig          = None,
        agentOwnRef        = None,
        systemDate         = None
      )
    }
  }
}
