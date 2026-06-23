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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.SubmittedReturnSingle
import uk.gov.hmrc.rdsdatacacheproxy.gambling.stub.SubmittedReturnSingleStubData.getSubmittedReturnSingleData

import scala.concurrent.Future

class SubmittedReturnSingleDataCacheRepositoryISpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite {

  class SubmittedReturnSingleRdsStub extends SubmittedReturnSingleDataSource {
    override def getSubmittedReturnSingle(regNumber: String, consecNo: Int): Future[SubmittedReturnSingle] =
      Future.successful(getSubmittedReturnSingleData(regNumber, consecNo))
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[SubmittedReturnSingleDataSource].toInstance(new SubmittedReturnSingleRdsStub))
    .build()

  private lazy val repository: SubmittedReturnSingleDataSource = app.injector.instanceOf[SubmittedReturnSingleDataSource]

  "getSubmittedReturnSingle (stubbed repository)" should {

    "return correct SubmittedReturnSingleData" in {
      val result = repository.getSubmittedReturnSingle("XYZ00000000001", 23).futureValue

      result mustBe getSubmittedReturnSingleData("XYZ00000000001", 23)
    }

    "return consistent results across multiple calls" in {
      val result1 = repository.getSubmittedReturnSingle("XYZ00000000012", 23).futureValue
      val result2 = repository.getSubmittedReturnSingle("XYZ00000000012", 23).futureValue

      result1 mustBe result2
    }

    "handle different valid regNumbers independently" in {
      val result1 = repository.getSubmittedReturnSingle("XYZ00000000010", 23).futureValue
      val result2 = repository.getSubmittedReturnSingle("XYZ00000000001", 23).futureValue

      result1 must not be result2
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repository.getSubmittedReturnSingle("ERR00000000000", 23).futureValue
      }

      exception.getMessage must include("Simulated downstream failure")
    }
  }
}
