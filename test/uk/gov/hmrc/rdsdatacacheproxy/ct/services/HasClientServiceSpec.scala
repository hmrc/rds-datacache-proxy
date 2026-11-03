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

package uk.gov.hmrc.rdsdatacacheproxy.ct.services

import org.mockito.Mockito.{verify, verifyNoMoreInteractions, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.HasClient
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.HasClientRepository

import scala.concurrent.Future

class HasClientServiceSpec extends AnyWordSpec with Matchers with ScalaFutures with MockitoSugar {

  private class Setup {
    val mockRepository: HasClientRepository = mock[HasClientRepository]
    val service = new HasClientService(mockRepository)

    val utr: Long = 1234567890L
    val irAgentId: String = "AGENT123"
    val credentialId: String = "CREDENTIAL123"

    val hasClient: HasClient = HasClient(status = 1)
  }

  "hasClient returns status retrieved from repository" in new Setup {

    when(mockRepository.hasClient(utr, irAgentId, credentialId))
      .thenReturn(Future.successful(hasClient))

    val result: HasClient = service.hasClient(utr, irAgentId, credentialId).futureValue

    result mustBe hasClient

    verify(mockRepository).hasClient(utr, irAgentId, credentialId)
  }

  "hasClient returns failure from repository" in new Setup {

    val ex = new RuntimeException("boom")

    when(mockRepository.hasClient(utr, irAgentId, credentialId))
      .thenReturn(Future.failed(ex))

    val result: Throwable = service.hasClient(utr, irAgentId, credentialId).failed.futureValue

    result mustBe ex

    verify(mockRepository).hasClient(utr, irAgentId, credentialId)
    verifyNoMoreInteractions(mockRepository)
  }

}
