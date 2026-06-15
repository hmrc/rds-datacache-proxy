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
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseRepaymentInterestDetails

class RepaymentInterestDetailsSpec extends AnyWordSpec with Matchers {

  "RepaymentInterestDetails JSON format" should {

    "serialize to JSON correctly" in {
      val json = Json.toJson(validResponseRepaymentInterestDetails)

      (json \ "periodStartDate").as[String] mustBe "2013-03-01"
      (json \ "periodEndDate").as[String] mustBe "2014-03-11"
      (json \ "total").as[Double] mustBe -2600.0
      (json \ "totalRecords").as[Int] mustBe 3

      (json \ "items").as[Seq[RepaymentInterestDetailItem]].size mustBe 3

      val item1 = (json \ "items")(0)
      (item1 \ "descriptionCode").as[Int] mustBe 2740
      (item1 \ "amount").as[Double] mustBe -800.0
      (item1 \ "interestId").as[String] mustBe "SAFE-CHG-00003"
      (item1 \ "periodStartDate").as[String] mustBe "2014-01-01"
      (item1 \ "periodEndDate").as[String] mustBe "2014-03-31"

      val item2 = (json \ "items")(1)
      (item2 \ "descriptionCode").as[Int] mustBe 2740
      (item2 \ "amount").as[Double] mustBe -400.0
      (item2 \ "interestId").as[String] mustBe "SAFE-CHG-00004"

      val item3 = (json \ "items")(2)
      (item3 \ "descriptionCode").as[Int] mustBe 2740
      (item3 \ "amount").as[Double] mustBe -1400.0
      (item3 \ "interestId").as[String] mustBe "SAFE-CHG-00005"
    }

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        s"""{
           |  "periodStartDate":"2013-03-01",
           |  "periodEndDate":"2014-03-11",
           |  "total":-2600.0,
           |  "totalRecords":3,
           |  "items":[
           |    {
           |      "descriptionCode":2740,
           |      "amount":-800.0,
           |      "interestId":"SAFE-CHG-00003",
           |      "periodStartDate":"2014-01-01",
           |      "periodEndDate":"2014-03-31"
           |    },
           |    {
           |      "descriptionCode":2740,
           |      "amount":-400.0,
           |      "interestId":"SAFE-CHG-00004",
           |      "periodStartDate":"2014-10-01",
           |      "periodEndDate":"2014-12-31"
           |    },
           |    {
           |      "descriptionCode":2740,
           |      "amount":-1400.0,
           |      "interestId":"SAFE-CHG-00005",
           |      "periodStartDate":"2013-04-01",
           |      "periodEndDate":"2013-06-30"
           |    }
           |  ]
           |}""".stripMargin
      )

      val result: JsResult[RepaymentInterestDetails] = json.validate[RepaymentInterestDetails]

      result mustBe JsSuccess(validResponseRepaymentInterestDetails)
    }

    "round-trip write then read should return same object" in {
      val json = Json.toJson(validResponseRepaymentInterestDetails)
      val parsed = json.as[RepaymentInterestDetails]

      parsed mustBe validResponseRepaymentInterestDetails
    }

    "fail to deserialize when required fields are missing" in {
      val json = Json.parse(
        """
          |{
          |  "periodStartDate":"2013-03-01"
          |}
          |""".stripMargin
      )

      val result = json.validate[RepaymentInterestDetails]

      result.isError mustBe true
    }

    "fail to deserialize when field types are incorrect" in {
      val json = Json.parse(
        s"""{
           |  "periodStartDate":"2013-03-01",
           |  "periodEndDate":"2014-03-11",
           |  "total":-2600.0,
           |  "totalRecords":"3",
           |  "items":[
           |    {
           |      "descriptionCode":2740,
           |      "amount":-800.0,
           |      "interestId":"SAFE-CHG-00003",
           |      "periodStartDate":"2014-01-01",
           |      "periodEndDate":"2014-03-31"
           |    }
           |  ]
           |}""".stripMargin
      )

      val result = json.validate[RepaymentInterestDetails]

      result.isError mustBe true
    }
  }
}
