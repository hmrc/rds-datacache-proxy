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
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseInterestBreakdownSummary

class InterestBreakdownSummarySpec extends AnyWordSpec with Matchers {

  "InterestBreakdownSummary JSON format" should {

    "serialize to JSON correctly" in {
      val json = Json.toJson(validResponseInterestBreakdownSummary)

      (json \ "periodStartDate").as[String] mustBe "2013-03-01"
      (json \ "periodEndDate").as[String] mustBe "2014-03-11"
      (json \ "interestAmount").as[Double] mustBe -81.84
      (json \ "interestAccruingAmount").as[Double] mustBe -25.76
      (json \ "repaymentInterestAmount").as[Double] mustBe 41.23
      (json \ "total").as[Double] mustBe 66.37
    }

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        s"""{
           |  "periodStartDate":"2013-03-01",
           |  "periodEndDate":"2014-03-11",
           |  "interestAmount":-81.84,
           |  "interestAccruingAmount":-25.76,
           |  "repaymentInterestAmount":41.23,
           |  "total":66.37
           |}""".stripMargin
      )

      val result: JsResult[InterestBreakdownSummary] = json.validate[InterestBreakdownSummary]

      result mustBe JsSuccess(validResponseInterestBreakdownSummary)
    }

    "round-trip write then read should return same object" in {
      val json = Json.toJson(validResponseInterestBreakdownSummary)
      val parsed = json.as[InterestBreakdownSummary]

      parsed mustBe validResponseInterestBreakdownSummary
    }

    "fail to deserialize when required fields are missing" in {
      val json = Json.parse(
        """
          |{
          |  "periodStartDate":"2013-03-01"
          |}
          |""".stripMargin
      )

      val result = json.validate[InterestBreakdownSummary]

      result.isError mustBe true
    }

    "fail to deserialize when field types are incorrect" in {
      val json = Json.parse(
        s"""{
           |  "periodStartDate":"2013-03-01",
           |  "periodEndDate":"2014-03-11",
           |  "interestAmount":"£81.84",
           |  "interestAccruingAmount":-25.76,
           |  "repaymentInterestAmount":41.23,
           |  "total":66.37
           |}""".stripMargin
      )

      val result = json.validate[InterestBreakdownSummary]

      result.isError mustBe true
    }
  }
}
