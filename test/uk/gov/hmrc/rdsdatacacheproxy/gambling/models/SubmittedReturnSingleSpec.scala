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
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseSubmittedReturnSingle

class SubmittedReturnSingleSpec extends AnyWordSpec with Matchers {

  "SubmittedReturnSingle JSON format" should {

    "serialize to JSON correctly" in {
      val json = Json.toJson(validResponseSubmittedReturnSingle)

      (json \ "consecNo").as[Int] mustBe 23
      (json \ "mgdPeriod").as[String] mustBe "01/01/2025 - 30/03/2025"
      (json \ "submittedDate").as[String] mustBe "2025-05-01"
      (json \ "ackRef").as[String] mustBe "123456789012345"
      (json \ "noOfMachines").as[Int] mustBe 5
      (json \ "netTakingsHigherRate").as[Double] mustBe 100.10
      (json \ "netTakingsStdRate").as[Double] mustBe 20.00
      (json \ "netTakingsLowerRate").as[Double] mustBe 200.20
      (json \ "totalDueHigherRate").as[Double] mustBe 10.00
      (json \ "totalDueStdRate").as[Double] mustBe 300.30
      (json \ "totalDueLowerRate").as[Double] mustBe 5.00
      (json \ "dutyPayable").as[Double] mustBe 35.00
      (json \ "underDeclaredDuty").as[Double] mustBe 40.00
      (json \ "previousReturnAmount").as[Double] mustBe 100.00
      (json \ "negativeAmountCarriedForward").as[Double] mustBe 99.99
      (json \ "totalNetDutyPayable").as[Double] mustBe 75.49
    }

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        s"""{
           |  "consecNo":23,
           |  "mgdPeriod":"01/01/2025 - 30/03/2025",
           |  "submittedDate":"2025-05-01",
           |  "ackRef":"123456789012345",
           |  "noOfMachines":5,
           |  "netTakingsHigherRate":100.1,
           |  "netTakingsStdRate":20.0,
           |  "netTakingsLowerRate":200.2,
           |  "totalDueHigherRate":10.0,
           |  "totalDueStdRate":300.3,
           |  "totalDueLowerRate":5.0,
           |  "dutyPayable":35.0,
           |  "underDeclaredDuty":40.0,
           |  "previousReturnAmount":100.0,
           |  "negativeAmountCarriedForward":99.99,
           |  "totalNetDutyPayable":75.49
           |}""".stripMargin
      )

      val result: JsResult[SubmittedReturnSingle] = json.validate[SubmittedReturnSingle]

      result mustBe JsSuccess(validResponseSubmittedReturnSingle)
    }

    "round-trip write then read should return same object" in {
      val json = Json.toJson(validResponseSubmittedReturnSingle)
      val parsed = json.as[SubmittedReturnSingle]

      parsed mustBe validResponseSubmittedReturnSingle
    }

    "fail to deserialize when required fields are missing" in {
      val json = Json.parse(
        """
          |{
          |  "mgdPeriod":"01/01/2025 - 30/03/2025"
          |}
          |""".stripMargin
      )

      val result = json.validate[SubmittedReturnSingle]

      result.isError mustBe true
    }

    "fail to deserialize when field types are incorrect" in {
      val json = Json.parse(
        s"""{
           |  "consecNo":23,
           |  "mgdPeriod":"01/01/2025 - 30/03/2025",
           |  "submittedDate":"2025-05-01",
           |  "reference":"123456789012345",
           |  "noOfMachines":"5",
           |  "netTakingsHigherRate":100.1,
           |  "netTakingsStdRate":20.0,
           |  "netTakingsLowerRate":200.2,
           |  "totalDueHigherRate":10.0,
           |  "totalDueStdRate":300.3,
           |  "totalDueLowerRate":5.0,
           |  "dutyPayable":35.0,
           |  "underDeclaredDuty":40.0,
           |  "previousReturnAmount":100.0,
           |  "negativeAmountCarriedForward":99.99,
           |  "totalNetDutyPayable":75.49
           |}""".stripMargin
      )

      val result = json.validate[SubmittedReturnSingle]

      result.isError mustBe true
    }
  }
}
