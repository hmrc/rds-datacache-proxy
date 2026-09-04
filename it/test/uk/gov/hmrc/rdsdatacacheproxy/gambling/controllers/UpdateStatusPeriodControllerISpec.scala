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
import play.api.libs.json.Json
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.UpdateStatusPeriodDataSource
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.Future

class UpdateStatusPeriodControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class UpdateStatusPeriodRdsStub extends UpdateStatusPeriodDataSource {
    override def updateStatusPeriod(regNumber: String, consecNo: Int, status: Int): Future[Unit] =
      regNumber match {
        case "XZM33333066666" => Future.failed(new RuntimeException("simulated failure"))
        case _                => Future.successful(())
      }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[UpdateStatusPeriodDataSource].toInstance(new UpdateStatusPeriodRdsStub)
      )
      .build()

  private final val endpoint = "/gambling/update-status-period"
  private final val MGD = "mgd"

  "PUT /gambling/update-status-period (stubbed repo, no DB)" should {

    "return 204 when the update succeeds" in {
      AuthStub.authorised()

      val response = put(s"$endpoint/$MGD/XGM00003122200/3", Json.obj("status" -> 1)).futureValue

      response.status mustBe NO_CONTENT
    }

    "return 400 for invalid status" in {
      AuthStub.authorised()

      val response = put(s"$endpoint/$MGD/XGM00003122200/3", Json.obj("status" -> 2)).futureValue

      response.status mustBe BAD_REQUEST
      (response.json \ "code").as[String] mustBe "INVALID_STATUS"
    }

    "return 400 for invalid regNumber format" in {
      AuthStub.authorised()

      val response = put(s"$endpoint/$MGD/INVALID/3", Json.obj("status" -> 1)).futureValue

      response.status mustBe BAD_REQUEST
      (response.json \ "code").as[String] mustBe "INVALID_REG_NUMBER"
    }

    "return 400 for invalid regime" in {
      AuthStub.authorised()

      val response = put(s"$endpoint/BAD_REGIME/XGM00003122200/3", Json.obj("status" -> 1)).futureValue

      response.status mustBe BAD_REQUEST
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()

      val response = put(s"$endpoint/$MGD/XGM00003122200/3", Json.obj("status" -> 1)).futureValue

      response.status mustBe UNAUTHORIZED
    }

    "return 500 when stub simulates failure" in {
      AuthStub.authorised()

      val response = put(s"$endpoint/$MGD/XZM33333066666/3", Json.obj("status" -> 1)).futureValue

      response.status mustBe INTERNAL_SERVER_ERROR
      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
    }
  }
}
