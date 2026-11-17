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
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.manageClient.GetClientsByCrnStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.ApplicationWithWiremock

import scala.concurrent.Future

class GetClientsByCrnRepositoryISpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with ApplicationWithWiremock {

  class GetClientsByCrnStub extends GetClientsByCrnRepository {
    override def getClientByCrn(irAgentReference: String, credentialId: String, companyRegNo: String): Future[AllClients] =
      Future.successful(
        GetClientsByCrnStubData.getClientByCrn(irAgentReference, credentialId, companyRegNo)
      )
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[GetClientsByCrnRepository].toInstance(new GetClientsByCrnStub)
      )
      .build()

  private lazy val repo = app.injector.instanceOf[GetClientsByCrnRepository]

  "getClientByCrn" should {

    "return AllClients containing multiple items" in {

      val result = repo.getClientByCrn("134", "73", "YT3").futureValue

      result mustBe GetClientsByCrnStubData.multipleObjectsResponse
    }
    "return AllClients containing single item" in {

      val result = repo.getClientByCrn("1234", "783", "Y73").futureValue

      result mustBe GetClientsByCrnStubData.singleObjectResponse
    }

    "return empty AllClients" in {

      val result = repo.getClientByCrn("1234", "78783", "YT73").futureValue

      result mustBe GetClientsByCrnStubData.emptyResponse

    }

    "return downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repo.getClientByCrn("12", "78", "Y").futureValue
      }

      exception.getMessage must include("Downstream error")
    }

  }

}
