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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.APBalancedItem
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.AccountingPeriodDetailsStubData

import scala.concurrent.Future

class AccountingPeriodDetailsRepositoryISpec extends AnyWordSpec
  with Matchers with ScalaFutures with IntegrationPatience
  with GuiceOneAppPerSuite
  with AccountingPeriodDetailsStubData {

  class AccountingPeriodDetailsRepositoryRdsStub extends AccountingPeriodDetailsRepository {
    def getIsAPBalanced(taxRef: Long, accPeriod: Long): Future[APBalancedItem] =
      Future.successful(
        getIsAPBalancedData(taxRef, accPeriod)
      )
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[AccountingPeriodDetailsRepository].toInstance(new AccountingPeriodDetailsRepositoryRdsStub))
    .build()

  private lazy val repository: AccountingPeriodDetailsRepository = app.injector.instanceOf[AccountingPeriodDetailsRepository]

  "getIsAPBalanced" should {

    "return correct APBalancedItem item" in {
      val result = repository.getIsAPBalanced(1L, 1L).futureValue

      result mustBe aPBalancedItemDefault
    }

    "return correct AccountingPeriodDetails empty record" in {
      val result = repository.getIsAPBalanced(100L, 17L).futureValue

      result mustBe aPBalancedItemEmpty
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[Error] {
        repository.getIsAPBalanced(101L, 29L).futureValue
      }

      exception.getMessage must include("Simulated downstream failure")
    }

  }

}
