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
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseRepaymentsSummary

class RepaymentsSummarySpec extends AnyWordSpec with Matchers {

  "RepaymentsSummary JSON format" should {

    "serialize to JSON correctly" in {
      val json = Json.toJson(validResponseRepaymentsSummary)

      (json \ "periodStartDate").as[String] mustBe "2013-03-01"
      (json \ "periodEndDate").as[String] mustBe "2014-03-11"
      (json \ "actualRepaymentsAmount").as[Double] mustBe 71.84
      (json \ "repaymentsInterestRepaidAmount").as[Double] mustBe -35.76
      (json \ "total").as[Double] mustBe 36.08
    }

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        s"""{
           |  "periodStartDate":"2013-03-01",
           |  "periodEndDate":"2014-03-11",
           |  "actualRepaymentsAmount":71.84,
           |  "repaymentsInterestRepaidAmount":-35.76,
           |  "total":36.08
           |}""".stripMargin
      )

      val result: JsResult[RepaymentsSummary] = json.validate[RepaymentsSummary]

      result mustBe JsSuccess(validResponseRepaymentsSummary)
    }

    "round-trip write then read should return same object" in {
      val json = Json.toJson(validResponseRepaymentsSummary)
      val parsed = json.as[RepaymentsSummary]

      parsed mustBe validResponseRepaymentsSummary
    }

    "fail to deserialize when required fields are missing" in {
      val json = Json.parse(
        """
          |{
          |  "periodStartDate":"2013-03-01"
          |}
          |""".stripMargin
      )

      val result = json.validate[RepaymentsSummary]

      result.isError mustBe true
    }

    "fail to deserialize when field types are incorrect" in {
      val json = Json.parse(
        s"""{
           |  "periodStartDate":"2013-03-01",
           |  "periodEndDate":"2014-03-11",
           |  "actualRepaymentsAmount":"£71.84",
           |  "repaymentsInterestRepaidAmount":-35.76,
           |  "total":36.08
           |}""".stripMargin
      )

      val result = json.validate[RepaymentsSummary]

      result.isError mustBe true
    }
  }
}
