/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.*
import play.api.libs.json.Json
import uk.gov.hmrc.rdsdatacacheproxy.itutil.ApplicationWithWiremock

class WorkingDaysOffsetISpec extends ApplicationWithWiremock with Matchers with ScalaFutures with IntegrationPatience:

  "Working Days Offset" should:
    "succeed" when:
      "retrieving Earliest Payment Date" when:
        "user provides a date and offset" in:
          val response = post(
            "/direct-debits/future-working-days",
            Json.parse(s"""
                 |{
                 |  "baseDate": "2025-01-01",
                 |  "offsetWorkingDays": 2
                 |}
                 |""".stripMargin)
          ).futureValue

          response.status        shouldBe OK
          response.json.toString shouldBe """{"date":"2025-01-03"}"""

    "fail" when:
      "with a 400" when:
        "calling an endpoint without required JSON" in:
          val response = post("/direct-debits/future-working-days",
                              Json.parse("""
                |{
                |  "whoAmI":"Where am I?"
                |}""".stripMargin)
                             ).futureValue

          response.status      shouldBe BAD_REQUEST
          response.json.toString should include("Json validation error")

        "calling with an invalid date" in:
          val response = post(
            "/direct-debits/future-working-days",
            Json.parse(s"""
                 |{
                 |  "baseDate": "My Birthday",
                 |  "offsetWorkingDays": 5
                 |}
                 |""".stripMargin)
          ).futureValue

          response.status      shouldBe BAD_REQUEST
          response.json.toString should include("Json validation error")

        "calling with an invalid firstNumber" in:
          val response = post(
            "/direct-debits/future-working-days",
            Json.parse(s"""
                 |{
                 |  "baseDate": "2025-01-01",
                 |  "offsetWorkingDays": "five"
                 |}
                 |""".stripMargin)
          ).futureValue

          response.status shouldBe BAD_REQUEST

    "with a 404" when:
      "calling an endpoint that doesn't exist" in:
        val response = get("/future-working-days").futureValue

        response.status shouldBe NOT_FOUND
