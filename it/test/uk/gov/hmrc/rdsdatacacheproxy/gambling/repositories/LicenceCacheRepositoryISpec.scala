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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{LicenceDetails, Regime}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.stub.LicenceStubData.getLicenceDetailsData

import scala.concurrent.Future

class LicenceCacheRepositoryISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with GuiceOneAppPerSuite {

  class LicenceRdsStub extends LicenceDataSource {
    override def getLicenceDetails(regime: Regime, regNumber: String): Future[LicenceDetails] =
      Future.successful(getLicenceDetailsData(regNumber))
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[LicenceDataSource].toInstance(new LicenceRdsStub))
    .build()

  private lazy val repository: LicenceDataSource = app.injector.instanceOf[LicenceDataSource]

  "getLicenceDetails (stubbed repository)" should {

    "return correct LicenceDetailsData" in {
      val result = repository.getLicenceDetails(Regime.MGD, "XEM00000001335").futureValue

      result mustBe getLicenceDetailsData("XEM00000001335")
    }

    "return consistent results across multiple calls" in {
      val result1 = repository.getLicenceDetails(Regime.MGD, "XEM00000001335").futureValue
      val result2 = repository.getLicenceDetails(Regime.MGD, "XEM00000001335").futureValue

      result1 mustBe result2
    }

    "handle different valid regNumbers independently" in {
      val result1 = repository.getLicenceDetails(Regime.MGD, "XEM00000001335").futureValue
      val result2 = repository.getLicenceDetails(Regime.MGD, "XEM00000000000").futureValue

      result1 must not be result2
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repository.getLicenceDetails(Regime.MGD, "XZM33333066666").futureValue
      }

      exception.getMessage must include("Simulated downstream failure")
    }
  }
}
