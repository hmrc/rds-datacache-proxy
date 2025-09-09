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
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

class MonthlyReturnSpec extends ApplicationWithWiremock
  with Matchers
  with ScalaFutures
  with IntegrationPatience {

  "Monthly Returns" should :
    "succeed" when {
      "retrieving monthly returns with both tax office number and tax office reference" in :
        AuthStub.authorised()
        val response = post(
          uri  = "/monthly-returns",
          body = Json.obj("taxOfficeNumber" -> "123", "taxOfficeReference" -> "AB456")
        ).futureValue

        response.status shouldBe OK
        response.json.toString should include("monthlyReturnList")
    }

    "fail with 400" when {
      "both identifiers are missing" in {
        AuthStub.authorised()
        val response = post(
          uri  = "/monthly-returns",
          body = Json.obj() 
        ).futureValue
        
        response.status shouldBe BAD_REQUEST
        response.json.toString.toLowerCase should include("missing")
      }

      "TaxOfficeNumber is missing" in {
        AuthStub.authorised()
        val response = post(
          "/monthly-returns",
          Json.obj("taxOfficeReference" -> "AB456")
        ).futureValue

        response.status shouldBe BAD_REQUEST
      }

      "TaxOfficeReference is missing" in {
        AuthStub.authorised()
        val response = post(
          "/monthly-returns",
          Json.obj("taxOfficeNumber" -> "123")
        ).futureValue

        response.status shouldBe BAD_REQUEST
      }
    }

      "fail with 401" when {
        "auth says unauthorised" in {
          AuthStub.unauthorised()
          val response = post(
            "/monthly-returns",
            Json.obj("taxOfficeNumber" -> "123", "taxOfficeReference" -> "AB456")
          ).futureValue

          response.status shouldBe UNAUTHORIZED
        }

        "no auth/session headers sent at all" in {
          val response = wsClient
            .url(s"$baseUrl/monthly-returns")
            .post(Json.obj("taxOfficeNumber" -> "123", "taxOfficeReference" -> "AB456"))
            .futureValue

          response.status shouldBe UNAUTHORIZED
        }

        "auth returns 200 but without internalId" in {
          AuthStub.noTokenReturned()
          val response = post(
            "/monthly-returns",
            Json.obj("taxOfficeNumber" -> "123", "taxOfficeReference" -> "AB456")
          ).futureValue

          response.status shouldBe UNAUTHORIZED
        }
      }

    "return 404" when {
      "calling a non-existent endpoint" in {
        AuthStub.authorised()
        val response = wsClient
          .url(s"$baseUrl/does-not-exist")
          .post(Json.obj("taxOfficeNumber" -> "123", "taxOfficeReference" -> "AB456"))
          .futureValue

        response.status shouldBe NOT_FOUND
      }
    }
}

