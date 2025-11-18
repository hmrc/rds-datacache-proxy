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

package uk.gov.hmrc.rdsdatacacheproxy.cis.controllers

import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.cis.models.ClientListDownloadStatus
import uk.gov.hmrc.rdsdatacacheproxy.cis.services.ClientService

import scala.concurrent.{ExecutionContext, Future}

class ClientControllerSpec extends SpecBase with MockitoSugar {
  "ClientController#getClientListDownloadStatus" - {

    "returns 200 with status 'InitiateDownload' when service returns Right(InitiateDownload)" in new Setup {
      when(mockService.getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Right(ClientListDownloadStatus.InitiateDownload)))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&serviceName=service-xyz&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "status").as[String] mustBe "InitiateDownload"
      verify(mockService).getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext])
      verifyNoMoreInteractions(mockService)
    }

    "returns 200 with status 'InProgress' when service returns Right(InProgress)" in new Setup {
      when(mockService.getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Right(ClientListDownloadStatus.InProgress)))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&serviceName=service-xyz&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      status(res) mustBe OK
      (contentAsJson(res) \ "status").as[String] mustBe "InProgress"
    }

    "returns 200 with status 'Succeeded' when service returns Right(Succeeded)" in new Setup {
      when(mockService.getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Right(ClientListDownloadStatus.Succeeded)))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&serviceName=service-xyz&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      status(res) mustBe OK
      (contentAsJson(res) \ "status").as[String] mustBe "Succeeded"
    }

    "returns 200 with status 'Failed' when service returns Right(Failed)" in new Setup {
      when(mockService.getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Right(ClientListDownloadStatus.Failed)))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&serviceName=service-xyz&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      status(res) mustBe OK
      (contentAsJson(res) \ "status").as[String] mustBe "Failed"
    }

    "returns 500 with error message when service returns Left(error)" in new Setup {
      when(mockService.getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Left("Could not map client list download status")))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&serviceName=service-xyz&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      status(res) mustBe INTERNAL_SERVER_ERROR
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "error").as[String] mustBe "Could not map client list download status"
      verify(mockService).getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext])
      verifyNoMoreInteractions(mockService)
    }

    "uses default grace period when not specified" in new Setup {
      when(mockService.getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Right(ClientListDownloadStatus.Succeeded)))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&serviceName=service-xyz")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      status(res) mustBe OK
      verify(mockService).getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext])
    }

    "propagates exceptions from service" in new Setup {
      when(mockService.getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.failed(new RuntimeException("Database error")))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&serviceName=service-xyz&gracePeriod=14400")
      val res = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      whenReady(res.failed) { ex =>
        ex mustBe a[RuntimeException]
        ex.getMessage mustBe "Database error"
      }
    }

    "handles custom grace period values" in new Setup {
      when(mockService.getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(7200))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Right(ClientListDownloadStatus.Succeeded)))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&serviceName=service-xyz&gracePeriod=7200")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz", 7200)(req)

      status(res) mustBe OK
      verify(mockService).getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(7200))(using any[ExecutionContext])
    }

    "returns 400 when credentialId is empty" in new Setup {
      val req = FakeRequest(GET, "/client-list-status?credentialId=&serviceName=service-xyz&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("", "service-xyz")(req)

      status(res) mustBe BAD_REQUEST
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "error").as[String] mustBe "credentialId and serviceName must be provided"
      verifyNoInteractions(mockService)
    }

    "returns 400 when serviceName is empty" in new Setup {
      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&serviceName=&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "")(req)

      status(res) mustBe BAD_REQUEST
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "error").as[String] mustBe "credentialId and serviceName must be provided"
      verifyNoInteractions(mockService)
    }
  }

  private trait Setup {
    val mockService: ClientService = mock[ClientService]
    val controller = new ClientController(fakeAuthAction, mockService, cc)(using ec)
  }
}
