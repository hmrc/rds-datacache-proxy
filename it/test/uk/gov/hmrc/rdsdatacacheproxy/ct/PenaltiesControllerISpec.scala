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

package uk.gov.hmrc.rdsdatacacheproxy.ct

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{Penalties, PenaltyTransaction}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.PenaltiesDatacacheRepository
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.*
import scala.concurrent.Future

class PenaltiesControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class PenaltiesRdsStub extends PenaltiesDatacacheRepository {

    def getPenaltyTransactionList(taxRef: Long, accPeriod: Long): Future[List[PenaltyTransaction]] = {
      Future.successful(CorporationTaxStubData.getPenaltiesItems(taxRef))
    }

  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[PenaltiesDatacacheRepository].toInstance(new PenaltiesRdsStub())
      )
      .build()

  private final val endpoint = "/corporation-tax"

  "GET /corporation-tax/penalty-transactions" should {

    "return 200 with PenaltiesData list contains two items" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/penalty-transactions/1/3").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[Penalties] mustBe Penalties(penaltyTransactions = CorporationTaxStubData.penaltiesItems)
    }

    "return 200 with PenaltiesData empty list" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/penalty-transactions/8/3").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[Penalties] mustBe Penalties(penaltyTransactions = List[PenaltyTransaction]())
    }

    "return 500 when stub simulates failure" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/penalty-transactions/19/3").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()
      val response = get(s"$endpoint/penalty-transactions/8/3").futureValue
      response.status mustBe UNAUTHORIZED
    }
  }

}
