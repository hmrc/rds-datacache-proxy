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
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponsePenalties

class PenaltiesSpec extends AnyWordSpec with Matchers {

  "Penalties JSON format" should {

    "serialize to JSON correctly" in {
      val json = Json.toJson(validResponsePenalties)

      (json \ "periodStartDate").as[String] mustBe "2013-03-01"
      (json \ "periodEndDate").as[String] mustBe "2014-03-11"
      (json \ "total").as[Double] mustBe -24500.0
      (json \ "totalRecords").as[Int] mustBe 3

      (json \ "items").as[Seq[PenaltiesItem]].size mustBe 3

      val item1 = (json \ "items")(0)
      (item1 \ "dateRaised").as[String] mustBe "2014-01-01"
      (item1 \ "descriptionCode").as[Int] mustBe 2680
      (item1 \ "periodStartDate").as[String] mustBe "2014-04-01"
      (item1 \ "periodEndDate").as[String] mustBe "2014-06-30"
      (item1 \ "amount").as[Double] mustBe -9500.0

      val item2 = (json \ "items")(1)
      (item2 \ "dateRaised").as[String] mustBe "2014-01-02"
      (item2 \ "descriptionCode").as[Int] mustBe 2690
      (item2 \ "periodStartDate").as[String] mustBe "2014-01-01"
      (item2 \ "periodEndDate").as[String] mustBe "2014-03-31"
      (item2 \ "amount").as[Double] mustBe -8000.0

      val item3 = (json \ "items")(2)
      (item3 \ "dateRaised").as[String] mustBe "2014-01-03"
      (item3 \ "descriptionCode").as[Int] mustBe 2680
      (item3 \ "periodStartDate").as[String] mustBe "2013-10-01"
      (item3 \ "periodEndDate").as[String] mustBe "2013-12-31"
      (item3 \ "amount").as[Double] mustBe -7000.0
    }

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        s"""{
           |  "periodStartDate":"2013-03-01",
           |  "periodEndDate":"2014-03-11",
           |  "total":-24500.0,
           |  "totalRecords":3,
           |  "items":[
           |    {
           |      "dateRaised":"2014-01-01",
           |      "descriptionCode":2680,
           |      "amount":-9500.0,
           |      "periodStartDate":"2014-04-01",
           |      "periodEndDate":"2014-06-30"
           |    },
           |    {
           |      "dateRaised":"2014-01-02",
           |      "descriptionCode":2690,
           |      "amount":-8000.0,
           |      "periodStartDate":"2014-01-01",
           |      "periodEndDate":"2014-03-31"
           |    },
           |    {
           |      "dateRaised":"2014-01-03",
           |      "descriptionCode":2680,
           |      "amount":-7000.0,
           |      "periodStartDate":"2013-10-01",
           |      "periodEndDate":"2013-12-31"
           |    }
           |  ]
           |}""".stripMargin
      )

      val result: JsResult[Penalties] = json.validate[Penalties]

      result mustBe JsSuccess(validResponsePenalties)
    }

    "round-trip write then read should return same object" in {
      val json = Json.toJson(validResponsePenalties)
      val parsed = json.as[Penalties]

      parsed mustBe validResponsePenalties
    }

    "fail to deserialize when required fields are missing" in {
      val json = Json.parse(
        """
          |{
          |  "periodStartDate":"2013-03-01"
          |}
          |""".stripMargin
      )

      val result = json.validate[Penalties]

      result.isError mustBe true
    }

    "fail to deserialize when field types are incorrect" in {
      val json = Json.parse(
        s"""{
           |  "periodStartDate":"2013-03-01",
           |  "periodEndDate":"2014-03-11",
           |  "total":-24500.0,
           |  "totalRecords":"3",
           |  "items":[
           |    {
           |      "dateRaised":"2014-01-01",
           |      "descriptionCode":2680,
           |      "amount":-9500.0,
           |      "periodStartDate":"2014-04-01",
           |      "periodEndDate":"2014-06-30"
           |    }
           |  ]
           |}""".stripMargin
      )

      val result = json.validate[Penalties]

      result.isError mustBe true
    }
  }
}