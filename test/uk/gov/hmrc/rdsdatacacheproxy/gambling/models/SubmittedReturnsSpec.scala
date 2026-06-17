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
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseSubmittedReturns

class SubmittedReturnsSpec extends AnyWordSpec with Matchers {

  "SubmittedReturns JSON format" should {

    "serialize to JSON correctly" in {
      val json = Json.toJson(validResponseSubmittedReturns)

      (json \ "items").as[Seq[SubmittedReturnsItem]].size mustBe 3

      val item1 = (json \ "items")(0)
      (item1 \ "consec_no").as[Int] mustBe 12345
      (item1 \ "mgd_period").as[String] mustBe "01/01/2025 - 30/03/2025"
      (item1 \ "submitted_date").as[String] mustBe "2025-04-01"
      (item1 \ "ack_ref").as[String] mustBe "123456789012345"

      val item2 = (json \ "items")(1)
      (item2 \ "consec_no").as[Int] mustBe 22345
      (item2 \ "mgd_period").as[String] mustBe "01/04/2025 - 30/06/2025"
      (item2 \ "submitted_date").as[String] mustBe "2025-07-01"
      (item2 \ "ack_ref").as[String] mustBe "12345"

      val item3 = (json \ "items")(2)
      (item3 \ "consec_no").as[Int] mustBe 111222
      (item3 \ "mgd_period").as[String] mustBe "10/02/2024 - 29/04/2024"
      (item3 \ "submitted_date").as[String] mustBe "2024-05-01"
      (item3 \ "ack_ref").as[String] mustBe "111222111222"
    }

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        s"""{
           |  "items":[
           |    {
           |      "consec_no":12345,
           |      "mgd_period":"01/01/2025 - 30/03/2025",
           |      "submitted_date":"2025-04-01",
           |      "ack_ref":"123456789012345"
           |    },
           |    {
           |      "consec_no":22345,
           |      "mgd_period":"01/04/2025 - 30/06/2025",
           |      "submitted_date":"2025-07-01",
           |      "ack_ref":"12345"
           |    },
           |    {
           |      "consec_no":111222,
           |      "mgd_period":"10/02/2024 - 29/04/2024",
           |      "submitted_date":"2024-05-01",
           |      "ack_ref":"111222111222"
           |    }
           |  ]
           |}""".stripMargin
      )

      val result: JsResult[SubmittedReturns] = json.validate[SubmittedReturns]

      result mustBe JsSuccess(validResponseSubmittedReturns)
    }

    "round-trip write then read should return same object" in {
      val json = Json.toJson(validResponseSubmittedReturns)
      val parsed = json.as[SubmittedReturns]

      parsed mustBe validResponseSubmittedReturns
    }

    "fail to deserialize when required fields are missing" in {
      val json = Json.parse(
        """
          |{
          |  "periodStartDate":"2013-03-01"
          |}
          |""".stripMargin
      )

      val result = json.validate[SubmittedReturns]

      result.isError mustBe true
    }

    "fail to deserialize when field types are incorrect" in {
      val json = Json.parse(
        s"""{
             "items":[
           |    {
           |      "consec_no":"12345",
           |      "mgd_period":"01/01/2025 - 30/03/2025",
           |      "submitted_date":"2025-04-01",
           |      "ack_ref":"123456789012345"
           |    }
           |  ]
           |}""".stripMargin
      )

      val result = json.validate[SubmittedReturns]

      result.isError mustBe true
    }
  }
}
