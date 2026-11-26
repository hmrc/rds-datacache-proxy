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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.overarching

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.overarching.Enqueue
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.overarching.EnqueueStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.ApplicationWithWiremock

import scala.concurrent.Future

class EnqueueLongRepositoryISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with ApplicationWithWiremock {

  class EnqueueLongRepositoryStub extends EnqueueLongRepository {

    override def getEnqueueLong(messageIdIn: Long, sender: String, queue: String, replyQueue: String, correlationId: String, filter: String, key: String, dataType: Int, numberValue: Int): Future[Enqueue] =
      Future.successful(EnqueueStubData.getEnqueue(messageIdIn: Long))
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[EnqueueLongRepository].toInstance(new EnqueueLongRepositoryStub)
      )
      .build()

  private lazy val repo = app.injector.instanceOf[EnqueueLongRepository]

  "getEnqueueLong" should {

    "return enqueue long" in {

      val result = repo.getEnqueueLong(12345, "sender1", "queue1", "replyQueue1", "correlationId1", "filter1", "key1", 1, 1010).futureValue

      result mustBe EnqueueStubData.enqueue

    }

    "return downstream failure from stub" in {
      val exception = intercept[RuntimeException] {

        repo.getEnqueueLong(67890, "sender2", "queue2", "replyQueue2", "correlationId2", "filter2", "key2", 2, 2020).futureValue
      }

      exception.getMessage must include("Downstream error")
    }

  }

}
