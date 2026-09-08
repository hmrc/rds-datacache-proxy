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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{LicenceDetails, Regime}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.LicenceDataSource
import uk.gov.hmrc.rdsdatacacheproxy.gambling.stub.LicenceStubData
import uk.gov.hmrc.rdsdatacacheproxy.gambling.stub.LicenceStubData.getLicenceDetailsData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LicenceControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class LicenceRdsStub extends LicenceDataSource {
    override def getLicenceDetails(regime: Regime, regNumber: String) =
      Future {
        LicenceStubData.getLicenceDetailsData(regNumber)
      }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[LicenceDataSource].toInstance(new LicenceRdsStub)
      )
      .build()

  private final val endpoint = "/gambling/licence-details"
  private final val MGD = "mgd"

  "GET /gambling/licence-details (stubbed repo, no DB)" should {

    "return 200 with correct LicenceDetails" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/$MGD/XEM00000001335").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[LicenceDetails] mustBe getLicenceDetailsData("XEM00000001335")
    }

    "return 200 with defaulted values when there is no licence data" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/$MGD/XEM00000000000").futureValue

      response.status mustBe OK
      response.json.as[LicenceDetails] mustBe getLicenceDetailsData("XEM00000000000")
      (response.json \ "gamblingLicenceNo").as[String] mustBe ""
    }

    "normalise lowercase input" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$MGD/xem00000001335 ").futureValue
      response.status mustBe OK
      response.json.as[LicenceDetails] mustBe getLicenceDetailsData("XEM00000001335")
    }

    "trim whitespace around regNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$MGD/   XEM00000001335   ").futureValue
      response.status mustBe OK
      response.json.as[LicenceDetails] mustBe getLicenceDetailsData("XEM00000001335")
    }

    "return JSON content type for valid response" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$MGD/XEM00000001335").futureValue
      response.contentType mustBe "application/json"
    }

    "return 400 for partially valid regNumber (wrong length)" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$MGD/XEM123").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for invalid regime" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/BAD_REGIME/XEM00000001335").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for invalid regNumber format" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/$MGD/INVALID").futureValue
      response.status mustBe BAD_REQUEST
      (response.json \ "code").as[String] mustBe "INVALID_REG_NUMBER"
      (response.json \ "message").as[String] mustBe "regNumber has invalid format"
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()
      val response = get(s"$endpoint/$MGD/XEM00000001335").futureValue
      response.status mustBe UNAUTHORIZED
    }

    "return 404 for missing regNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$MGD/").futureValue
      response.status mustBe NOT_FOUND
    }

    "return 500 when stub simulates failure" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$MGD/XZM33333066666").futureValue
      response.status mustBe INTERNAL_SERVER_ERROR
      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
    }
  }
}
