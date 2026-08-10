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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.ReallocationFromAccPeriod
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.ReallocationFromAccPeriodStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.ApplicationWithWiremock

import scala.concurrent.Future

class ReallocationFromAccPeriodRepositoryISpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with ApplicationWithWiremock {

  class ReallocationFromAccPeriodRepositoryStub extends ReallocationFromAccPeriodRepository {

    override def getReallocationFromAccPeriod(taxRef: Long, accPeriod: Long): Future[ReallocationFromAccPeriod] =
      Future.successful(
        ReallocationFromAccPeriodStubData.getReallocationFromAccPeriod(taxRef: Long, accPeriod: Long)
      )
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[ReallocationFromAccPeriodRepository].toInstance(new ReallocationFromAccPeriodRepositoryStub)
      )
      .build()

  private lazy val repo = app.injector.instanceOf[ReallocationFromAccPeriodRepository]

  "getReallocationFromAccPeriod" should {

    "return ReallocationFromAccPeriod containing 2 items" in {

      val result = repo.getReallocationFromAccPeriod(12L, 16L).futureValue

      result mustBe ReallocationFromAccPeriodStubData.reallocationFromAccPeriod
    }

    "return empty ReallocationFromAccPeriod" in {

      val result = repo.getReallocationFromAccPeriod(97L, 36L).futureValue

      result mustBe ReallocationFromAccPeriodStubData.emptyListReallocationFromAccPeriod

    }

    "return downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repo.getReallocationFromAccPeriod(9798L, 3786L).futureValue
      }

      exception.getMessage must include("Error from downstream")
    }

  }

}
