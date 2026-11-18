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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.manageClient

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.manageClient.AllClients
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.manageClient.AllClientsStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.ApplicationWithWiremock

import scala.concurrent.Future

class AllClientsRepositoryISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with ApplicationWithWiremock {

  class AllClientsRepositoryStub extends AllClientsRepository {

    override def getAllClients(irAgentId: String, credentialId: String, start: Int, count: Int, sort: Int, order: String): Future[AllClients] =
      Future.successful(AllClientsStubData.getAllClients(irAgentId: String, credentialId: String, start: Int, count: Int, sort: Int, order: String))
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[AllClientsRepository].toInstance(new AllClientsRepositoryStub)
      )
      .build()

  private lazy val repo = app.injector.instanceOf[AllClientsRepository]

  "getAllClients" should {

    "return all clients" in {

      val result = repo.getAllClients("irAgentId1", "credentialId1", 0, 30, 0, "ASC").futureValue

      result mustBe AllClientsStubData.allClients

    }

    "return multiple items of client list in all clients" in {

      val result = repo.getAllClients("irAgentId2", "credentialId2", 0, 30, 0, "ASC").futureValue

      result mustBe AllClientsStubData.multipleClientListAllClients
    }

    "return multiple items of client name chars in all clients" in {

      val result = repo.getAllClients("irAgentId3", "credentialId3", 0, 30, 0, "ASC").futureValue

      result mustBe AllClientsStubData.multipleItemsClientNameAllClients

    }

    "return downstream failure from stub" in {
      val exception = intercept[RuntimeException] {

        repo.getAllClients("irAgentIdNA", "credentialIdNA", 0, 30, 0, "ASC").futureValue
      }

      exception.getMessage must include("Downstream error")
    }

  }

}
