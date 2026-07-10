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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.inject.bind
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.InterestCharges

import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.InterestChargesStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.ApplicationWithWiremock

import scala.concurrent.Future

class InterestChargeSummaryDataCacheRepositoryISpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with ApplicationWithWiremock {

  class InterestChargesSummaryDataCacheRepositoryStub extends InterestChargeSummaryDataCacheRepository {
    override def getInterestSummary(request: String): Future[InterestCharges] =
      Future {
        InterestChargesStubData.getInterestCharges(request)
      }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[InterestChargeSummaryDataCacheRepository].toInstance(new InterestChargesSummaryDataCacheRepositoryStub)
      )
      .build()

  private lazy val repository = app.injector.instanceOf[InterestChargeSummaryDataCacheRepository]

  "getInterestSummary" should {

    "return InterestCharges with containing 3 items" in {

      val result = repository.getInterestSummary("12").futureValue

      result mustBe InterestChargesStubData.interestCharges

    }

    "return empty InterestCharges" in {

      val result = repository.getInterestSummary("16").futureValue

      result mustBe InterestChargesStubData.emptyInterestCharges

    }

    "propagate downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repository.getInterestSummary("invalidTaxPayerReference").futureValue
      }

      exception.getMessage must include("Error from downstream")
    }

  }

}
