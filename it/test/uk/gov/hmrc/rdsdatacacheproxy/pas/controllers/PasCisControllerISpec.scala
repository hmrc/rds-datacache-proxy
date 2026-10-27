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

package uk.gov.hmrc.rdsdatacacheproxy.pas.controllers

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, inject}
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.*
import uk.gov.hmrc.rdsdatacacheproxy.pas.repositories.PasCisSource
import uk.gov.hmrc.rdsdatacacheproxy.pas.stub.PasStubData

import scala.concurrent.Future

class PasCisControllerISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with ApplicationWithWiremock {

  class PasCisStub extends PasCisSource {
    override def createCisAppeal(request: CreateAppealRequest): Future[CreateAppealResponse] =
      Future.successful(
        PasStubData.makeCreateAppealResponse(request)
      )

    override def getCisAppealablePenalties(request: GetAppealablePenaltiesRequest): Future[GetAppealablePenaltiesResponse] =
      Future.successful(
        PasStubData.makeGetAppealablePenaltiesResponse(request)
      )

    override def getCisAppealablePenalty(request: GetAppealablePenaltyRequest): Future[GetAppealablePenaltyResponse] =
      Future.successful(
        PasStubData.makeGetAppealablePenaltyResponse(request)
      )

    override def getCisPenaltyAllowed(request: GetPenaltyAllowedRequest): Future[GetPenaltyAllowedResponse] =
      Future.successful(
        PasStubData.makeGetPenaltyAllowedResponse(request)
      )
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides {
        bind[PasCisSource].toInstance(new PasCisStub())
      }
      .build()


  private final val endpoint = "/pas"

  "POST /pas/cis/appeal" should {

    "return 201 with a valid CreateAppealResponse" in {
      AuthStub.authorised()

      val requestJson = Json.obj(
        "penaltyId" -> 1L
      )
      val response = post(s"$endpoint/cis/appeal", requestJson).futureValue

      response.status mustBe CREATED
      response.contentType mustBe "application/json"

      response.json.as[CreateAppealResponse] mustBe PasStubData.createAppealSuccessResponse
    }

    "return 500 when stub simulates failure" in {

      AuthStub.authorised()

      val requestJson = Json.obj(
        "penaltyId" -> 500L
      )
      val response = post(s"$endpoint/cis/appeal", requestJson).futureValue

      response.status mustBe INTERNAL_SERVER_ERROR

    }

    "return 401 when unauthorised" in {

      AuthStub.unauthorised()

      val requestJson = Json.obj(
        "penaltyId" -> 1L
      )
      val response = post(s"$endpoint/cis/appeal", requestJson).futureValue

      response.status mustBe UNAUTHORIZED
    }
  }

  "POST /pas/cis/penalties" should {

    "return 200 with a valid GetAppealablePenaltiesResponse when List contains 2 items" in {
      AuthStub.authorised()

      val requestJson = Json.obj(
        "hmrcOfficeNumber" -> "123",
        "schemeReferenceNumber" -> "AB456",
        "startRow" -> 1,
        "endRow" -> 10
      )

      val response = post(s"$endpoint/cis/penalties", requestJson).futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[GetAppealablePenaltiesResponse] mustBe PasStubData.appealablePenalties
    }

    "return 200 with a valid GetAppealablePenaltiesResponse when List is empty" in {
      AuthStub.authorised()

      val requestJson = Json.obj(
        "hmrcOfficeNumber" -> "456",
        "schemeReferenceNumber" -> "AB456",
        "startRow" -> 1,
        "endRow" -> 10
      )

      val response = post(s"$endpoint/cis/penalties", requestJson).futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[GetAppealablePenaltiesResponse] mustBe PasStubData.appealablePenaltiesEmpty
    }

    "return 500 when stub simulates failure" in {

      AuthStub.authorised()

      val requestJson = Json.obj(
        "hmrcOfficeNumber" -> "789",
        "schemeReferenceNumber" -> "AB456",
        "startRow" -> 1,
        "endRow" -> 10
      )

      val response = post(s"$endpoint/cis/penalties", requestJson).futureValue

      response.status mustBe INTERNAL_SERVER_ERROR
    }

    "return 401 when unauthorised" in {

      AuthStub.unauthorised()

      val requestJson = Json.obj(
        "hmrcOfficeNumber" -> "123",
        "schemeReferenceNumber" -> "AB456",
        "startRow" -> 1,
        "endRow" -> 10
      )

      val response = post(s"$endpoint/cis/penalties", requestJson).futureValue

      response.status mustBe UNAUTHORIZED
    }
  }

  "POST /pas/cis/penalty" should {

    "return 200 with a valid GetAppealablePenaltyResponse" in {
      AuthStub.authorised()

      val requestJson = Json.obj(
        "penaltyId" -> 1L,
        "hmrcOfficeNumber" -> "1234",
        "schemeReferenceNumber" -> "5678"
      )
      val response = post(s"$endpoint/cis/penalty", requestJson).futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[GetAppealablePenaltyResponse] mustBe PasStubData.getAppealablePenaltySuccessResponse
    }

    "return 500 when stub simulates failure" in {

      AuthStub.authorised()

      val requestJson = Json.obj(
        "penaltyId" -> 500L,
        "hmrcOfficeNumber" -> "1234",
        "schemeReferenceNumber" -> "5678"
      )
      val response = post(s"$endpoint/cis/penalty", requestJson).futureValue

      response.status mustBe INTERNAL_SERVER_ERROR

    }

    "return 401 when unauthorised" in {

      AuthStub.unauthorised()

      val requestJson = Json.obj(
        "penaltyId" -> 1L,
        "hmrcOfficeNumber" -> "1234",
        "schemeReferenceNumber" -> "5678"
      )
      val response = post(s"$endpoint/cis/penalty", requestJson).futureValue

      response.status mustBe UNAUTHORIZED
    }
    
    "POST /cis/penalties/access-allowed" should {

      "return 200 with a valid GetPenaltyAllowedResponse" in {
        AuthStub.authorised()

        val requestJson = Json.obj(
          "districtNumber" -> 1L,
          "employerReference" -> "hdz",
          "uniqueId" -> 1L
        )
        val response = post(s"$endpoint/cis/penalties/access-allowed", requestJson).futureValue

        response.status mustBe OK
        response.contentType mustBe "application/json"

        response.json.as[GetPenaltyAllowedResponse] mustBe PasStubData.getPenaltyAllowedSuccessResponse
      }

      "return 500 when stub simulates failure" in {

        AuthStub.authorised()

        val requestJson = Json.obj(
          "districtNumber" -> 1L,
          "employerReference" -> "1234",
          "uniqueId" -> 2L
        )
        val response = post(s"$endpoint/cis/penalties/access-allowed", requestJson).futureValue

        response.status mustBe INTERNAL_SERVER_ERROR

      }

      "return 401 when unauthorised" in {

        AuthStub.unauthorised()

        val requestJson = Json.obj(
          "districtNumber" -> 1L,
          "employerReference" -> "1234",
          "uniqueId" -> 1L
        )
        val response = post(s"$endpoint/cis/penalties/access-allowed", requestJson).futureValue

        response.status mustBe UNAUTHORIZED
      }
    }
  }
}