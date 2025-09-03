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
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

class MonthlyReturnSpec extends ApplicationWithWiremock
  with Matchers
  with ScalaFutures
  with IntegrationPatience {

  private val TonHeader = "X-Tax-Office-Number"
  private val TorHeader = "X-Tax-Office-Reference"

  "Monthly Returns" should :
    "succeed" when {
      "retrieving monthly returns with both tax office number and tax office reference" in :
        AuthStub.authorised()
        val response = get(
          "/monthly-returns",
          TonHeader -> "123",
          TorHeader -> "AB456"
        ).futureValue

        response.status shouldBe OK
        response.json.toString should include("monthlyReturnList")

      "trim whitespace in both identifiers" in {
        AuthStub.authorised()
        val response = get(
          "/monthly-returns",
          TonHeader -> "   123   ",
          TorHeader -> "   AB456   "
        ).futureValue
        response.status shouldBe OK
      }
    }

    "fail with 400" when {
      "both identifiers are missing" in {
        AuthStub.authorised()
        val response = get("/monthly-returns").futureValue
        response.status shouldBe BAD_REQUEST
        response.json.toString.toLowerCase should include("missing")
      }

      "TaxOfficeNumber is missing" in {
        AuthStub.authorised()
        val response = get(
          "/monthly-returns",
          TorHeader -> "AB456"
        ).futureValue
        response.status shouldBe BAD_REQUEST
      }

      "TaxOfficeReference is missing" in {
        AuthStub.authorised()
        val response = get(
          "/monthly-returns",
          TonHeader -> "123"
        ).futureValue
        response.status shouldBe BAD_REQUEST
      }
    }

      "fail with 401" when {
        "auth says unauthorised" in {
          AuthStub.unauthorised()
          val response = get(
            "/monthly-returns",
            TonHeader -> "123",
            TorHeader -> "AB456"
          ).futureValue
          response.status shouldBe UNAUTHORIZED
        }

        "no auth/session headers sent at all" in {
          val response = wsClient
            .url(s"$baseUrl/monthly-returns")
            .withHttpHeaders(TonHeader -> "123", TorHeader -> "AB456")
            .get()
            .futureValue
          response.status shouldBe UNAUTHORIZED
        }

        "auth returns 200 but without internalId" in {
          AuthStub.noTokenReturned()
          val response = get(
            "/monthly-returns",
            TonHeader -> "123",
            TorHeader -> "AB456"
          ).futureValue
          response.status shouldBe UNAUTHORIZED
        }
      }

    "return 404" when {
      "calling a non-existent endpoint" in {
        AuthStub.authorised()
        val response = get(
          "/does-not-exist",
          TonHeader -> "123",
          TorHeader -> "AB456"
        ).futureValue
        response.status shouldBe NOT_FOUND
      }
    }
}

