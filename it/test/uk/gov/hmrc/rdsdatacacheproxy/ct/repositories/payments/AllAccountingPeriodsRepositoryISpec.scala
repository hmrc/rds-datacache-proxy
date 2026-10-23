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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.payments

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.AllAccountingPeriods
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.payments.AllAccountingPeriodsStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.ApplicationWithWiremock

import scala.concurrent.Future

class AllAccountingPeriodsRepositoryISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with ApplicationWithWiremock {

  class AllAccountingPeriodsRepositoryStub extends AllAccountingPeriodsRepository {

    override def getAccountingPeriodList(taxRef: Long): Future[ AllAccountingPeriods] =
      Future.successful(AllAccountingPeriodsStubData.getAccountingPeriodList(taxRef: Long))
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[AllAccountingPeriodsRepository].toInstance(new AllAccountingPeriodsRepositoryStub)
      )
      .build()

  private lazy val repo = app.injector.instanceOf[ AllAccountingPeriodsRepository]

  "getAccountingPeriodList" should {

    "return all accounting periods" in {

      val result = repo.getAccountingPeriodList(10L).futureValue

      result mustBe AllAccountingPeriodsStubData.allAccountingPeriods

    }

    "return multiple items of all accounting periods" in {

      val result = repo.getAccountingPeriodList(20L).futureValue

      result mustBe AllAccountingPeriodsStubData.multipleAllAccountingPeriods
    }

    "return empty list of all accounting periods" in {

      val result = repo.getAccountingPeriodList(30L).futureValue

      result mustBe AllAccountingPeriodsStubData.emptyAllAccountingPeriods

    }

    "return downstream failure from stub" in {
      val exception = intercept[RuntimeException] {

        repo.getAccountingPeriodList(200L).futureValue
      }

      exception.getMessage must include("Downstream error")
    }

  }

}
