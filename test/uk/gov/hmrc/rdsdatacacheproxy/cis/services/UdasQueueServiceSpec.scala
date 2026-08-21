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
import uk.gov.hmrc.rdsdatacacheproxy.cis.models.{EnqueueClobRequest, EnqueueMessageHeaderRequest}
import uk.gov.hmrc.rdsdatacacheproxy.cis.repositories.CisMonthlyReturnSource

import scala.concurrent.Future

final class UdasQueueServiceSpec extends SpecBase {

  private val source = mock[CisMonthlyReturnSource]
  private val service = new UdasQueueService(source)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(source)
  }

  "CisTaxpayerService#enqueueMessageHeader" - {

    val request: EnqueueMessageHeaderRequest = EnqueueMessageHeaderRequest(
      sender        = "Portal",
      queueName     = "AGTAUTH",
      replyQueue    = "",
      correlationId = "",
      filter        = "RemoveClient"
    )

    "return messageId when the repository succeeded" in {
      when(source.enqueueMessageHeader(eqTo(request)))
        .thenReturn(Future.successful(10L))
      val out = service.enqueueMessageHeader(request).futureValue

      verify(source).enqueueMessageHeader(eqTo(request))
      verifyNoMoreInteractions(source)
      out mustBe 10L
    }

    "propagate upstream failures from the repository" in {
      val boom = UpstreamErrorResponse("db exploded", 502)

      when(source.enqueueMessageHeader(eqTo(request)))
        .thenReturn(Future.failed(boom))

      val ex = service.enqueueMessageHeader(request).failed.futureValue
      ex mustBe boom

      verify(source).enqueueMessageHeader(eqTo(request))
      verifyNoMoreInteractions(source)
    }
  }

  "CisTaxpayerService#enqueueClob" - {
    val request: EnqueueClobRequest = EnqueueClobRequest(
      messageId     = 12345L,
      sender        = "Portal",
      queueName     = "AGTAUTH",
      replyQueue    = "",
      correlationId = "",
      filter        = "RemoveClient",
      payload = Map(
        "IRAgentID"    -> "123456789",
        "Service"      -> "CIS",
        "TaxReference" -> "123/ABC123"
      )
    )

    "return messageId when the repository succeeded" in {
      when(source.enqueueClob(eqTo(request)))
        .thenReturn(Future.successful(10L))
      val out = service.enqueueClob(request).futureValue

      verify(source).enqueueClob(eqTo(request))
      verifyNoMoreInteractions(source)
      out mustBe 10L
    }

    "propagate upstream failures from the repository" in {
      val boom = UpstreamErrorResponse("db exploded", 502)

      when(source.enqueueClob(eqTo(request)))
        .thenReturn(Future.failed(boom))

      val ex = service.enqueueClob(request).failed.futureValue
      ex mustBe boom

      verify(source).enqueueClob(eqTo(request))
      verifyNoMoreInteractions(source)
    }
  }
}
