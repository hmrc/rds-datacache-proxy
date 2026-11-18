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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers.manageClient

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.manageClient.AllClients
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.manageClient.{AllClientsRepository, GetClientsByUtrRepository}
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.manageClient.{AllClientsStubData, GetClientsByUtrStubData}
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.Future

class GetClientsByUtrControllerISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with ApplicationWithWiremock {

  class GetClientsByUtrRepositoryStub extends GetClientsByUtrRepository {

    override def getClientsByUtr(irAgentId: String, credentialId: String, utr: String): Future[AllClients] = {
      Future.successful(GetClientsByUtrStubData.getClientsByUtr(irAgentId, credentialId, utr)
      )
    }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[GetClientsByUtrRepository].toInstance(new GetClientsByUtrRepositoryStub())
      )
      .build()

  private final val endpoint = "/corporation-tax"

  "GET /corporation-tax/clients-by-utr" should {

    "return 200 and an empty client list" in {
      AuthStub.authorised()

      val response =
        get(s"$endpoint/clients-by-utr/IR1001/CR1001/1234567890").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"
      response.json.as[AllClients] mustBe GetClientsByUtrStubData.emptyClients
    }

    "return 200 and a single client" in {
      AuthStub.authorised()

      val response =
        get(s"$endpoint/clients-by-utr/IR2002/CR2002/1234567890").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"
      response.json.as[AllClients] mustBe GetClientsByUtrStubData.singleClient
    }

    "return 200 and multiple clients" in {
      AuthStub.authorised()

      val response =
        get(s"$endpoint/clients-by-utr/IR3003/CR3003/2345678901").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"
      response.json.as[AllClients] mustBe GetClientsByUtrStubData.multipleClients
    }

    "return 200 and multiple client name starting characters" in {
      AuthStub.authorised()

      val response =
        get(s"$endpoint/clients-by-utr/IR4004/CR4004/4567890123").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"
      response.json.as[AllClients] mustBe GetClientsByUtrStubData.multipleNameStartChars
    }

    "return 500 when stub fails" in {
      AuthStub.authorised()

      val response =
        get(s"$endpoint/clients-by-utr/IR9999/CR9999/9999999999").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()

      val response =
        get(s"$endpoint/clients-by-utr/IR1001/CR1001/1234567890").futureValue

      response.status mustBe UNAUTHORIZED
    }
  }
}
