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
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseInterestAccruingDetails

import java.time.LocalDate

class InterestAccruingDetailsSpec extends AnyWordSpec with Matchers {

  "InterestAccruingDetails JSON format" should {

    "serialize to JSON correctly" in {
      val json = Json.toJson(validResponseInterestAccruingDetails)

      (json \ "periodStartDate").as[String] mustBe "2013-01-01"
      (json \ "periodEndDate").as[String] mustBe "2014-11-03"
      (json \ "total").as[Double] mustBe 7500.0
      (json \ "totalRecords").as[Int] mustBe 3

      (json \ "items").as[Seq[InterestAccruingDetailsItem]].size mustBe 3

      val item1 = (json \ "items")(0)
      (item1 \ "descriptionCode").as[BigDecimal] mustBe BigDecimal(1)
      (item1 \ "amount").as[BigDecimal] mustBe BigDecimal(3000.00)
      (item1 \ "interestId").as[String] mustBe "SAFE-CHG-00001"
      (item1 \ "periodStartDate").as[String] mustBe "2014-10-01"
      (item1 \ "periodEndDate").as[String] mustBe "2014-10-31"

      val item2 = (json \ "items")(1)
      (item2 \ "descriptionCode").as[BigDecimal] mustBe BigDecimal(2)
      (item2 \ "amount").as[BigDecimal] mustBe BigDecimal(5000.00)
      (item2 \ "interestId").as[String] mustBe "SAFE-CHG-00002"
      (item2 \ "periodStartDate").as[String] mustBe "2014-07-15"
      (item2 \ "periodEndDate").as[String] mustBe "2014-07-31"

      val item3 = (json \ "items")(2)
      (item3 \ "descriptionCode").as[BigDecimal] mustBe BigDecimal(3)
      (item3 \ "amount").as[BigDecimal] mustBe BigDecimal(-500.00)
      (item3 \ "interestId").as[String] mustBe "SAFE-CHG-00003"
      (item3 \ "periodStartDate").as[String] mustBe "2013-06-01"
      (item3 \ "periodEndDate").as[String] mustBe "2013-06-30"
    }

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        s"""{
           "periodStartDate":"${LocalDate.of(2013, 1, 1)}",
           "periodEndDate":"${LocalDate.of(2014, 11, 3)}",
           "total":7500.0,
           "totalRecords":3,
           "items":[
             {
                "descriptionCode":1,
                "amount":3000.0,
                "interestId":"SAFE-CHG-00001",
                "periodStartDate":"${LocalDate.of(2014, 10, 1)}",
                "periodEndDate":"${LocalDate.of(2014, 10, 31)}"
              },
              {
                "descriptionCode":2,
                "amount":5000.0,
                "interestId":"SAFE-CHG-00002",
                "periodStartDate":"${LocalDate.of(2014, 7, 15)}",
                "periodEndDate":"${LocalDate.of(2014, 7, 31)}"
              },
              {
                "descriptionCode":3,
                "amount":-500.0,
                "interestId":"SAFE-CHG-00003",
                "periodStartDate":"${LocalDate.of(2013, 6, 1)}",
                "periodEndDate":"${LocalDate.of(2013, 6, 30)}"
              }
           ]
           }""".stripMargin
      )

      val result: JsResult[InterestAccruingDetails] = json.validate[InterestAccruingDetails]

      result mustBe JsSuccess(validResponseInterestAccruingDetails)
    }

    "round-trip (write then read) should return same object" in {
      val original = validResponseInterestAccruingDetails

      val json = Json.toJson(original)
      val parsed = json.as[InterestAccruingDetails]

      parsed mustBe original
    }

    "fail to deserialize when fields are missing" in {
      val json = Json.parse(
        """
          |{
          |  "periodStartDate":"2013-01-01"
          |}
          |""".stripMargin
      )

      val result = json.validate[InterestAccruingDetails]

      result.isError mustBe true
    }

    "fail to deserialize when types are incorrect" in {
      val json = Json.parse(
        s"""{
           "periodStartDate":"${LocalDate.of(2013, 1, 1)}",
           "periodEndDate":"${LocalDate.of(2014, 11, 3)}",
           "total":7500.0,
           "totalRecords":"3",
           "items":[
             {
                "descriptionCode":1,
                "amount":3000.0,
                "interestId":"SAFE-CHG-00001",
                "periodStartDate":"${LocalDate.of(2014, 10, 1)}",
                "periodEndDate":"${LocalDate.of(2014, 10, 31)}"
              }
           ]
           }""".stripMargin
      )

      val result = json.validate[InterestAccruingDetails]

      result.isError mustBe true
    }
  }
}
