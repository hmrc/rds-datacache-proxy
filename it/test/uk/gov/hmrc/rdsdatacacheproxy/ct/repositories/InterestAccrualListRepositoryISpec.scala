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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.InterestAccruals
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.InterestAccrualListStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.ApplicationWithWiremock

import scala.concurrent.Future

class InterestAccrualListRepositoryISpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with ApplicationWithWiremock {

  class InterestAccrualListCacheRepositoryStub extends InterestAccrualListDatacacheRepository {
    override def getInterestAccrualList(taxRef: Long, accPeriod: Long, interestType: String): Future[InterestAccruals] =
    Future.successful(
      InterestAccrualListStubData.getAccrualInterestListItems(taxRef, accPeriod, interestType)
    )
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[InterestAccrualListDatacacheRepository].toInstance(new InterestAccrualListCacheRepositoryStub)
      )
      .build()

  private lazy val repository = app.injector.instanceOf[InterestAccrualListDatacacheRepository]

  "getInterestAccrualList" should {

    "return Interest Accrual List containing single item" in {

      val result = repository.getInterestAccrualList(1L, 1L, "IDB").futureValue

      result mustBe InterestAccrualListStubData.interestAccrualListSingleItem

    }

    "return Interest Accrual List containing multiple items" in {

      val result = repository.getInterestAccrualList(2L, 1L, "IDB").futureValue

      result mustBe InterestAccrualListStubData.interestAccrualListMultipleItems

    }

    "return empty Interest Accrual List" in {

      val result = repository.getInterestAccrualList(3L, 1L, "IDB").futureValue

      result mustBe InterestAccrualListStubData.interestAccrualListEmpty

    }

    "propagate downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repository.getInterestAccrualList(99L, 1L, "IDB").futureValue
      }

      exception.getMessage must include("Simulated downstream failure")
    }

  }

}
