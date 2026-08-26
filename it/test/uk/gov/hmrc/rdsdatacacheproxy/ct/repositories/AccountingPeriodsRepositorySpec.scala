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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{RdsAccountingPeriod, ReallocationFromAccPeriod}
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.{AccountingPeriodsStubData, ReallocationFromAccPeriodStubData}
import uk.gov.hmrc.rdsdatacacheproxy.itutil.ApplicationWithWiremock

import scala.concurrent.Future

class AccountingPeriodsRepositorySpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with ApplicationWithWiremock {

  class AccountingPeriodRepositoryStub extends AccountingPeriodsRepository {

    override def getAccountingPeriods(taxRef: Long): Future[RdsAccountingPeriod] =
      Future.successful(
        AccountingPeriodsStubData.getAccountingPeriods(taxRef: Long)
      )
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[AccountingPeriodsRepository].toInstance(new AccountingPeriodRepositoryStub)
      )
      .build()

  private lazy val repo = app.injector.instanceOf[AccountingPeriodsRepository]

  "getAccountingPeriods" should {

    "return RdsAccountingPeriod containing multiple items" in {

      val result = repo.getAccountingPeriods(20L).futureValue

      result mustBe AccountingPeriodsStubData.accountingPeriodsWithMultipleItems
    }

    "return empty RdsAccountingPeriod" in {

      val result = repo.getAccountingPeriods(12L).futureValue

      result mustBe AccountingPeriodsStubData.emptyAccountingPeriods

    }

    "return downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repo.getAccountingPeriods(9798L).futureValue
      }

      exception.getMessage must include("Error from downstream")
    }

  }

}
