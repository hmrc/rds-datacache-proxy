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
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, inject}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.InterestCharges
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.InterestChargeSummaryDataCacheRepository
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.InterestChargesStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class InterestChargeControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class InterestChargesStub extends InterestChargeSummaryDataCacheRepository {
    override def getInterestSummary(request: String): Future[InterestCharges] =
      Future {
        InterestChargesStubData.getInterestCharges(request)
      }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[InterestChargeSummaryDataCacheRepository].toInstance(new InterestChargesStub)
      )
      .build()


   private val endpoint = "/corporation-tax"


  "GET /corporation-tax/interest-charge-summary" should {

    "return 200 with InterestCharges list contains two items" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/interest-charge-summary/12").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[InterestCharges] mustBe InterestChargesStubData.interestCharges
    }

    "return 200 with InterestCharges empty list" in {

      AuthStub.authorised()

      val response = get(s"$endpoint/interest-charge-summary/31").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[InterestCharges] mustBe InterestChargesStubData.emptyInterestCharges

    }

    "return 500 when stub simulates failure" in {

      AuthStub.authorised()
      
      val invalidTaxPayerReference = "invalidTaxPayerReference"

      val response = get(s"$endpoint/interest-charge-summary/$invalidTaxPayerReference").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR

    }

    "return 401 when unauthorised" in {

      AuthStub.unauthorised()

      val response = get(s"$endpoint/interest-charge-summary/3").futureValue

      response.status mustBe UNAUTHORIZED
    }
  }


}
