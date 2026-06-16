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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.controllers

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.http.Status.*
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.SubmittedReturns
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.SubmittedReturnsDataSource
import uk.gov.hmrc.rdsdatacacheproxy.gambling.stub.SubmittedReturnsStubData
import uk.gov.hmrc.rdsdatacacheproxy.gambling.stub.SubmittedReturnsStubData.{DEFAULT_ORDER_BY, DEFAULT_SORT_BY, getSubmittedReturnsData}
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmittedReturnsControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class SubmittedReturnsRdsStub extends SubmittedReturnsDataSource {
    override def getSubmittedReturns(regNumber: String, sortBy: Int, orderBy: String) =
      Future {
        SubmittedReturnsStubData.getSubmittedReturnsData(regNumber, sortBy, orderBy)
      }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[SubmittedReturnsDataSource].toInstance(new SubmittedReturnsRdsStub)
      )
      .build()

  private final val endpoint = "/gambling/submitted-returns"

  "GET /gambling/submitted-returns (stubbed repo, no DB)" should {

    "return 200 with correct SubmittedReturnsData" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/XYZ00000000001?sortBy=1&orderBy=DESC").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[SubmittedReturns] mustBe getSubmittedReturnsData("XYZ00000000001", 1, "DESC")
    }

    "return 200 with correct SubmittedReturnsData with DEFAULTS when sortBy & orderBy NOT provided" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/XYZ00000000001").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[SubmittedReturns] mustBe getSubmittedReturnsData("XYZ00000000001", DEFAULT_SORT_BY, DEFAULT_ORDER_BY)
    }

    "return 200 with correct SubmittedReturnsData with DEFAULTS when sortBy & orderBy are RANDOM" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/XYZ00000000001?sortBy=999&orderBy=RANDOM").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[SubmittedReturns] mustBe getSubmittedReturnsData("XYZ00000000001", DEFAULT_SORT_BY, DEFAULT_ORDER_BY)
    }

    "normalise lowercase input" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/xyz00000000012 ").futureValue
      response.status mustBe OK
      response.json.as[SubmittedReturns] mustBe getSubmittedReturnsData("XYZ00000000012", DEFAULT_SORT_BY, DEFAULT_ORDER_BY)
    }

    "trim whitespace around regNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/   XYZ00000000012   ").futureValue
      response.status mustBe OK
      response.json.as[SubmittedReturns] mustBe getSubmittedReturnsData("XYZ00000000012", DEFAULT_SORT_BY, DEFAULT_ORDER_BY)
    }

    "return consistent results across multiple calls" in {
      AuthStub.authorised()
      val res1 = get(s"$endpoint/XYZ00000000012").futureValue
      val res2 = get(s"$endpoint/XYZ00000000012").futureValue
      res1.json mustBe res2.json
    }

    "return JSON content type for valid response" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/XYZ00000000012").futureValue
      response.contentType mustBe "application/json"
    }

    "return 400 for partially valid regNumber (wrong length)" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/XYZ123").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for regNumber with special characters" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/XYZ00000@00000").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for invalid regNumber format" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/INVALID").futureValue
      response.status mustBe BAD_REQUEST
      (response.json \ "code").as[String] mustBe "INVALID_REG_NUMBER"
      (response.json \ "message").as[String] mustBe "regNumber has invalid format"
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()
      val response = get(s"$endpoint/XYZ00000000000").futureValue
      response.status mustBe UNAUTHORIZED
    }

    "return 404 for missing regNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/").futureValue
      response.status mustBe NOT_FOUND
    }

    "return 404 for whitespace-only regNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/   ").futureValue
      response.status mustBe NOT_FOUND
    }

    "return 500 when stub simulates failure" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/ERR00000000000").futureValue
      response.status mustBe INTERNAL_SERVER_ERROR
      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
    }

    "return correct error structure for 500 response" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/ERR00000000000").futureValue
      response.status mustBe INTERNAL_SERVER_ERROR
      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
      (response.json \ "message").as[String] mustBe "Unexpected error occurred"
    }

  }
}
