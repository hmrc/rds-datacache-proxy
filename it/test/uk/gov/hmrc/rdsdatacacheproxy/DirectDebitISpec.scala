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

class DirectDebitISpec extends ApplicationWithWiremock
  with Matchers
  with ScalaFutures
  with IntegrationPatience:

  "Direct Debits" should :
    "succeed" when:
      "retrieving direct debits" when :
        "user has no direct debits" in:
          AuthStub.authorised()
          val response = get("/direct-debits").futureValue

          response.status shouldBe OK

        "user has one direct debit" in:
          AuthStub.authorised()
          val response = get("/direct-debits").futureValue

          response.status shouldBe OK

        "user has many direct debits" in:
          AuthStub.authorised()
          val response = get("/direct-debits").futureValue

          response.status shouldBe OK

        "user provides an offset and limit" in:
          AuthStub.authorised()
          val response = get("/direct-debits?offset=2020-02-02&limit=3").futureValue

          response.status shouldBe OK

      "creating direct debits" in:
        AuthStub.authorised()
        val response = post(
          "/direct-debits",
          Json.parse(
            s"""
               |{
               |  "paymentReference": "12345"
               |}
              """.stripMargin)
        ).futureValue

        response.status shouldBe CREATED
        response.json shouldBe Json.parse("""12345""")

    "fail" when:
      "with a 400" when :
        "calling an endpoint without required JSON" in:
          AuthStub.authorised()
          val response = post(
            "/direct-debits",
            Json.parse(
              """
                |{
                |  "whoAmI":"Where am I?"
                |}""".stripMargin)).futureValue

          response.status shouldBe BAD_REQUEST
          response.json.toString should include("Json validation error")

        "calling with only an offset date" in:
          AuthStub.authorised()
          val response = get("/direct-debits?offset=2020-02-02").futureValue

          response.status shouldBe BAD_REQUEST

        "calling with an invalid offset date" in:
          AuthStub.authorised()
          val response = get("/direct-debits?offset=PancakeDay&limit=3").futureValue

          response.status shouldBe INTERNAL_SERVER_ERROR

        "calling with only a limit" in:
          AuthStub.authorised()
          val response = get("/direct-debits?limit=2").futureValue

          response.status shouldBe BAD_REQUEST

      "with a 401" when :
        "calling an endpoint when tokens are unauthorised" in:
          AuthStub.unauthorised()
          val response = get("/direct-debits").futureValue

          response.status shouldBe UNAUTHORIZED
          response.json shouldBe Json.parse("""{"statusCode":401,"message":"MissingResponseHeader"}""")

        "calling an endpoint when no internalId returned by Auth" in:
          AuthStub.noTokenReturned()
          val response = get("/direct-debits").futureValue

          response.status shouldBe UNAUTHORIZED
          response.json shouldBe Json.parse("""{"statusCode":401,"message":"Unable to retrieve internal Id"}""")

        "calling an endpoint when no tokens in header" in:
          val response = wsClient.url(s"$baseUrl/direct-debits").get().futureValue

          response.status shouldBe UNAUTHORIZED
          response.json shouldBe Json.parse("""{"statusCode":401,"message":"Bearer token not supplied"}""")

      "with a 404" when :
        "calling an endpoint that doesn't exist" in:
          AuthStub.authorised()
          val response = get("/indirect-debits").futureValue

          response.status shouldBe NOT_FOUND
