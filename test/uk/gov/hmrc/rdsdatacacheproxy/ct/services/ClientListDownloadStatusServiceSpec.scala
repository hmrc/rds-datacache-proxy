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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoMoreInteractions, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.ClientListDownloadStatusRepository
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.ClientDownloadStatus

import scala.concurrent.Future

class ClientListDownloadStatusServiceSpec extends AnyWordSpec with Matchers with ScalaFutures with MockitoSugar {

  private class Setup {
    val mockRepository: ClientListDownloadStatusRepository = mock[ClientListDownloadStatusRepository]
    val service = new ClientListDownloadStatusService(mockRepository)

    val credentialId: String = "1"
    val serviceName: String = "CT"
    val gracePeriod: Int = 14400

    val initiateDownload: ClientDownloadStatus = ClientDownloadStatus(-1)

  }

  "getClientListDownloadStatus returns an Int from repository" in new Setup {

    when(mockRepository.getClientListDownloadStatus(any[String], any[String], any[Int]))
      .thenReturn(Future.successful(initiateDownload))

    val result: ClientDownloadStatus = service.getClientListDownloadStatus(credentialId, serviceName, gracePeriod).futureValue

    result mustBe initiateDownload

    verify(mockRepository).getClientListDownloadStatus(credentialId, serviceName, gracePeriod)
  }

  "getTaxTransactions returns failure from repository" in new Setup {

    val ex = new RuntimeException("boom")

    when(mockRepository.getClientListDownloadStatus(any(), any(), any())).thenReturn(Future.failed(ex))

    val result: Throwable = service.getClientListDownloadStatus(credentialId, serviceName, gracePeriod).failed.futureValue

    result mustBe ex

    verify(mockRepository).getClientListDownloadStatus(credentialId, serviceName, gracePeriod)
    verifyNoMoreInteractions(mockRepository)

  }

}
