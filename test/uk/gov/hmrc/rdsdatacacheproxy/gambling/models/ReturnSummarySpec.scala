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

class ReturnSummarySpec extends AnyWordSpec with Matchers {

  "ReturnSummary JSON format" should {

    "serialize to JSON correctly" in {
      val model = ReturnSummary(
        mgdRegNumber   = "XYZ00000000012",
        returnsDue     = 1,
        returnsOverdue = 2
      )

      val json = Json.toJson(model)

      (json \ "mgdRegNumber").as[String] mustBe "XYZ00000000012"
      (json \ "returnsDue").as[Int] mustBe 1
      (json \ "returnsOverdue").as[Int] mustBe 2
    }

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        """
          |{
          |  "mgdRegNumber": "XYZ00000000012",
          |  "returnsDue": 1,
          |  "returnsOverdue": 2
          |}
          |""".stripMargin
      )

      val result: JsResult[ReturnSummary] = json.validate[ReturnSummary]

      result mustBe JsSuccess(
        ReturnSummary("XYZ00000000012", 1, 2)
      )
    }

    "round-trip (write then read) should return same object" in {
      val original = ReturnSummary("XYZ00000000021", 2, 1)

      val json = Json.toJson(original)
      val parsed = json.as[ReturnSummary]

      parsed mustBe original
    }

    "fail to deserialize when fields are missing" in {
      val json = Json.parse(
        """
          |{
          |  "mgdRegNumber": "XYZ00000000012"
          |}
          |""".stripMargin
      )

      val result = json.validate[ReturnSummary]

      result.isError mustBe true
    }

    "fail to deserialize when types are incorrect" in {
      val json = Json.parse(
        """
          |{
          |  "mgdRegNumber": "XYZ00000000012",
          |  "returnsDue": "one",
          |  "returnsOverdue": 2
          |}
          |""".stripMargin
      )

      val result = json.validate[ReturnSummary]

      result.isError mustBe true
    }
  }
}
