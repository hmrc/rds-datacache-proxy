/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy.cis.services

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.{reset, verify, verifyNoMoreInteractions, when}
import org.scalatest.matchers.must.Matchers.mustBe
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.cis.models.{EnqueueMessage, EnqueueMessageRequest, EnqueueNumber, EnqueueTracking}
import uk.gov.hmrc.rdsdatacacheproxy.cis.repositories.CisMonthlyReturnSource

import scala.concurrent.Future

final class UdasQueueServiceSpec extends SpecBase {

  private val source = mock[CisMonthlyReturnSource]
  private val service = new UdasQueueService(source)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(source)
  }

  "CisTaxpayerService#enqueueMessage" - {
    val request: EnqueueMessageRequest = EnqueueMessageRequest(
      message = EnqueueMessage(
        sender        = "Portal",
        queueName     = "AGTAUTH",
        replyQueue    = "",
        correlationID = "",
        filter        = "RemoveClient",
        payload = Map(
          "IRAgentID"    -> "123456789",
          "Service"      -> "CIS",
          "TaxReference" -> "123/ABC123"
        )
      ),
      tracking = Some(
        EnqueueTracking(
          message = EnqueueMessage(
            sender        = "Portal",
            queueName     = "AGTAUTH",
            replyQueue    = "",
            correlationID = "",
            filter        = "AGENTAUTH",
            payload = Map(
              "GGIS_DTSTAMP"    -> "20260826",
              "MESSAGE_TYPE"    -> "AGENT_AUTH_PORTAL",
              "ADDITIONAL_INFO" -> "Request client removal",
              "GW_AGENT_ID"     -> "AGENT123",
              "IR_CLIENT_REF"   -> "123/ABC123",
              "USER_ID"         -> "user123",
              "Service"         -> "CIS"
            )
          ),
          number = EnqueueNumber(
            dataType = 1,
            payload = Map(
              "EVENT_TYPE" -> 1010L
            )
          )
        )
      )
    )

    "return messageId when the repository succeeded" in {
      when(source.enqueueMessage(eqTo(request)))
        .thenReturn(Future.successful(10L))
      val out = service.enqueueMessage(request).futureValue

      verify(source).enqueueMessage(eqTo(request))
      verifyNoMoreInteractions(source)
      out mustBe 10L
    }

    "propagate upstream failures from the repository" in {
      val boom = UpstreamErrorResponse("db exploded", 502)

      when(source.enqueueMessage(eqTo(request)))
        .thenReturn(Future.failed(boom))

      val ex = service.enqueueMessage(request).failed.futureValue
      ex mustBe boom

      verify(source).enqueueMessage(eqTo(request))
      verifyNoMoreInteractions(source)
    }
  }
}
