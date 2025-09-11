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
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}
import uk.gov.hmrc.rdsdatacacheproxy.models.responses.DirectDebit

import java.time.LocalDateTime

class DirectDebitISpec extends ApplicationWithWiremock
  with Matchers
  with ScalaFutures
  with IntegrationPatience:

  def expected(i: Int): DirectDebit =
    DirectDebit.apply(
      s"defaultRef$i",
      LocalDateTime.parse("2020-02-02T22:22:22"),
      "00-00-00",
      "00000000",
      "Bank Ltd",
      false,
      i
    )

  "Direct Debits" should :
    "succeed" when:
      "retrieving direct debits" in :
        AuthStub.authorised()
        val response = get("/direct-debits").futureValue

        response.status shouldBe OK

      "retrieving future working days" in :
        AuthStub.authorised()
        val response = post("/direct-debits/future-working-days",
          Json.parse(
            s"""
               |{
               |  "baseDate": "2024-12-28",
               |  "offsetWorkingDays": 10
               |}
            """.stripMargin)
        ).futureValue

        response.status shouldBe OK

      "retrieving direct debit reference" in :
        AuthStub.authorised()
        val response = post("/direct-debit-reference",
          Json.parse(
            s"""
               |{
               |  "paymentReference": "693048576"
               |}
            """.stripMargin)
        ).futureValue

        response.status shouldBe OK

    "fail" when:
      "with a 401" when :
        "calling an retrieve DD endpoint when tokens are unauthorised" in :
          AuthStub.unauthorised()
          val response = get("/direct-debits").futureValue

          response.status shouldBe UNAUTHORIZED
        "calling an DD endpoint when no session in header" in:
          val response = wsClient.url(s"$baseUrl/direct-debits").get().futureValue

          response.status shouldBe UNAUTHORIZED
          response.json shouldBe Json.parse("""{"statusCode":401,"message":"Unable to retrieve session ID from headers"}""")

      "with a 404" when :
        "calling incorrect retrieval DD endpoint that doesn't exist" in:
          AuthStub.authorised()
          val response = get("/indirect-debits").futureValue

          response.status shouldBe NOT_FOUND

        "calling incorrect future working days endpoint when tokens are unauthorised" in :
          AuthStub.unauthorised()
          val response = get("/direct-debit/future-working-days").futureValue

          response.status shouldBe NOT_FOUND

        "calling incorrect ddi ref endpoint when tokens are unauthorised" in :
          AuthStub.unauthorised()
          val response = get("/direct-debit-reference1").futureValue

          response.status shouldBe NOT_FOUND
