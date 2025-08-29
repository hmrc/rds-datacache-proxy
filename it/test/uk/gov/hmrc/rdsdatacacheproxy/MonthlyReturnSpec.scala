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
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

class MonthlyReturnSpec extends ApplicationWithWiremock
  with Matchers
  with ScalaFutures
  with IntegrationPatience:

  "Monthly Returns" should :
    "succeed" when:
      "retrieving monthly returns with a valid instanceId" in :
          AuthStub.authorised()
          val response = get("/monthly-returns?instanceId=test123").futureValue

          response.status shouldBe OK
          response.json.toString should include ("monthlyReturnList")

      "trims whitespace in instanceId" in:
          AuthStub.authorised()
          val response = get("/monthly-returns?instanceId=%20%20test123%20%20").futureValue

          response.status shouldBe OK
          response.json.toString should include("monthlyReturnList")

    "fail" when:
      "with a 400" when :
        "missing instanceId" in:
          AuthStub.authorised()
          val response = get("/monthly-returns").futureValue

          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.obj("message" -> "Missing or empty instanceId")

        "empty instanceId" in:
          AuthStub.authorised()
          val response = get("/monthly-returns?instanceId=").futureValue

          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.obj("message" -> "Missing or empty instanceId")

        "whitespace-only instanceId" in :
          AuthStub.authorised()
          val response = get("/monthly-returns?instanceId=%20%20").futureValue

          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.obj("message" -> "Missing or empty instanceId")

      "with a 401" when :
        "calling an endpoint when tokens are unauthorised" in:
          AuthStub.unauthorised()
          val response = get("/monthly-returns?instanceId=test123").futureValue

          response.status shouldBe UNAUTHORIZED
          response.json shouldBe Json.parse("""{"statusCode":401,"message":"MissingResponseHeader"}""")

        "calling an endpoint when no internalId returned by Auth" in:
          AuthStub.noTokenReturned()
          val response = get("/monthly-returns?instanceId=test123").futureValue

          response.status shouldBe UNAUTHORIZED
          response.json shouldBe Json.parse("""{"statusCode":401,"message":"Unable to retrieve internal ID from headers"}""")

        "calling an endpoint when no session in header" in:
          val response = wsClient.url(s"$baseUrl/monthly-returns?instanceId=test123").get().futureValue

          response.status shouldBe UNAUTHORIZED
          response.json shouldBe Json.parse("""{"statusCode":401,"message":"Unable to retrieve session ID from headers"}""")

        "calling an endpoint when no auth tokens in header" in:
          val response = wsClient
            .url(s"$baseUrl/monthly-returns?instanceId=test123")
            .withHttpHeaders(HeaderNames.xSessionId -> "sessionId")
            .get()
            .futureValue

          response.status shouldBe UNAUTHORIZED
          response.json shouldBe Json.parse("""{"statusCode":401,"message":"Bearer token not supplied"}""")

      "with a 404" when :
        "calling an endpoint that doesn't exist" in:
          AuthStub.authorised()
          val response = get("/daily-returns?instanceId=test123").futureValue

          response.status shouldBe NOT_FOUND

