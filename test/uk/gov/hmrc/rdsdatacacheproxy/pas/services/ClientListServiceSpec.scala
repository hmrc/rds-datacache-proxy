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

package uk.gov.hmrc.rdsdatacacheproxy.pas.services

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.*
import org.scalatest.matchers.must.Matchers.mustBe
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.cis.models.ClientListDownloadStatus
import uk.gov.hmrc.rdsdatacacheproxy.pas.repositories.ClientListSource

import scala.concurrent.Future

final class ClientListServiceSpec extends SpecBase {

  private val source = mock[ClientListSource]
  private val service = new ClientListService(source)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(source)
  }

  private val credentialId = "cred-123"
  private val serviceName = "service-xyz"
  private val gracePeriod = 14400

  "ClientListService.getClientListDownloadStatus" - {

    "return Right(InitiateDownload) when repository returns -1" in {
      when(source.getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod)))
        .thenReturn(Future.successful(-1))

      val result = service.getClientListDownloadStatus(credentialId, serviceName, gracePeriod)(using ec).futureValue

      result mustBe Right(ClientListDownloadStatus.InitiateDownload)
      verify(source).getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod))
      verifyNoMoreInteractions(source)
    }

    "return Right(InProgress) when repository returns 0" in {
      when(source.getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod)))
        .thenReturn(Future.successful(0))

      val result = service.getClientListDownloadStatus(credentialId, serviceName, gracePeriod)(using ec).futureValue

      result mustBe Right(ClientListDownloadStatus.InProgress)
      verify(source).getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod))
      verifyNoMoreInteractions(source)
    }

    "return Right(Succeeded) when repository returns 1" in {
      when(source.getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod)))
        .thenReturn(Future.successful(1))

      val result = service.getClientListDownloadStatus(credentialId, serviceName, gracePeriod)(using ec).futureValue

      result mustBe Right(ClientListDownloadStatus.Succeeded)
      verify(source).getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod))
      verifyNoMoreInteractions(source)
    }

    "return Right(Failed) when repository returns 2" in {
      when(source.getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod)))
        .thenReturn(Future.successful(2))

      val result = service.getClientListDownloadStatus(credentialId, serviceName, gracePeriod)(using ec).futureValue

      result mustBe Right(ClientListDownloadStatus.Failed)
      verify(source).getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod))
      verifyNoMoreInteractions(source)
    }

    "return Left with error message when repository returns unrecognized status code" in {
      when(source.getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod)))
        .thenReturn(Future.successful(99))

      val result = service.getClientListDownloadStatus(credentialId, serviceName, gracePeriod)(using ec).futureValue

      result mustBe Left("Could not map client list download status")
      verify(source).getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod))
      verifyNoMoreInteractions(source)
    }

    "use default grace period when not specified" in {
      when(source.getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(14400)))
        .thenReturn(Future.successful(1))

      val result = service.getClientListDownloadStatus(credentialId, serviceName)(using ec).futureValue

      result mustBe Right(ClientListDownloadStatus.Succeeded)
      verify(source).getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(14400))
      verifyNoMoreInteractions(source)
    }

    "propagate exceptions from repository" in {
      val exception = new RuntimeException("Database error")
      when(source.getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod)))
        .thenReturn(Future.failed(exception))

      val result = service.getClientListDownloadStatus(credentialId, serviceName, gracePeriod)(using ec).failed.futureValue

      result mustBe exception
      verify(source).getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod))
      verifyNoMoreInteractions(source)
    }
  }
}
