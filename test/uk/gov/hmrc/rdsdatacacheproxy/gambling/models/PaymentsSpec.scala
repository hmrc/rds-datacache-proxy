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
import play.api.libs.json.{JsResult, JsSuccess, Json}
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponsePayments

class PaymentsSpec extends AnyWordSpec with Matchers {

  "Payments JSON format" should {

    "serialize to JSON correctly" in {
      val json = Json.toJson(validResponsePayments)

      (json \ "periodStartDate").as[String] mustBe "2013-01-01"
      (json \ "periodEndDate").as[String] mustBe "2014-11-03"
      (json \ "total").as[Double] mustBe 7500.0
      (json \ "totalRecords").as[Int] mustBe 3

      (json \ "items").as[Seq[PaymentItem]].size mustBe 3

      val item1 = (json \ "items")(0)
      (item1 \ "transactionDate").as[String] mustBe "2014-10-01"
      (item1 \ "descriptionCode").as[String] mustBe "E"
      (item1 \ "amount").as[Double] mustBe 3000.0

      val item2 = (json \ "items")(1)
      (item2 \ "transactionDate").as[String] mustBe "2014-07-15"
      (item2 \ "descriptionCode").as[String] mustBe "E"
      (item2 \ "amount").as[Double] mustBe 5000.0

      val item3 = (json \ "items")(2)
      (item3 \ "transactionDate").as[String] mustBe "2013-06-01"
      (item3 \ "descriptionCode").as[String] mustBe "C"
      (item3 \ "amount").as[Double] mustBe -500.0
    }

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        s"""{
           |  "periodStartDate":"2013-01-01",
           |  "periodEndDate":"2014-11-03",
           |  "total":7500.0,
           |  "totalRecords":3,
           |  "items":[
           |    {
           |      "transactionDate":"2014-10-01",
           |      "descriptionCode":"E",
           |      "amount":3000.0
           |    },
           |    {
           |      "transactionDate":"2014-07-15",
           |      "descriptionCode":"E",
           |      "amount":5000.0
           |    },
           |    {
           |      "transactionDate":"2013-06-01",
           |      "descriptionCode":"C",
           |      "amount":-500.0
           |    }
           |  ]
           |}""".stripMargin
      )

      val result: JsResult[Payments] = json.validate[Payments]

      result mustBe JsSuccess(validResponsePayments)
    }

    "round-trip write then read should return same object" in {
      val json = Json.toJson(validResponsePayments)
      val parsed = json.as[Payments]

      parsed mustBe validResponsePayments
    }

    "fail to deserialize when required fields are missing" in {
      val json = Json.parse(
        """
          |{
          |  "periodStartDate":"2013-01-01"
          |}
          |""".stripMargin
      )

      val result = json.validate[Payments]

      result.isError mustBe true
    }

    "fail to deserialize when field types are incorrect" in {
      val json = Json.parse(
        s"""{
           |  "periodStartDate":"2013-01-01",
           |  "periodEndDate":"2014-11-03",
           |  "total":7500.0,
           |  "totalRecords":"3",
           |  "items":[
           |    {
           |      "transactionDate":"2014-10-01",
           |      "descriptionCode":"E",
           |      "amount":3000.0
           |    }
           |  ]
           |}""".stripMargin
      )

      val result = json.validate[Payments]

      result.isError mustBe true
    }
  }
}
