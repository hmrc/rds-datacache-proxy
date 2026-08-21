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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.DisplayNeededItem
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.DisplayNeededStubData

import scala.concurrent.Future

class DisplayNeededRepositoryISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with GuiceOneAppPerSuite {

  class DisplayNeededRepositoryStub extends DisplayNeededRepository {
    override def getDisplayNeeded(taxRef: Long, accPeriod: Long): Future[DisplayNeededItem] =
      Future.successful(DisplayNeededStubData.getDisplayNeeded(taxRef, accPeriod))
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[DisplayNeededRepository].toInstance(new DisplayNeededRepositoryStub))
    .build()

  private lazy val repository: DisplayNeededRepository = app.injector.instanceOf[DisplayNeededRepository]

  "getDisplayNeeded" should {

    "return Display Needed with all flags set as false" in {
      val result = repository.getDisplayNeeded(10L, 1L).futureValue

      result mustBe DisplayNeededStubData.displayNeededItemAllFalse
    }

    "return Display Needed with all flags set as true" in {
      val result = repository.getDisplayNeeded(20L, 1L).futureValue

      result mustBe DisplayNeededStubData.displayNeededItemAllTrue
    }

    "return Display Needed with some flags set as true and false" in {
      val result = repository.getDisplayNeeded(30L, 1L).futureValue

      result mustBe DisplayNeededStubData.displayNeededItemMixed
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repository.getDisplayNeeded(999L, 1L).futureValue
      }

      exception.getMessage must include("Error from downstream")
    }

  }

}
