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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED, BAD_REQUEST}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, inject}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.ClientDownloadStatus
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.ClientListDownloadStatusRepository
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.ClientListDownloadStatusStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ClientListDownloadStatusControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class ClientListDownloadStatusStub extends ClientListDownloadStatusRepository {
    override def getClientListDownloadStatus(credentialId: String,
                                             serviceName: String,
                                             gracePeriod: Int): Future[ClientDownloadStatus] =
      Future {
        ClientListDownloadStatusStubData.getClientListDownloadStatus(credentialId, serviceName, gracePeriod)
      }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[ClientListDownloadStatusRepository].toInstance(new ClientListDownloadStatusStub)
      )
      .build()


   private final val endpoint = "/corporation-tax/client-list-status"


  "GET /corporation-tax/client-list-status" should {

    "return 200 with ClientListDownloadStatus initiate download" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/1?serviceName=CT&gracePeriod=14400").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[ClientDownloadStatus] mustBe ClientDownloadStatus(ClientListDownloadStatusStubData.initiateDownload)
    }

    "return 200 with ClientListDownloadStatus succeeded with default params" in {

      AuthStub.authorised()

      val response = get(s"$endpoint/3").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[ClientDownloadStatus] mustBe ClientDownloadStatus(ClientListDownloadStatusStubData.succeeded)

    }

    "return 500 when stub simulates failure" in {

      AuthStub.authorised()

      val response = get(s"$endpoint/99?serviceName=CT&gracePeriod=14400").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR

    }

    "return 401 when unauthorised" in {

      AuthStub.unauthorised()

      val response = get(s"$endpoint/3?serviceName=CT&gracePeriod=14400").futureValue

      response.status mustBe UNAUTHORIZED
    }

  }


}
