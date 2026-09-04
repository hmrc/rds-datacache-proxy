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
import play.api.inject.bind
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.AdjustmentTransactions
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.AdjustmentTransactionsStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.ApplicationWithWiremock

import scala.concurrent.Future

class AdjustmentTransactionsRepositoryISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with ApplicationWithWiremock {

  class AdjustmentTransactionsRepositoryStub extends AdjustmentTransactionsRepository {

    override def getAdjustmentTransactions(taxRef: Long, accPeriod: Long): Future[List[AdjustmentTransactions]] =
      Future.successful(AdjustmentTransactionsStubData.getAdjustmentTransactions(taxRef: Long, accPeriod: Long))
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[AdjustmentTransactionsRepository].toInstance(new AdjustmentTransactionsRepositoryStub)
      )
      .build()

  private lazy val repo = app.injector.instanceOf[AdjustmentTransactionsRepository]

  "getAdjustmentTransactions" should {

    "return adjustment transactions containing 3 items" in {

      val result = repo.getAdjustmentTransactions(1L, 2L).futureValue

      result mustBe AdjustmentTransactionsStubData.getAdjustmentTransactions(1L, 2L)
    }

    "return empty adjustment transactions" in {

      val result = repo.getAdjustmentTransactions(2L, 3L).futureValue

      result mustBe AdjustmentTransactionsStubData.emptyAdjustmentTransactions

    }

    "return downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repo.getAdjustmentTransactions(200L, 2L).futureValue
      }

      exception.getMessage must include("Downstream error")
    }

  }

}
