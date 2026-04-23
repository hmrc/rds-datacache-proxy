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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.*
import java.time.LocalDate


class GamblingModelSpec extends AnyWordSpec with Matchers {


  "ReturnSummaryModel" should {
    "read and write a full-populated object" in {
      val jsonAsString: String =
        s"""
          |{
          |"mgdRegNumber": "XYZ00000000000",
          |"returnsDue": 2,
          |"returnsOverdue": 1
          |}
        """.stripMargin


      val json = Json.parse(jsonAsString)
      val model = json.as[ReturnSummary]

      model mustBe ReturnSummary(
        mgdRegNumber  = "XYZ00000000000",
        returnsDue = 2,
        returnsOverdue = 1
      )

      Json.toJson(model) mustBe json
    }
  }
    "BusinessNameModel" should {
  val dateBusinessName: LocalDate = LocalDate.of(1991, 1, 1)
    "read and write a full-populated object" in {
      val jsonAsString: String =
        s"""
          |{
          |"mgdRegNumber": "XYZ00000000000",
          |"solePropTitle": "Mr",
          |"solePropFirstName": "John",
          |"solePropMidName": "C",
          |"solePropLastName": "Doe",
          |"businessName": "John Doe Co.",
          |"businessType": "Sole Proprietor",
          |"tradingName": "DoeDoe",
          |"systemDate": "$dateBusinessName"
          |}
        """.stripMargin


      val json = Json.parse(jsonAsString)
      val model = json.as[BusinessName]

      model mustBe BusinessName(
        mgdRegNumber  = "XYZ00000000000",
        solePropTitle = "Mr",
        solePropFirstName = "John",
        solePropMidName = "C",
        solePropLastName = "Doe",
        businessName = "John Doe Co.",
        businessType = "Sole Proprietor",
        tradingName = "DoeDoe",
        systemDate = Some(dateBusinessName)
      )

      Json.toJson(model) mustBe json
    }
  }
}

