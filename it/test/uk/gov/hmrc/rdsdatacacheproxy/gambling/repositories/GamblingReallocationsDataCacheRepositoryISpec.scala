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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.GamblingReallocationsStubData.*
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{Reallocations, ReallocationsDetails, ReallocationsOut, Regime}

import scala.concurrent.Future

class GamblingReallocationsDataCacheRepositoryISpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite {

  class GamblingReallocationsRdsStub extends GamblingReallocationsDataSource {
    override def getReallocationsIn(regime: Regime, regNumber: String, paginationStart: Int, paginationMaxRows: Int): Future[Reallocations] =
      Future.successful(getReallocationsInData(regNumber, paginationStart, paginationMaxRows))

    override def getReallocationsOut(regime: Regime, regNumber: String, paginationStart: Int, paginationMaxRows: Int): Future[ReallocationsOut] =
      Future.successful(getReallocationsOutData(regNumber, paginationStart, paginationMaxRows))

    override def getReallocationsDetails(regime: Regime, regNumber: String): Future[ReallocationsDetails] =
      Future.successful(getReallocationsDetailData(regNumber))
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[GamblingReallocationsDataSource].toInstance(new GamblingReallocationsRdsStub))
    .build()

  private lazy val repository: GamblingReallocationsDataSource = app.injector.instanceOf[GamblingReallocationsDataSource]

  "getReallocationsIn (stubbed repository)" should {

    "return correct ReallocationsInData" in {
      val result = repository.getReallocationsIn(Regime.MGD, "XYZ00000000000", 1, 10).futureValue

      result mustBe getReallocationsInData("XYZ00000000000")
    }

    "return correct data when paginationStart is 1" in {
      val result = repository.getReallocationsIn(Regime.MGD, "XYZ00000000001", 1, 10).futureValue
      result mustBe getReallocationsInData("XYZ00000000001")
    }

    "return consistent results across multiple calls" in {
      val result1 = repository.getReallocationsIn(Regime.MGD, "XYZ00000000012", 1, 10).futureValue
      val result2 = repository.getReallocationsIn(Regime.MGD, "XYZ00000000012", 1, 10).futureValue

      result1 mustBe result2
    }

    "handle different valid regNumbers independently" in {
      val result1 = repository.getReallocationsIn(Regime.MGD, "XYZ00000000010", 1, 10).futureValue
      val result2 = repository.getReallocationsIn(Regime.MGD, "XYZ00000000001", 1, 10).futureValue

      result1 must not be result2
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repository.getReallocationsIn(Regime.MGD, "ERR00000000000", 1, 10).futureValue
      }

      exception.getMessage must include("Simulated downstream failure")
    }
  }

  "getReallocationsDetail (stubbed repository)" should {

    "return correct Reallocations Detail" in {
      val result = repository.getReallocationsDetails(Regime.MGD, "XYZ00000000000").futureValue

      result mustBe getReallocationsDetailData("XYZ00000000000")
    }

    "return consistent results across multiple calls" in {
      val result1 = repository.getReallocationsDetails(Regime.MGD, "XYZ00000000012").futureValue
      val result2 = repository.getReallocationsDetails(Regime.MGD, "XYZ00000000012").futureValue

      result1 mustBe result2
    }

    "handle different valid regNumbers independently" in {
      val result1 = repository.getReallocationsDetails(Regime.MGD, "XYZ00000000000").futureValue
      val result2 = repository.getReallocationsDetails(Regime.MGD, "XYZ00000000001").futureValue

      result1 must not be result2
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repository.getReallocationsDetails(Regime.MGD, "ERR00000000000").futureValue
      }

      exception.getMessage must include("Simulated downstream failure")
    }
  }

}
