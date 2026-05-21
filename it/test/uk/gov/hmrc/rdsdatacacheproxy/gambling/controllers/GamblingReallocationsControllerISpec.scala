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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.GamblingReallocationsStubData
import uk.gov.hmrc.rdsdatacacheproxy.gambling.GamblingReallocationsStubData.{getReallocationsInData, getReallocationsOutData}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.Reallocations
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{ReallocationsOut, Regime}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.GamblingReallocationsDataSource
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GamblingReallocationsControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class GamblingReallocationsRdsStub extends GamblingReallocationsDataSource {
    override def getReallocationsIn(regime: Regime, regNumber: String, pageNo: Int, pageSize: Int): Future[Reallocations] =
      Future(GamblingReallocationsStubData.getReallocationsInData(regNumber, pageNo, pageSize))

    override def getReallocationsOut(regime: Regime, regNumber: String, pageNo: Int, pageSize: Int): Future[ReallocationsOut] =
      Future(GamblingReallocationsStubData.getReallocationsOutData(regNumber, pageNo, pageSize))
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[GamblingReallocationsDataSource].toInstance(new GamblingReallocationsRdsStub)
      )
      .build()

  private final val inEndpoint = "/gambling/reallocations-in"
  private final val outEndpoint = "/gambling/reallocations-out"
  private final val GBD = "gbd"
  private val MGD = "mgd"

  "GET /gambling/reallocations-in (stubbed repo, no DB)" should {

    "return 200 with correct getReallocationsInData" in {
      AuthStub.authorised()

      val response = get(s"$inEndpoint/$GBD/XYZ00000000000?pageNo=1&pageSize=10").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[Reallocations] mustBe getReallocationsInData("XYZ00000000000")
    }

    "return 200 with correct getReallocationsInData when pageNo & pageSize NOT provided" in {
      AuthStub.authorised()

      val response = get(s"$inEndpoint/$GBD/XYZ99999999999").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[Reallocations] mustBe getReallocationsInData("XYZ99999999999")
    }

    "normalise lowercase input" in {
      AuthStub.authorised()
      val response = get(s"$inEndpoint/$GBD/xyz00000000012 ").futureValue
      response.status mustBe OK
      response.json.as[Reallocations] mustBe getReallocationsInData("XYZ00000000012")
    }

    "trim whitespace around regNumber" in {
      AuthStub.authorised()
      val response = get(s"$inEndpoint/$GBD/   XYZ00000000012   ").futureValue
      response.status mustBe OK
      response.json.as[Reallocations] mustBe getReallocationsInData("XYZ00000000012")
    }

    "return consistent results across multiple calls" in {
      AuthStub.authorised()
      val res1 = get(s"$inEndpoint/$GBD/XYZ00000000012").futureValue
      val res2 = get(s"$inEndpoint/$GBD/XYZ00000000012").futureValue
      res1.json mustBe res2.json
    }

    "return JSON content type for valid response" in {
      AuthStub.authorised()
      val response = get(s"$inEndpoint/$GBD/XYZ00000000012").futureValue
      response.contentType mustBe "application/json"
    }

    "return 400 for partially valid regNumber (wrong length)" in {
      AuthStub.authorised()
      val response = get(s"$inEndpoint/$GBD/XYZ123?pageNo=1&pageSize=10").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for invalid regime)" in {
      AuthStub.authorised()
      val response = get(s"$inEndpoint/BAD_REGIME/XYZ00000000012?pageNo=1&pageSize=10").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for regNumber with special characters" in {
      AuthStub.authorised()
      val response = get(s"$inEndpoint/$GBD/XYZ00000@00000").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for invalid regNumber format" in {
      AuthStub.authorised()

      val response = get(s"$inEndpoint/$GBD/INVALID").futureValue
      response.status mustBe BAD_REQUEST
      (response.json \ "code").as[String] mustBe "INVALID_REG_NUMBER"
      (response.json \ "message").as[String] mustBe "regNumber has invalid format"
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()
      val response = get(s"$inEndpoint/$GBD/XYZ00000000000").futureValue
      response.status mustBe UNAUTHORIZED
    }

    "return 404 for missing regNumber" in {
      AuthStub.authorised()
      val response = get(s"$inEndpoint/$GBD/").futureValue
      response.status mustBe NOT_FOUND
    }

    "return 404 for whitespace-only regNumber" in {
      AuthStub.authorised()
      val response = get(s"$inEndpoint/$GBD/   ").futureValue
      response.status mustBe NOT_FOUND
    }

    "return 500 when stub simulates failure" in {
      AuthStub.authorised()
      val response = get(s"$inEndpoint/$GBD/ERR00000000000").futureValue
      response.status mustBe INTERNAL_SERVER_ERROR
      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
    }

    "return correct error structure for 500 response" in {
      AuthStub.authorised()
      val response = get(s"$inEndpoint/$GBD/ERR00000000000").futureValue
      response.status mustBe INTERNAL_SERVER_ERROR
      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
      (response.json \ "message").as[String] mustBe "Unexpected error occurred"
    }
  }

  "GET /gambling/reallocations-out (stubbed repo, no DB)" should {

    "return 200 with correct getReallocationsOutData" in {
      AuthStub.authorised()

      val response = get(s"$outEndpoint/$MGD/XYZ00000000000?pageNo=1&pageSize=10").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[ReallocationsOut] mustBe getReallocationsOutData("XYZ00000000000")
    }

    "return 200 with correct getReallocationsOutData when pageNo & pageSize NOT provided" in {
      AuthStub.authorised()

      val response = get(s"$outEndpoint/$MGD/XYZ00000000000").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[ReallocationsOut] mustBe getReallocationsOutData("XYZ00000000000")
    }

    "normalise lowercase input" in {
      AuthStub.authorised()
      val response = get(s"$outEndpoint/$MGD/xyz00000000012 ").futureValue
      response.status mustBe OK
      response.json.as[ReallocationsOut] mustBe getReallocationsOutData("XYZ00000000012")
    }

    "trim whitespace around regNumber" in {
      AuthStub.authorised()
      val response = get(s"$outEndpoint/$MGD/   XYZ00000000012   ").futureValue
      response.status mustBe OK
      response.json.as[ReallocationsOut] mustBe getReallocationsOutData("XYZ00000000012")
    }

    "return consistent results across multiple calls" in {
      AuthStub.authorised()
      val res1 = get(s"$outEndpoint/$MGD/XYZ00000000012").futureValue
      val res2 = get(s"$outEndpoint/$MGD/XYZ00000000012").futureValue
      res1.json mustBe res2.json
    }

    "return JSON content type for valid response" in {
      AuthStub.authorised()
      val response = get(s"$outEndpoint/$MGD/XYZ00000000012").futureValue
      response.contentType mustBe "application/json"
    }

    "return 400 for partially valid regNumber (wrong length)" in {
      AuthStub.authorised()
      val response = get(s"$outEndpoint/$MGD/XYZ123?pageNo=1&pageSize=10").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for invalid regime)" in {
      AuthStub.authorised()
      val response = get(s"$outEndpoint/BAD_REGIME/XYZ00000000012?pageNo=1&pageSize=10").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for regNumber with special characters" in {
      AuthStub.authorised()
      val response = get(s"$outEndpoint/$MGD/XYZ00000@00000").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for invalid regNumber format" in {
      AuthStub.authorised()

      val response = get(s"$outEndpoint/$MGD/INVALID").futureValue
      response.status mustBe BAD_REQUEST
      (response.json \ "code").as[String] mustBe "INVALID_REG_NUMBER"
      (response.json \ "message").as[String] mustBe "regNumber has invalid format"
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()
      val response = get(s"$outEndpoint/$MGD/XYZ00000000000").futureValue
      response.status mustBe UNAUTHORIZED
    }

    "return 404 for missing regNumber" in {
      AuthStub.authorised()
      val response = get(s"$outEndpoint/$MGD/").futureValue
      response.status mustBe NOT_FOUND
    }

    "return 404 for whitespace-only regNumber" in {
      AuthStub.authorised()
      val response = get(s"$outEndpoint/$MGD/   ").futureValue
      response.status mustBe NOT_FOUND
    }

    "return 500 when stub simulates failure" in {
      AuthStub.authorised()
      val response = get(s"$outEndpoint/$MGD/ERR00000000000").futureValue
      response.status mustBe INTERNAL_SERVER_ERROR
      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
    }

    "return correct error structure for 500 response" in {
      AuthStub.authorised()
      val response = get(s"$outEndpoint/$MGD/ERR00000000000").futureValue
      response.status mustBe INTERNAL_SERVER_ERROR
      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
      (response.json \ "message").as[String] mustBe "Unexpected error occurred"
    }
  }
}
