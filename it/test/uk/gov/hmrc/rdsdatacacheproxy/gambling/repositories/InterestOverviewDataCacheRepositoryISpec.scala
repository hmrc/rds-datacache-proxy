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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{InterestOverview, Regime}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.stub.InterestOverviewStubData.*

import scala.concurrent.Future

class InterestOverviewDataCacheRepositoryISpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite {

  class InterestOverviewRdsStub extends InterestOverviewDataSource {
    override def getInterestOverview(regime: Regime, regNumber: String): Future[InterestOverview] =
      Future.successful(getInterestOverviewData(regNumber))
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[InterestOverviewDataSource].toInstance(new InterestOverviewRdsStub))
    .build()

  private lazy val repository: InterestOverviewDataSource = app.injector.instanceOf[InterestOverviewDataSource]

  "getInterestOverview (stubbed repository)" should {

    "return correct InterestOverviewStubData" in {
      val result = repository.getInterestOverview(Regime.MGD, "XYZ00000000000").futureValue
      result mustBe getInterestOverviewData("XYZ00000000000")
    }

    "return correct data when values are 0" in {
      val result = repository.getInterestOverview(Regime.MGD, "XYZ99999999999").futureValue
      result mustBe getInterestOverviewData("XYZ99999999999")
    }

    "return consistent results across multiple calls" in {
      val result1 = repository.getInterestOverview(Regime.MGD, "XYZ00000000000").futureValue
      val result2 = repository.getInterestOverview(Regime.MGD, "XYZ00000000000").futureValue

      result1 mustBe result2
    }

    "handle different valid regNumbers independently" in {
      val result1 = repository.getInterestOverview(Regime.MGD, "XYZ00000000000").futureValue
      val result2 = repository.getInterestOverview(Regime.MGD, "XYZ99999999999").futureValue

      result1 must not be result2
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repository.getInterestOverview(Regime.MGD, "ERR00000000000").futureValue
      }

      exception.getMessage must include("Simulated downstream failure")
    }
  }
}
