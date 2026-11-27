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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers.overarching

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.overarching.Enqueue
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.overarching.EnqueueLongRepository
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.overarching.EnqueueStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.Future

class EnqueueLongControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class EnqueueLongRepositoryStub extends EnqueueLongRepository {
    override def getEnqueueLong(messageIdIn: Long, sender: String, queue: String, replyQueue: String, correlationId: String, filter: String, key: String, dataType: Int, numberValue: Int): Future[Enqueue] = {
      Future.successful(EnqueueStubData.getEnqueue(messageIdIn: Long))
    }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[EnqueueLongRepository].toInstance(new EnqueueLongRepositoryStub())
      )
      .build()

  private final val endpoint = "/corporation-tax"

  "GET /corporation-tax/enqueue-long" should {

    "return 200 and enqueue long" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/enqueue-long/12345/sender1/queue1/replyQueue1/correlationId1/filter1/key1/1/1010").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[Enqueue] mustBe EnqueueStubData.enqueue
    }

    "return 500 when stub fails" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/enqueue-long/67890/senderNA/queueNA/replyQueueNA/correlationIdNA/filterNA/keyNA/2/2020").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()

      val response = get(s"$endpoint/enqueue-long/111213/senderNON/queueNON/replyQueueNON/correlationIdNON/filterNON/keyNON/3/3030").futureValue

      response.status mustBe UNAUTHORIZED
    }
  }

}
