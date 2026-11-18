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
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.manageClient.GetClientsByUtrStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.ApplicationWithWiremock

import scala.concurrent.Future

class GetClientsByUtrRepositoryISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with ApplicationWithWiremock {

  class GetClientsByUtrRepositoryStub extends GetClientsByUtrRepository {

    override def getClientsByUtr(irAgentId: String, credentialId: String, utr: String): Future[AllClients] =
      Future.successful(
        GetClientsByUtrStubData.getClientsByUtr(irAgentId, credentialId, utr
        )
      )
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[GetClientsByUtrRepository].toInstance(new GetClientsByUtrRepositoryStub)
      )
      .build()

  private lazy val repo = app.injector.instanceOf[GetClientsByUtrRepository]

  "getClientsByUtr" should {

    "return an empty client list" in {

      val result = repo.getClientsByUtr(
        "IR1001",
        "CR1001",
        "1234567890"
      ).futureValue

      result mustBe GetClientsByUtrStubData.emptyClients
    }

    "return a single client" in {

      val result = repo.getClientsByUtr(
        "IR2002",
        "CR2002",
        "1234567890"
      ).futureValue

      result mustBe GetClientsByUtrStubData.singleClient
    }

    "return multiple clients" in {

      val result = repo.getClientsByUtr(
        "IR3003",
        "CR3003",
        "2345678901"
      ).futureValue

      result mustBe GetClientsByUtrStubData.multipleClients
    }

    "return multiple client name starting characters" in {

      val result = repo.getClientsByUtr(
        "IR4004",
        "CR4004",
        "4567890123"
      ).futureValue

      result mustBe GetClientsByUtrStubData.multipleNameStartChars
    }

    "return downstream failure from stub" in {

      val exception = intercept[RuntimeException] {

        repo.getClientsByUtr(
          "IR9999",
          "CR9999",
          "9999999999"
        ).futureValue
      }

      exception.getMessage must include("Downstream error")
    }
  }
}
