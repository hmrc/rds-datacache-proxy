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
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.{CreateCisAppealRequest, CreateCisAppealResponse, GetCisAppealablePenaltiesRequest, GetCisAppealablePenaltiesResponse, GetCisAppealablePenaltyRequest, GetCisAppealablePenaltyResponse}
import uk.gov.hmrc.rdsdatacacheproxy.pas.repositories.PasCisSource
import uk.gov.hmrc.rdsdatacacheproxy.pas.stub.PasCisStubData

import scala.concurrent.Future

class PasCisControllerISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with ApplicationWithWiremock {

  class PasCisStub extends PasCisSource {
    override def createCisAppeal(request: CreateCisAppealRequest): Future[CreateCisAppealResponse] =
      Future.successful(
        PasCisStubData.makeCreateCisAppealResponse(request)
      )

    override def getCisAppealablePenalties(request: GetCisAppealablePenaltiesRequest): Future[GetCisAppealablePenaltiesResponse] =
      Future.successful(
        PasCisStubData.makeGetCisAppealablePenaltiesResponse(request)
      )

    override def getCisAppealablePenalty(request: GetCisAppealablePenaltyRequest): Future[GetCisAppealablePenaltyResponse] =
      Future.successful(
        PasCisStubData.makeGetCisAppealablePenaltyResponse(request)
      )
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides{
        bind[PasCisSource].toInstance(new PasCisStub())
      }
      .build()


  private final val endpoint = "/pas"

  "POST /pas/cis/appeal" should {

    "return 201 with a valid CreateCisAppealResponse" in {
      AuthStub.authorised()

      val requestJson = Json.obj(
        "penaltyId" -> 1L
      )
      val response = post(s"$endpoint/cis/appeal", requestJson).futureValue

      response.status mustBe CREATED
      response.contentType mustBe "application/json"

      response.json.as[CreateCisAppealResponse] mustBe PasCisStubData.createCisAppealSuccessResponse
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

    "return 200 with a valid GetCisAppealablePenaltiesResponse when List contains 2 items" in {
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

      response.json.as[GetCisAppealablePenaltiesResponse] mustBe PasCisStubData.appealablePenalties
    }

    "return 200 with a valid GetCisAppealablePenaltiesResponse when List is empty" in {
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

      response.json.as[GetCisAppealablePenaltiesResponse] mustBe PasCisStubData.appealablePenaltiesEmpty
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

    "return 200 with a valid GetCisAppealablePenaltyResponse" in {
      AuthStub.authorised()

      val requestJson = Json.obj(
        "penaltyId" -> 1L,
        "hmrcOfficeNumber" -> "1234",
        "schemeReferenceNumber" -> "5678"
      )
      val response = post(s"$endpoint/cis/penalty", requestJson).futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[GetCisAppealablePenaltyResponse] mustBe PasCisStubData.getCisAppealablePenaltySuccessResponse
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
  }
}
