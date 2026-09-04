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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.Repayments
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.RepaymentsStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.ApplicationWithWiremock

import scala.concurrent.Future

class RepaymentsRepositoryISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with ApplicationWithWiremock {

  class RepaymentsRepositoryStub extends RepaymentsRepository {

    override def getRepayments(taxRef: Long, accPeriod: Long): Future[Repayments] =
      Future.successful(RepaymentsStubData.getRepayments(taxRef: Long, accPeriod: Long))
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[RepaymentsRepository].toInstance(new RepaymentsRepositoryStub)
      )
      .build()

  private lazy val repo = app.injector.instanceOf[RepaymentsRepository]

  "getRepayments" should {

    "return repayments with one item " in {

      val result = repo.getRepayments(10L, 2L).futureValue

      result mustBe RepaymentsStubData.repaymentsWithOneItem

    }

    "return repayments with multiple items " in {

      val result = repo.getRepayments(20L, 2L).futureValue

      result mustBe RepaymentsStubData.repaymentsWithMultipleItems
    }

    "return empty payment repayment reallocations" in {

      val result = repo.getRepayments(1L, 3L).futureValue

      result mustBe RepaymentsStubData.emptyRepayments

    }

    "return downstream failure from stub" in {
      val exception = intercept[RuntimeException] {

        repo.getRepayments(200L, 2L).futureValue
      }

      exception.getMessage must include("Downstream error")
    }

  }

}
