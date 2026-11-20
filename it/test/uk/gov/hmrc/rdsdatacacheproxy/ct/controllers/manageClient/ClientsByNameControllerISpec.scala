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
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.manageClient.ClientsByNameRepository
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.manageClient.AllClientsStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.Future

class ClientsByNameControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class ClientsByNameRepositoryStub extends ClientsByNameRepository {
    override def getClientsByName(irAgentId: String, credentialId: String, name: String, start: Int, count: Int, sort: Int, order: String): Future[AllClients] = {
      Future.successful(AllClientsStubData.getClientsByName(irAgentId: String, credentialId: String, name: String, start: Int, count: Int, sort: Int, order: String))
    }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[ClientsByNameRepository].toInstance(new ClientsByNameRepositoryStub())
      )
      .build()

  private final val endpoint = "/corporation-tax"

  "GET /corporation-tax/clients-by-name" should {

    "return 200 and clients by name" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/clients-by-name/irAgentId1/credentialId1/name1").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[AllClients] mustBe AllClientsStubData.allClients
    }

    "return 200 and multiple items of client list for client by name" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/clients-by-name/irAgentId2/credentialId2/name2").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[AllClients] mustBe AllClientsStubData.multipleClientListAllClients
    }

    "return 200 and multiple items of client name chars for client by name" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/clients-by-name/irAgentId3/credentialId3/name3").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[AllClients] mustBe AllClientsStubData.multipleItemsClientNameAllClients
    }

    "return 500 when stub fails" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/clients-by-name/irAgentIdNA/credentialIdNA/nameNA").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()

      val response = get(s"$endpoint/clients-by-name/irAgentIdNON/credentialIdNON/nameNON").futureValue

      response.status mustBe UNAUTHORIZED
    }
  }

}
