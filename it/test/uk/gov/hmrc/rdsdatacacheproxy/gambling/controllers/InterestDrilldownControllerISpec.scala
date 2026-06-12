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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{InterestDetails, InterestDrilldown, InterestDrilldownItem, Regime}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.InterestDataSource
import uk.gov.hmrc.rdsdatacacheproxy.gambling.stub.InterestDrilldownStubData
import uk.gov.hmrc.rdsdatacacheproxy.gambling.stub.InterestDrilldownStubData.getInterestDrilldownData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class InterestDrilldownControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class InterestRdsStub extends InterestDataSource {
    override def getInterestDrilldown(regime: Regime, regNumber: String, interestId: String, pageNo: Int, pageSize: Int): Future[InterestDrilldown] =
      Future {
        InterestDrilldownStubData.getInterestDrilldownData(regNumber, interestId, pageNo, pageSize)
      }

    override def getInterestDetails(regime: Regime, regNumber: String, paginationStart: Int, paginationMaxRows: Int): Future[InterestDetails] =
      Future.failed(new UnsupportedOperationException("Not required for this test suite"))
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[InterestDataSource].toInstance(new InterestRdsStub)
      )
      .build()

  private final val endpoint = "/gambling/interest-drilldown"
  private final val GBD = "gbd"
  private final val interestId = "INT001"

  "GET /gambling/interest-drilldown (stubbed repo, no DB)" should {

    "return 200 with correct InterestData" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/$GBD/XYZ00000000000/$interestId?pageNo=1&pageSize=10").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"
      response.json.as[InterestDrilldown] mustBe getInterestDrilldownData("XYZ00000000000")
    }

    "return 200 with correct InterestData when pageNo & pageSize NOT provided" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/$GBD/XYZ99999999999/$interestId").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"
      response.json.as[InterestDrilldown] mustBe getInterestDrilldownData("XYZ99999999999")
    }

    "return 200 with empty items when no interest data exists for regNumber" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/$GBD/XYZ00000000000/$interestId").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"
      (response.json \ "items").as[Seq[InterestDrilldownItem]] mustBe empty
      (response.json \ "totalRecords").as[Int] mustBe 0
    }

    "normalise lowercase input" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$GBD/xyz00000000012 /$interestId").futureValue
      response.status mustBe OK
      response.json.as[InterestDrilldown] mustBe getInterestDrilldownData("XYZ00000000012")
    }

    "trim whitespace around regNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$GBD/   XYZ00000000012   /$interestId").futureValue
      response.status mustBe OK
      response.json.as[InterestDrilldown] mustBe getInterestDrilldownData("XYZ00000000012")
    }

    "return consistent results across multiple calls" in {
      AuthStub.authorised()
      val res1 = get(s"$endpoint/$GBD/XYZ00000000012/$interestId").futureValue
      val res2 = get(s"$endpoint/$GBD/XYZ00000000012/$interestId").futureValue
      res1.json mustBe res2.json
    }

    "return JSON content type for valid response" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$GBD/XYZ00000000012/$interestId").futureValue
      response.contentType mustBe "application/json"
    }

    "return 400 for partially valid regNumber (wrong length)" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$GBD/XYZ123/$interestId?pageNo=1&pageSize=10").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for invalid regime" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/BAD_REGIME/XYZ00000000012/$interestId?pageNo=1&pageSize=10").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for regNumber with special characters" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$GBD/XYZ00000@00000/$interestId").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for invalid regNumber format" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/$GBD/INVALID/$interestId").futureValue
      response.status mustBe BAD_REQUEST
      (response.json \ "code").as[String] mustBe "INVALID_REG_NUMBER"
      (response.json \ "message").as[String] mustBe "regNumber has invalid format"
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()
      val response = get(s"$endpoint/$GBD/XYZ00000000000/$interestId").futureValue
      response.status mustBe UNAUTHORIZED
    }

    "return 404 for missing regNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$GBD/").futureValue
      response.status mustBe NOT_FOUND
    }

    "return correct error structure for 500 response" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$GBD/ERR00000000000/$interestId").futureValue
      response.status mustBe INTERNAL_SERVER_ERROR
      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
      (response.json \ "message").as[String] mustBe "Unexpected error occurred"
    }
  }
}
