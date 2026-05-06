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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.GamblingReturnsStubData.getReturnsSubmittedData
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.ReturnsSubmitted

import scala.concurrent.Future

class GamblingReturnsDataCacheRepositoryISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with GuiceOneAppPerSuite {

  class GamblingReturnsRdsStub extends GamblingReturnsDataSource {
    override def getReturnsSubmitted(regNumber: String, paginationStart: Int, paginationMaxRows: Int): Future[ReturnsSubmitted] =
      Future.successful(getReturnsSubmittedData(regNumber, paginationStart, paginationMaxRows))
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[GamblingReturnsDataSource].toInstance(new GamblingReturnsRdsStub))
    .build()

  private lazy val repository: GamblingReturnsDataSource = app.injector.instanceOf[GamblingReturnsDataSource]

  "getReturnsSubmitted (stubbed repository)" should {

    "return correct ReturnsSubmittedData" in {
      val result = repository.getReturnsSubmitted("XYZ00000000000", 1, 10).futureValue

      result mustBe getReturnsSubmittedData("XYZ00000000000")
    }

    "return correct data when paginationStart is 1" in {
      val result = repository.getReturnsSubmitted("XYZ00000000001", 1, 10).futureValue
      result mustBe getReturnsSubmittedData("XYZ00000000001")
    }

    "return consistent results across multiple calls" in {
      val result1 = repository.getReturnsSubmitted("XYZ00000000012", 1, 10).futureValue
      val result2 = repository.getReturnsSubmitted("XYZ00000000012", 1, 10).futureValue

      result1 mustBe result2
    }

    "handle different valid regNumbers independently" in {
      val result1 = repository.getReturnsSubmitted("XYZ00000000010", 1, 10).futureValue
      val result2 = repository.getReturnsSubmitted("XYZ00000000001", 1, 10).futureValue

      result1 must not be result2
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repository.getReturnsSubmitted("ERR00000000000", 1, 10).futureValue
      }

      exception.getMessage must include("Simulated downstream failure")
    }
  }
}
