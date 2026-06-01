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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{ActualRepayments, Regime, RepaymentsSummary}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.stub.RepaymentsStubData.*

import scala.concurrent.Future

class RepaymentsDataCacheRepositoryISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with GuiceOneAppPerSuite {

  class RepaymentsRdsStub extends RepaymentsDataSource {
    override def getRepaymentsSummary(regime: Regime, regNumber: String): Future[RepaymentsSummary] =
      Future.successful(getRepaymentsSummaryData(regNumber))

    override def getActualRepayments(regime: Regime, regNumber: String, pageStart: Int, pageMaxRows: Int): Future[ActualRepayments] =
      Future.successful(getActualRepaymentsData(regNumber))
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[RepaymentsDataSource].toInstance(new RepaymentsRdsStub))
    .build()

  private lazy val repository: RepaymentsDataSource = app.injector.instanceOf[RepaymentsDataSource]

  "getRepaymentsSummary (stubbed repository)" should {

    "return correct RepaymentsStubData" in {
      val result = repository.getRepaymentsSummary(Regime.MGD, "XYZ00000000000").futureValue

      result mustBe getRepaymentsSummaryData("XYZ00000000000")
    }

    "return correct data when values are 0" in {
      val result = repository.getRepaymentsSummary(Regime.MGD, "XYZ99999999999").futureValue
      result mustBe getRepaymentsSummaryData("XYZ99999999999")
    }

    "return consistent results across multiple calls" in {
      val result1 = repository.getRepaymentsSummary(Regime.MGD, "XYZ00000000000").futureValue
      val result2 = repository.getRepaymentsSummary(Regime.MGD, "XYZ00000000000").futureValue

      result1 mustBe result2
    }

    "handle different valid regNumbers independently" in {
      val result1 = repository.getRepaymentsSummary(Regime.MGD, "XYZ00000000000").futureValue
      val result2 = repository.getRepaymentsSummary(Regime.MGD, "XYZ99999999999").futureValue

      result1 must not be result2
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repository.getRepaymentsSummary(Regime.MGD, "ERR00000000000").futureValue
      }

      exception.getMessage must include("Simulated downstream failure")
    }
  }
}
