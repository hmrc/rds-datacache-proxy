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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.SubmittedReturns
import uk.gov.hmrc.rdsdatacacheproxy.gambling.stub.SubmittedReturnsStubData.{DEFAULT_ORDER_BY, DEFAULT_SORT_BY, getSubmittedReturnsData}

import scala.concurrent.Future

class SubmittedReturnsDataCacheRepositoryISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with GuiceOneAppPerSuite {

  class SubmittedReturnsRdsStub extends SubmittedReturnsDataSource {
    override def getSubmittedReturns(regNumber: String, sortBy: Int, orderBy: String): Future[SubmittedReturns] =
      Future.successful(getSubmittedReturnsData(regNumber, sortBy, orderBy))
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[SubmittedReturnsDataSource].toInstance(new SubmittedReturnsRdsStub))
    .build()

  private lazy val repository: SubmittedReturnsDataSource = app.injector.instanceOf[SubmittedReturnsDataSource]

  "getSubmittedReturns (stubbed repository)" should {

    "return correct SubmittedReturnsData" in {
      val result = repository.getSubmittedReturns("XYZ00000000000", DEFAULT_SORT_BY, DEFAULT_ORDER_BY).futureValue

      result mustBe getSubmittedReturnsData("XYZ00000000000", DEFAULT_SORT_BY, DEFAULT_ORDER_BY)
    }

    "return correct data when paginationStart is 1" in {
      val result = repository.getSubmittedReturns("XYZ00000000001", DEFAULT_SORT_BY, DEFAULT_ORDER_BY).futureValue
      result mustBe getSubmittedReturnsData("XYZ00000000001", DEFAULT_SORT_BY, DEFAULT_ORDER_BY)
    }

    "return consistent results across multiple calls" in {
      val result1 = repository.getSubmittedReturns("XYZ00000000012", DEFAULT_SORT_BY, DEFAULT_ORDER_BY).futureValue
      val result2 = repository.getSubmittedReturns("XYZ00000000012", DEFAULT_SORT_BY, DEFAULT_ORDER_BY).futureValue

      result1 mustBe result2
    }

    "handle different valid regNumbers independently" in {
      val result1 = repository.getSubmittedReturns("XYZ00000000010", DEFAULT_SORT_BY, DEFAULT_ORDER_BY).futureValue
      val result2 = repository.getSubmittedReturns("XYZ00000000001", DEFAULT_SORT_BY, DEFAULT_ORDER_BY).futureValue

      result1 must not be result2
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repository.getSubmittedReturns("XVM33333333333", DEFAULT_SORT_BY, DEFAULT_ORDER_BY).futureValue
      }

      exception.getMessage must include("Simulated downstream failure")
    }
  }
}
