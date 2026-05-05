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

package uk.gov.hmrc.rdsdatacacheproxy.gambling

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.http.Status.*
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.gambling.GamblingReturnsStubData.getReturnsSubmittedData
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.ReturnsSubmitted
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.GamblingReturnsDataSource
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GamblingReturnsControllerIntegrationSpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with ApplicationWithWiremock {

  class GamblingReturnsRdsStub extends GamblingReturnsDataSource {
    override def getReturnsSubmitted(regNumber: String, pageNo: Int, pageSize: Int) =
      Future {
        GamblingReturnsStubData.getReturnsSubmittedData(regNumber, pageNo, pageSize)
      }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[GamblingReturnsDataSource].toInstance(new GamblingReturnsRdsStub)
      )
      .build()

  private final val endpoint = "/gambling/returns-submitted"
  private final val GBD = "gbd"

  "GET /gambling/returns-submitted (stubbed repo, no DB)" should {

    "return 200 with correct ReturnsSubmittedData" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/$GBD/XYZ00000000000?pageNo=1&pageSize=10").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[ReturnsSubmitted] mustBe getReturnsSubmittedData("XYZ00000000000")
    }

    "return 200 with correct ReturnsSubmittedData when pageNo & pageSize NOT provided" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/$GBD/XYZ99999999999").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[ReturnsSubmitted] mustBe getReturnsSubmittedData("XYZ99999999999")
    }

    "normalise lowercase input" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$GBD/xyz00000000012 ").futureValue
      response.status mustBe OK
      response.json.as[ReturnsSubmitted] mustBe getReturnsSubmittedData("XYZ00000000012")
    }

    "trim whitespace around regNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$GBD/   XYZ00000000012   ").futureValue
      response.status mustBe OK
      response.json.as[ReturnsSubmitted] mustBe getReturnsSubmittedData("XYZ00000000012")
    }

    "return consistent results across multiple calls" in {
      AuthStub.authorised()
      val res1 = get(s"$endpoint/$GBD/XYZ00000000012").futureValue
      val res2 = get(s"$endpoint/$GBD/XYZ00000000012").futureValue
      res1.json mustBe res2.json
    }

    "return JSON content type for valid response" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$GBD/XYZ00000000012").futureValue
      response.contentType mustBe "application/json"
    }

    "return 400 for partially valid regNumber (wrong length)" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$GBD/XYZ123?pageNo=1&pageSize=10").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for invalid regime)" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/BAD_REGIME/XYZ00000000012?pageNo=1&pageSize=10").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for regNumber with special characters" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$GBD/XYZ00000@00000").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for invalid regNumber format" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/$GBD/INVALID").futureValue
      response.status mustBe BAD_REQUEST
      (response.json \ "code").as[String] mustBe "INVALID_REG_NUMBER"
      (response.json \ "message").as[String] mustBe "regNumber has invalid format"
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()
      val response = get(s"$endpoint/$GBD/XYZ00000000000").futureValue
      response.status mustBe UNAUTHORIZED
    }

    "return 404 for missing regNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$GBD/").futureValue
      response.status mustBe NOT_FOUND
    }

    "return 404 for whitespace-only regNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$GBD/   ").futureValue
      response.status mustBe NOT_FOUND
    }

    "return 500 when stub simulates failure" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$GBD/ERR00000000000").futureValue
      response.status mustBe INTERNAL_SERVER_ERROR
      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
    }

    "return correct error structure for 500 response" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$GBD/ERR00000000000").futureValue
      response.status mustBe INTERNAL_SERVER_ERROR
      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
      (response.json \ "message").as[String] mustBe "Unexpected error occurred"
    }

  }
}
